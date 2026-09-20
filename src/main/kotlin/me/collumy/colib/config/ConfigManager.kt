package me.collumy.colib.config

import net.fabricmc.loader.api.FabricLoader
import java.io.File

object ConfigManager {

    private val baseDir: File = FabricLoader.getInstance().configDir.toFile()
    private val configs = mutableListOf<ConfigFile>()

    fun <T : ConfigFile> register(config: T) {
        val fileName = if (config.name.endsWith(".json")) config.name else "${config.name}.json"
        val targetFile = File(baseDir, fileName)

        config.load(targetFile)
        configs.add(config)
    }

    /**
     * Принудительная перезагрузка всех зарегистрированных конфигов
     */
    fun loadAll() {
        for (config in configs) {
            config.file?.let { config.load(it) }
        }
    }

    /**
     * Сохранить все текущие конфиги
     */
    fun saveAll() {
        configs.forEach { it.save() }
    }
}