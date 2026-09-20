package me.collumy.colib.command.context

import me.collumy.colib.command.base.ClientCommandArgument

class ClientCommandContext(
    private val arguments: Map<String, Any>
) {

    /** Получение по имени аргумента (для работы делегата) */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): T {
        return arguments[name] as? T
            ?: throw IllegalArgumentException("Аргумент '$name' не найден в контексте команды")
    }

    /** Оператор индексации: ctx[arg] */
    operator fun <T> get(arg: ClientCommandArgument.Dynamic<T>): T = get(arg)
}