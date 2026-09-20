package me.collumy.colib.command.context

internal object ClientCommandContextHolder {
    private val threadLocalContext = ThreadLocal<ClientCommandContext>()

    val current: ClientCommandContext
        get() = threadLocalContext.get()
            ?: error("Контекст команды недоступен за пределами её выполнения!")

    inline fun <R> runInContext(ctx: ClientCommandContext, block: () -> R): R {
        threadLocalContext.set(ctx)
        return try { block() } finally { threadLocalContext.remove() }
    }
}