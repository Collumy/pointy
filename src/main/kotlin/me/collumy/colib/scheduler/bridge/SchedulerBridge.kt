package me.collumy.colib.scheduler.bridge

import me.collumy.colib.scheduler.Scheduler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object SchedulerBridge {

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            Scheduler.tickMainThread()
        }
    }
}