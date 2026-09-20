package me.collumy.colib.command

import me.collumy.colib.command.base.CommandBranch
import me.collumy.colib.command.base.CommandBranchBuilder
import me.collumy.colib.command.context.ClientCommandContext

class ClientCommand(
    val name: String,
    vararg val aliases: String,
    builder: (ClientCommand.() -> Unit)
) {
    internal val subcommands = mutableListOf<ClientCommand>()
    internal val branches = mutableListOf<CommandBranch>()
    internal var defaultAction: (ClientCommandContext.() -> Unit)? = null

    init {
        builder.invoke(this)
    }

    operator fun String.invoke(vararg aliases: String, block: ClientCommand.() -> Unit) {
        val sub = ClientCommand(this, *aliases, builder = block)
        subcommands.add(sub)
    }

    /** Подключение уже созданной команды из переменной */
    fun subcommand(command: ClientCommand) {
        subcommands.add(command)
    }

    /** Действие по умолчанию при вызове /cmd без аргументов */
    fun executes(action: ClientCommandContext.() -> Unit) {
        this.defaultAction = action
    }

    /** Определение пути с аргументами (/cmd <arg>) */
    fun branch(block: CommandBranchBuilder.() -> Unit) {
        val pathBuilder = CommandBranchBuilder().apply(block)
        branches.add(pathBuilder.build())
    }
}