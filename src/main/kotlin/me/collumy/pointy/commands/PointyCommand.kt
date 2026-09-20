package me.collumy.pointy.commands

import me.collumy.colib.command.ClientCommand
import me.collumy.colib.utils.PlayerAction
import me.collumy.pointy.PointyConfig
import me.collumy.pointy.core.Locale
import me.collumy.pointy.core.Locale.tr
import me.collumy.pointy.core.PointAnimations

object PointyCommand {

    val cmd = ClientCommand("pointy") {
        subcommand(PointyBlockCommand.cmd)
        subcommand(PointyEntityCommand.cmd)
        subcommand(SoundCommand.cmd)

        "send-color" {
            "on" { executes {
                PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.send_color".tr(Locale.on))
                PointyConfig.sendColor = true
            } }

            "off" { executes {
                PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.send_color".tr(Locale.off))
                PointyConfig.sendColor = false
            } }
        }

        "pointed-by-you" {
            branch {
                val new by everything("you")

                executes {
                    PlayerAction.sendMessage(Locale.prefix + "pointy.cmd.by_you".tr(Locale.yellow + new))
                    PointyConfig.pointedByYou = new
                }
            }
        }
    }
}