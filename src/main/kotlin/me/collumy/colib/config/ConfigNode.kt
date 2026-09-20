package me.collumy.colib.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ConfigNode {
    internal abstract fun getJsonObject(): JsonObject
    internal abstract fun save()

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val defaultInitializers = mutableListOf<() -> Boolean>()
    internal fun registerInitializer(initializer: () -> Boolean) {
        defaultInitializers.add(initializer)
    }

    fun ensureDefaults(): Boolean {
        var modified = false
        for (initializer in defaultInitializers) {
            if (initializer()) modified = true
        }
        for (child in childNodes) {
            if (child.ensureDefaults()) modified = true
        }
        return modified
    }

    inline fun <reified T> value(default: T, key: String? = null) =
        delegate(default, key, object : TypeToken<T>() {}.type)

    fun boolean(key: String, default: Boolean) = value(default, key)
    fun string(key: String, default: String) = value(default, key)
    fun int(key: String, default: Int) = value(default, key)
    fun double(key: String, default: Double) = value(default, key)

    inline fun <reified T> list(key: String, default: List<T> = emptyList()) =
        delegate(default, key, object : TypeToken<List<T>>() {}.type)

    inline fun <reified K, reified V> map(key: String, default: Map<K, V> = emptyMap()) =
        delegate(default, key, object : TypeToken<Map<K, V>>() {}.type)

    inline fun <reified T> obj(key: String, default: T) =
        delegate(default, key, object : TypeToken<T>() {}.type)

    private val childNodes = mutableListOf<ConfigNode>()
    fun <S : ConfigSection> section(key: String, section: S): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, S>> {
        return PropertyDelegateProvider { _, property ->
            val sectionKey = key
            childNodes.add(section)
            section.bind(this, sectionKey)
            registerInitializer {
                val json = getJsonObject()
                if (!json.has(sectionKey) || !json.get(sectionKey).isJsonObject) {
                    json.add(sectionKey, JsonObject())
                    true
                } else false
            }
            ReadOnlyProperty { _, _ -> section }
        }
    }

    @PublishedApi
    internal fun <T> delegate(
        default: T,
        key: String?,
        type: Type
    ): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> {
        return PropertyDelegateProvider { _, property ->
            val propKey = key ?: property.name

            // 1. Автоматическая подстановка дефолта при load()
            registerInitializer {
                val json = getJsonObject()
                if (!json.has(propKey)) {
                    json.add(propKey, gson.toJsonTree(default, type))
                    true
                } else false
            }

            // 2. Логика getValue и setValue
            object : ReadWriteProperty<Any?, T> {
                override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    val json = getJsonObject()
                    val pKey = key ?: property.name
                    if (!json.has(pKey)) return default

                    return runCatching {
                        gson.fromJson(json.get(pKey), type) ?: default
                    }.getOrDefault(default)
                }

                override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    val pKey = key ?: property.name
                    getJsonObject().add(pKey, gson.toJsonTree(value, type))
                    save()
                }
            }
        }
    }
}