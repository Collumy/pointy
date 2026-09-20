package me.collumy.colib.command

import me.collumy.colib.command.bridge.CommandRegistrationBridge

object ClientCommandManager {
    private val commands = mutableMapOf<String, ClientCommand>()

    init {
        CommandRegistrationBridge.init()
    }

    /** Регистрация новой команды */
    fun register(command: ClientCommand) {
        commands[command.name.lowercase()] = command

        command.aliases.forEach { alias ->
            commands[alias.lowercase()] = command
        }

        CommandRegistrationBridge.onRegister(command)
    }

    /** Полное удаление команды по имени или алиасу */
    fun unregister(commandName: String) {
        val cmd = commands.remove(commandName.lowercase()) ?: return

        // Удаляем основное имя и все алиасы
        commands.entries.removeIf { it.value == cmd }
        CommandRegistrationBridge.onUnregister(cmd.name)
    }

    /** Список всех уникальных зарегистрированных команд */
    fun getAll(): List<ClientCommand> {
        return commands.values.distinct()
    }

    /** Очистка всех команд */
    fun clear() {
        val allNames = commands.keys.toList()
        commands.clear()
        allNames.forEach { CommandRegistrationBridge.onUnregister(it) }
    }
}