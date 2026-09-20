package me.collumy.pointy.core

import net.minecraft.client.resources.language.I18n

object Locale {

    operator fun invoke(key: String, vararg args: Any): String {
        var text = I18n.get(key)

        args.forEachIndexed { index, arg ->
            text = text.replace("{$index}", arg.toString())
        }

        return text
    }

    fun String.tr(vararg args: Any) = invoke(this, *args)

    val prefix: String
        get() = "&#61CCB6${"pointy".tr()} &#7a8085» &f"

    val yellow = "&#EFF786"

    val on: String
        get() = "<#bdecb6>${"pointy.on".tr()}</>"
    val off: String
        get() = "<#e39287>${"pointy.off".tr()}</>"
}