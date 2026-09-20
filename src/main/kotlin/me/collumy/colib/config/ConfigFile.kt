package me.collumy.colib.config

import com.google.gson.JsonObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

abstract class ConfigFile(val name: String) : ConfigNode() {
    internal var file: File? = null
    internal var rootJson = JsonObject()

    override fun getJsonObject(): JsonObject = rootJson

    private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "Config-Save-Scheduler").apply { isDaemon = true }
    }
    private var pendingSaveTask: ScheduledFuture<*>? = null

    fun load(file: File) {
        this.file = file
        rootJson = if (file.exists()) {
            runCatching {
                gson.fromJson(file.readText(), JsonObject::class.java) ?: JsonObject()
            }.getOrDefault(JsonObject())
        } else {
            JsonObject()
        }

        if (ensureDefaults()) saveDirectly()
    }

    override fun save() {
        synchronized(this) {
            pendingSaveTask?.cancel(false)
            pendingSaveTask = scheduler.schedule({
                saveDirectly()
            }, 5, TimeUnit.SECONDS)
        }
    }

    /**
     * Прямая запись на диск без задержек (вызывается авто-сохранением по таймеру или вручную).
     */
    fun saveDirectly() {
        synchronized(this) {
            pendingSaveTask?.cancel(false)
            pendingSaveTask = null

            val targetFile = file ?: return
            runCatching {
                targetFile.parentFile?.mkdirs()
                targetFile.writeText(gson.toJson(rootJson))
            }.onFailure { it.printStackTrace() }
        }
    }
}