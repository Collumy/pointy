package me.collumy.colib.scheduler

import me.collumy.colib.scheduler.bridge.SchedulerBridge
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

object Scheduler {

    private val idCounter = AtomicLong(0)
    private val activeTasks = ConcurrentHashMap<Long, InternalTask>()
    private val asyncExecutor = Executors.newScheduledThreadPool(4)

    init {
        SchedulerBridge.init()
    }

    // Очередь задач, ожидающих выполнения на Главном потоке (Main Thread)
    internal val mainThreadQueue = ConcurrentHashMap<Long, InternalTask>()

    /** Запуск задачи на Главном потоке Minecraft */
    fun runOnMain(delay: TimeAmount = 0.ms, block: (ScheduledTask) -> Unit): ScheduledTask {
        val task = InternalTask(idCounter.incrementAndGet(), isAsync = false, delay = delay, block = block)
        activeTasks[task.id] = task

        if (delay.toMillis <= 0) {
            mainThreadQueue[task.id] = task
        } else {
            asyncExecutor.schedule({
                if (!task.isCancelled) {
                    mainThreadQueue[task.id] = task
                }
            }, delay.amount, delay.unit)
        }

        return task
    }

    /** Запуск задачи в фоновом потоке (Async) */
    fun runAsync(delay: TimeAmount = 0.ms, block: (ScheduledTask) -> Unit): ScheduledTask {
        val task = InternalTask(idCounter.incrementAndGet(), isAsync = true, delay = delay, block = block)
        activeTasks[task.id] = task

        val runnable = Runnable {
            if (!task.isCancelled) {
                try {
                    block(task)
                } finally {
                    if (task.period.toMillis <= 0) activeTasks.remove(task.id)
                }
            }
        }

        if (delay.toMillis <= 0) {
            asyncExecutor.submit(runnable)
        } else {
            asyncExecutor.schedule(runnable, delay.amount, delay.unit)
        }

        return task
    }

    /** Повторяющаяся задача (Repeating Task) */
    fun repeat(
        period: TimeAmount,
        delay: TimeAmount = 0.ms,
        async: Boolean = false,
        block: (ScheduledTask) -> Unit
    ): ScheduledTask {
        val task = InternalTask(
            id = idCounter.incrementAndGet(),
            isAsync = async,
            delay = delay,
            period = period,
            block = block
        )
        activeTasks[task.id] = task

        if (async) {
            asyncExecutor.scheduleAtFixedRate({
                if (task.isCancelled) {
                    activeTasks.remove(task.id)
                } else {
                    block(task)
                }
            }, delay.amount, period.amount, period.unit)
        } else {
            // Для главного потока планирование происходит через тики в SchedulerBridge
            task.nextExecutionTime = System.currentTimeMillis() + delay.toMillis
        }

        return task
    }

    /**
     * Выполняет задачу с заданным интервалом ДО ТЕХ ПОР, пока [condition] возвращает `true`.
     * Как только [condition] становится `false`, задача автоматически отменяется.
     */
    fun runWhile(
        delay: TimeAmount = 0.ms,
        period: TimeAmount = 1.ticks,
        async: Boolean = false,
        condition: () -> Boolean,
        block: (ScheduledTask) -> Unit
    ): ScheduledTask {
        return repeat(delay, period, async) { task ->
            if (!condition()) {
                task.cancel()
                return@repeat
            }
            block(task)
        }
    }

    /**
     * Выполняет задачу с заданным интервалом ДО ТЕХ ПОР, пока [condition] НЕ станет `true`.
     * Удобно для ожидания наступления события (например, пока игрок не приземлится на землю).
     */
    fun runUntil(
        delay: TimeAmount = 0.ms,
        period: TimeAmount = 1.ticks,
        async: Boolean = false,
        condition: () -> Boolean,
        block: (ScheduledTask) -> Unit
    ): ScheduledTask {
        return runWhile(delay, period, async, condition = { !condition() }, block = block)
    }

    /** Вызывается из Моста (Bridge) на каждом тике Главного потока Minecraft */
    internal fun tickMainThread() {
        if (mainThreadQueue.isEmpty() && activeTasks.isEmpty()) return

        val now = System.currentTimeMillis()

        // 1. Выполняем разовые задачи из очереди
        val iterator = mainThreadQueue.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val task = entry.value
            iterator.remove()

            if (!task.isCancelled) {
                try {
                    task.block(task)
                } finally {
                    activeTasks.remove(task.id)
                }
            }
        }

        // 2. Проверяем повторяющиеся задачи для главного потока
        for (task in activeTasks.values) {
            if (!task.isAsync && task.period.toMillis > 0 && !task.isCancelled) {
                if (now >= task.nextExecutionTime) {
                    task.nextExecutionTime = now + task.period.toMillis
                    try {
                        task.block(task)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    internal class InternalTask(
        override val id: Long,
        val isAsync: Boolean,
        val delay: TimeAmount,
        val period: TimeAmount = 0.ms,
        val block: (ScheduledTask) -> Unit
    ) : ScheduledTask {
        @Volatile
        override var isCancelled: Boolean = false
        var nextExecutionTime: Long = 0

        override fun cancel() {
            isCancelled = true
            activeTasks.remove(id)
            mainThreadQueue.remove(id)
        }
    }
}