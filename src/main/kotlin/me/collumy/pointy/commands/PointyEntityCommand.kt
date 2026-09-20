package me.collumy.pointy.commands

import me.collumy.colib.command.ClientCommand
import me.collumy.colib.utils.PlayerAction
import me.collumy.pointy.PointyConfig
import me.collumy.pointy.core.Locale
import me.collumy.pointy.core.Locale.tr
import me.collumy.pointy.core.PointAnimations

object PointyEntityCommand {

    val cmd = ClientCommand("entity") {
        "text" {
            branch {
                val text by everything("text").suggest("Упомянут {0}")

                executes {
                    if ("{0}" !in text) {
                        PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.text_must_contain".tr())
                    } else {
                        PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.new_text".tr(Locale.yellow + text))
                        PointyConfig.entity.text = text
                    }
                }
            }
        }

        "color" {
            branch {
                val colorArg by everything("color").suggest(
                    "16764108", "14935069", "15263977", "16775940",
                    "15921941", "16767634", "14145480", "13619100",
                    "#FFC1CC", "#E3F2FD", "#E8F5E9", "#FFF9C4"
                )

                executes {
                    val color = if (colorArg.startsWith("#")) {
                        colorArg.removePrefix("#").toInt(16)
                    } else colorArg.toInt()

                    val colorText = "#%06X".format(color and 0xFFFFFF)
                    PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.new_color".tr("&$colorText$colorText"))
                    PointyConfig.entity.color = color
                }
            }
        }

        "animation" {
            branch {
                val id by word("animation").suggest(PointAnimations.animations)

                executes {
                    if (!PointAnimations.have(id)) {
                        PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.animation_not_found".tr(id))
                    } else {
                        PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.new_animation".tr(Locale.yellow + id))
                        PointyConfig.entity.animation = id
                    }
                }
            }
        }
    }

}