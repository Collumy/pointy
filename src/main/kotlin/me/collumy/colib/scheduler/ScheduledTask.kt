package me.collumy.colib.scheduler

import java.util.concurrent.TimeUnit

interface ScheduledTask {
    val id: Long
    val isCancelled: Boolean
    fun cancel()
}

data class TimeAmount(val amount: Long, val unit: TimeUnit) {
    val toMillis: Long get() = unit.toMillis(amount)
    val toTicks: Long get() = toMillis / 50
}

// Расширения для удобного DSL (500.ms, 2.seconds, 10.ticks)
val Int.ms get() = TimeAmount(this.toLong(), TimeUnit.MILLISECONDS)
val Int.seconds get() = TimeAmount(this.toLong(), TimeUnit.SECONDS)
val Int.ticks get() = TimeAmount(this.toLong() * 50, TimeUnit.MILLISECONDS)