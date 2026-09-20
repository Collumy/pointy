package me.collumy.colib.command.base

import me.collumy.colib.command.context.ClientCommandContext

class CommandBranch(
    val args: List<ClientCommandArgument<*>>,
    val action: (ClientCommandContext.() -> Unit)?
)