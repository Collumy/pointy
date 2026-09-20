package me.collumy.colib.command.bridge

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import me.collumy.colib.command.ClientCommand
import me.collumy.colib.command.ClientCommandManager
import me.collumy.colib.command.base.ClientArgumentType
import me.collumy.colib.command.base.ClientCommandArgument
import me.collumy.colib.command.context.ClientCommandContext
import me.collumy.colib.command.context.ClientCommandContextHolder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object CommandRegistrationBridge {

    private var activeDispatcher: CommandDispatcher<FabricClientCommandSource>? = null

    /** Инициализация моста при запуске мода */
    fun init() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            this.activeDispatcher = dispatcher

            // Регистрируем все команды, добавленные в ClientCommandRegistry до загрузки мира
            for (command in ClientCommandManager.getAll()) {
                registerToBrigadier(command, dispatcher)
            }
        }
    }

    /** Вызывается из ClientCommandRegistry.register() */
    fun onRegister(command: ClientCommand) {
        val dispatcher = activeDispatcher ?: return // Если диспетчер еще не готов, зарегает в init()
        registerToBrigadier(command, dispatcher)
    }

    /** Вызывается из ClientCommandRegistry.unregister() */
    fun onUnregister(commandName: String) {
        val dispatcher = activeDispatcher ?: return
        removeNodeFromBrigadier(commandName.lowercase(), dispatcher)
    }

    // --- Внутренняя конвертация дерева ClientCommand в Brigadier ---

    private fun registerToBrigadier(
        command: ClientCommand,
        dispatcher: CommandDispatcher<FabricClientCommandSource>
    ) {
        val rootNames = listOf(command.name) + command.aliases

        for (name in rootNames) {
            val literalBuilder = LiteralArgumentBuilder.literal<FabricClientCommandSource>(name)

            // Действие по умолчанию (/cmd)
            command.defaultAction?.let { action ->
                literalBuilder.executes { ctx ->
                    executeSafely(command, ctx) {
                        action.invoke(it)
                    }
                }
            }

            // Подключение подкоманд ("sub" { ... })
            for (sub in command.subcommands) {
                literalBuilder.then(buildSubcommandNode(sub, command))
            }

            // Подключение веток с аргументами (branch { ... })
            for (branch in command.branches) {
                if (branch.args.isNotEmpty()) {
                    literalBuilder.then(buildBranchChain(branch.args, branch.action, command))
                }
            }

            dispatcher.register(literalBuilder)
        }
    }

    private fun buildSubcommandNode(
        sub: ClientCommand,
        rootCmd: ClientCommand
    ): LiteralArgumentBuilder<FabricClientCommandSource> {
        val builder = LiteralArgumentBuilder.literal<FabricClientCommandSource>(sub.name)

        sub.defaultAction?.let { action ->
            builder.executes { ctx ->
                executeSafely(rootCmd, ctx) { action.invoke(it) }
            }
        }

        for (nestedSub in sub.subcommands) {
            builder.then(buildSubcommandNode(nestedSub, rootCmd))
        }

        for (branch in sub.branches) {
            if (branch.args.isNotEmpty()) {
                builder.then(buildBranchChain(branch.args, branch.action, rootCmd))
            }
        }

        return builder
    }

    private fun buildBranchChain(
        nodes: List<ClientCommandArgument<*>>,
        action: (ClientCommandContext.() -> Unit)?,
        rootCmd: ClientCommand
    ): ArgumentBuilder<FabricClientCommandSource, *> {
        val first = nodes.first()
        val rest = nodes.drop(1)

        val currentBuilder: ArgumentBuilder<FabricClientCommandSource, *> = when (first) {
            is ClientCommandArgument.Literal -> LiteralArgumentBuilder.literal(first.name)
            is ClientCommandArgument.Dynamic<*> -> {
                val argType = mapArgumentType(first.type)
                val argBuilder = createArgumentBuilder<Any>(first.name, argType)

                // Подключение подсказок (Tab Completion)
                first.suggestsBlock?.let { suggestFn ->
                    argBuilder.suggests { ctx, builder ->
                        val customContext = buildCustomContext(ctx)
                        suggestFn(customContext).forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                }
                argBuilder
            }
        }

        if (rest.isNotEmpty()) {
            currentBuilder.then(buildBranchChain(rest, action, rootCmd))
        } else if (action != null) {
            currentBuilder.executes { ctx ->
                executeSafely(rootCmd, ctx) { action.invoke(it) }
            }
        }

        return currentBuilder
    }

    // --- Вспомогательные функции ---

    private fun executeSafely(
        command: ClientCommand,
        brigadierCtx: CommandContext<FabricClientCommandSource>,
        block: (ClientCommandContext) -> Unit
    ): Int {
        val customContext = buildCustomContext(brigadierCtx)
        return try {
            ClientCommandContextHolder.runInContext(customContext) {
                block(customContext)
            }
            1
        } catch (e: Throwable) {
            0
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> createArgumentBuilder(
        name: String,
        type: ArgumentType<*>
    ): RequiredArgumentBuilder<FabricClientCommandSource, T> {
        return RequiredArgumentBuilder.argument<FabricClientCommandSource, T>(name, type as ArgumentType<T>)
    }

    private fun buildCustomContext(
        ctx: CommandContext<FabricClientCommandSource>
    ): ClientCommandContext {
        val parsedArgs = mutableMapOf<String, Any>()

        for (node in ctx.nodes) {
            val name = node.node.name
            try {
                val value = ctx.getArgument(name, Any::class.java)
                parsedArgs[name] = value
            } catch (_: IllegalArgumentException) { }
        }

        return ClientCommandContext(parsedArgs)
    }

    private fun mapArgumentType(type: ClientArgumentType<*>): ArgumentType<*> {
        return when (type) {
            is ClientArgumentType.WordArg -> StringArgumentType.word()
            is ClientArgumentType.StringArg -> StringArgumentType.string()
            is ClientArgumentType.EverythingArg -> StringArgumentType.greedyString()
            is ClientArgumentType.IntArg -> IntegerArgumentType.integer()
            is ClientArgumentType.DoubleArg -> DoubleArgumentType.doubleArg()
        }
    }

    /** Безопасное удаление узла из дерева Brigadier через рефлексию */
    @Suppress("UNCHECKED_CAST")
    private fun removeNodeFromBrigadier(
        commandName: String,
        dispatcher: CommandDispatcher<FabricClientCommandSource>
    ) {
        try {
            val root = dispatcher.root
            val childrenField = CommandNode::class.java.getDeclaredField("children").apply { isAccessible = true }
            val literalsField = CommandNode::class.java.getDeclaredField("literals").apply { isAccessible = true }

            val children = childrenField.get(root) as MutableMap<String, CommandNode<FabricClientCommandSource>>
            val literals = literalsField.get(root) as MutableMap<String, CommandNode<FabricClientCommandSource>>

            children.remove(commandName)
            literals.remove(commandName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}