package me.collumy.colib.utils

import me.collumy.colib.utils.TextUtils.toText
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component

object PlayerAction {

    private val client: Minecraft get() = Minecraft.getInstance()
    private val player: LocalPlayer? get() = client.player

    fun sendMessageAsPlayer(message: String) {
        val p = player ?: return

        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        if (trimmed.startsWith("/")) {
            p.connection.sendCommand(trimmed.removePrefix("/"))
        } else {
            p.connection.sendChat(trimmed)
        }
    }

    fun sendMessage(message: String) {
        sendMessage(message.toText())
    }

    fun sendMessage(component: Component) {
        val player = Minecraft.getInstance().player ?: return
        player.displayClientMessage(component, false)
    }
}