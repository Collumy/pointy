package me.collumy.pointy

import me.collumy.colib.config.ConfigFile
import me.collumy.colib.config.ConfigSection

object PointyConfig : ConfigFile("pointy") {

    class SoundSection : ConfigSection() {
        var isEnabled by boolean("enabled", true)
        var sound by string("id", "minecraft:entity.experience_orb.pickup")
    }

    class PointerSection : ConfigSection() {
        var text by string("text", "&eУпомянут {0}")
        var animation by string("animation", "orbit")
        var color by int("color", 0xffff00)
    }

    val block by section("block", PointerSection())
    val entity by section("entity", PointerSection())

    var sendColor by boolean("send_color", true)
    var pointedByYou by string("pointed-by-you", "вами")
    val sound by section("sound", SoundSection())
}