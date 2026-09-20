package me.collumy.pointy

import me.collumy.colib.command.ClientCommandManager
import me.collumy.colib.config.ConfigManager
import me.collumy.colib.keybind.Button
import me.collumy.colib.keybind.KeybindManager
import me.collumy.pointy.bitshift.BitShiftRegistry
import me.collumy.pointy.bitshift.BitShiftSender
import me.collumy.pointy.commands.PointyCommand
import me.collumy.pointy.commands.SoundCommand
import me.collumy.pointy.point.PointNetwork
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object Pointy : ClientModInitializer {
	private val LOGGER = LoggerFactory.getLogger("Pointy")
	fun log(text: String) { LOGGER.info(text) }

	override fun onInitializeClient() {
		log("Я создался")
		BitShiftRegistry.register(PointNetwork)
		ClientCommandManager.register(PointyCommand.cmd)
		ConfigManager.register(PointyConfig)

		KeybindManager.register("pointy.point", Button.J) {
			category = "pointy.point"

			onPress {
				PointNetwork.point()
			}
		}
	}
}
