package me.collumy.colib.command.base

import me.collumy.colib.command.context.ClientCommandContext

class CommandBranchBuilder {
    internal val args = mutableListOf<ClientCommandArgument<*>>()
    internal var action: (ClientCommandContext.() -> Unit)? = null

    /** Фиксированный литерал в ветке */
    fun literal(name: String): ClientCommandArgument.Literal {
        return ClientCommandArgument.Literal(name).also { args.add(it) }
    }

    /** Обычное слово (одно слово до пробела) */
    fun word(name: String): ClientCommandArgument.Dynamic<String> {
        return ClientCommandArgument.Dynamic(name, ClientArgumentType.WordArg).also { args.add(it) }
    }

    /** Строка (может быть в кавычках) */
    fun string(name: String): ClientCommandArgument.Dynamic<String> {
        return ClientCommandArgument.Dynamic(name, ClientArgumentType.StringArg).also { args.add(it) }
    }

    /** Захват всего оставшегося текста */
    fun everything(name: String): ClientCommandArgument.Dynamic<String> {
        return ClientCommandArgument.Dynamic(name, ClientArgumentType.EverythingArg).also { args.add(it) }
    }

    /** Целое число с диапазоном */
    fun int(name: String): ClientCommandArgument.Dynamic<Int> {
        return ClientCommandArgument.Dynamic(name, ClientArgumentType.IntArg).also { args.add(it) }
    }

    /** Дробное число с диапазоном */
    fun double(name: String): ClientCommandArgument.Dynamic<Double> {
        return ClientCommandArgument.Dynamic(name, ClientArgumentType.DoubleArg).also { args.add(it) }
    }

    /** Исполняемый блок для данной ветки */
    fun executes(block: ClientCommandContext.() -> Unit) {
        this.action = block
    }

    internal fun build() = CommandBranch(args.toList(), action)
}