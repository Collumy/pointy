package me.collumy.colib.command.base

sealed interface ClientArgumentType<T> {
    object WordArg : ClientArgumentType<String>
    object StringArg : ClientArgumentType<String>
    object EverythingArg : ClientArgumentType<String>

    object IntArg : ClientArgumentType<Int>
    object DoubleArg : ClientArgumentType<Double>
}