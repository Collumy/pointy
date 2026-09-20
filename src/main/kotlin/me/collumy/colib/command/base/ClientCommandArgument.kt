package me.collumy.colib.command.base

import me.collumy.colib.command.context.ClientCommandContext
import me.collumy.colib.command.context.ClientCommandContextHolder
import kotlin.reflect.KProperty

sealed class ClientCommandArgument<T>(val name: String) {

    /** Фиксированное слово (Literal) */
    class Literal(name: String) : ClientCommandArgument<Unit>(name)

    /** Динамический аргумент со значением */
    class Dynamic<T>(
        name: String,
        val type: ClientArgumentType<T>,
        var suggestsBlock: ((ClientCommandContext) -> List<String>)? = null
    ) : ClientCommandArgument<T>(name) {

        fun suggest(vararg suggestions: String): Dynamic<T> = apply {
            this.suggestsBlock = { suggestions.toList() }
        }

        fun suggest(suggestions: List<String>): Dynamic<T> = apply {
            this.suggestsBlock = { suggestions }
        }

        fun suggest(provider: (ClientCommandContext) -> Collection<String>): Dynamic<T> = apply {
            this.suggestsBlock = { ctx -> provider(ctx).toList() }
        }

        /** Удобный делегат для получения значения прямо из контекста */
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return ClientCommandContextHolder.current.get(name)
        }
    }
}