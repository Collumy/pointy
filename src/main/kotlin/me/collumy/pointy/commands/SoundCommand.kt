package me.collumy.pointy.commands

import me.collumy.colib.command.ClientCommand
import me.collumy.pointy.PointyConfig
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

object SoundCommand {

    val cmd = ClientCommand("sound") {
        "on" {
            executes {
                PointyConfig.sound.isEnabled = true
            }
        }

        "off" {
            executes {
                PointyConfig.sound.isEnabled = false
            }
        }

        "set" {
            branch {
                val id by everything("id").suggest(
                    "minecraft:entity.experience_orb.pickup",
                    "minecraft:entity.player.levelup",
                    "minecraft:block.note_block.harp",
                    "minecraft:block.amethyst_block.chime",
                    "minecraft:ui.button.click"
                )

                executes {
                    PointyConfig.sound.sound = id.replace(" ", "")
                }
            }
        }
    }

    private fun validate(idString: String): SoundEvent? {
        val id = Identifier.tryParse(idString) ?: return null
        val holder = BuiltInRegistries.SOUND_EVENT.get(id)

        val ref = holder.orElse(null) ?: return null
        return ref.value()
    }
}