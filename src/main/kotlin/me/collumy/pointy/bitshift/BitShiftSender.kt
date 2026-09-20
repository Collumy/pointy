package me.collumy.pointy.bitshift

import me.collumy.pointy.bitshift.api.BitShiftHeader
import me.collumy.pointy.bitshift.api.BitShiftProtocol
import me.collumy.pointy.bitshift.codec.BitShiftDecoder
import me.collumy.pointy.bitshift.codec.BitShiftSymbol
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket

object BitShiftSender {

    fun send(id: BitShiftReceiver, flags: Int, payload: ByteArray) {
        val client = Minecraft.getInstance()
        if (client.isSingleplayer) return
        val symbols = BitShiftDecoder.encode(payload)

        sendSymbol(client, BitShiftSymbol(BitShiftProtocol.magic[0]))
        sendSymbol(client, BitShiftSymbol(BitShiftProtocol.magic[1]))

        // header
        val header = BitShiftHeader(id.packetId, flags)
        sendSymbol(client, BitShiftSymbol(header.encode()))

        // length
        sendSymbol(client, BitShiftSymbol(payload.size * 8))

        // payload
        symbols.forEach { sendSymbol(client, it) }
    }

    private fun sendSymbol(client: Minecraft, symbol: BitShiftSymbol) {
        val offset = symbol.offset()
        val eyePos = BitShiftProtocol.eyeBlockPos(client.player!!)

        client.connection!!.send(ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
            eyePos.offset(offset), Direction.DOWN
        ))
    }
}