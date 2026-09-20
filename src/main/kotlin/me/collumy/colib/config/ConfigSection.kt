package me.collumy.colib.config

import com.google.gson.JsonObject

open class ConfigSection : ConfigNode() {
    private lateinit var parent: ConfigNode
    private lateinit var sectionKey: String

    internal fun bind(parent: ConfigNode, sectionKey: String) {
        this.parent = parent
        this.sectionKey = sectionKey
    }

    override fun getJsonObject(): JsonObject {
        val parentObj = parent.getJsonObject()
        if (!parentObj.has(sectionKey) || !parentObj.get(sectionKey).isJsonObject) {
            parentObj.add(sectionKey, JsonObject())
        }
        return parentObj.getAsJsonObject(sectionKey)
    }

    override fun save() {
        parent.save()
    }
}