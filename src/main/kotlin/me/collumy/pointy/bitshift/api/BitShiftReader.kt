package me.collumy.pointy.bitshift.api

import me.collumy.pointy.Pointy.log
import me.collumy.pointy.bitshift.BitShiftRegistry
import me.collumy.pointy.bitshift.api.BitShiftHeader
import me.collumy.pointy.bitshift.api.BitShiftProtocol
import me.collumy.pointy.bitshift.codec.BitShiftBuffer
import me.collumy.pointy.bitshift.codec.BitShiftDecoder
import me.collumy.pointy.bitshift.codec.BitShiftSymbol
import me.collumy.pointy.bitshift.utils.BitShiftState
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player

class BitShiftReader(private val player: Player) {

    private var state = BitShiftState.WAIT_MAGIC_0
    private var lastPacketAt = -1L

    private var header: BitShiftHeader? = null
    private var lengthBits: Int? = null
    private var buffer: BitShiftBuffer? = null

    fun feed(pos: BlockPos) {
        val now = System.currentTimeMillis()
        if (lastPacketAt != -1L && now - lastPacketAt > 3000) reset()
        lastPacketAt = now

        val eye = BitShiftProtocol.eyeBlockPos(player)
        val symbolValue = BitShiftProtocol.decodeSymbol(eye, pos)
        if (symbolValue == -1) return

        val symbol = BitShiftSymbol(symbolValue)
        when (state) {
            BitShiftState.WAIT_MAGIC_0 -> {
                if (symbol.value == BitShiftProtocol.magic[0]) {
                    state = BitShiftState.WAIT_MAGIC_1
                }
            }

            BitShiftState.WAIT_MAGIC_1 -> {
                if (symbol.value == BitShiftProtocol.magic[1]) {
                    state = BitShiftState.READ_HEADER
                } else reset()
            }

            BitShiftState.READ_HEADER -> {
                header = BitShiftHeader.decode(symbol.value)
                state = BitShiftState.READ_LENGTH
            }

            BitShiftState.READ_LENGTH -> {
                lengthBits = symbol.value
                buffer = BitShiftBuffer()
                state = BitShiftState.READ_PAYLOAD
            }

            BitShiftState.READ_PAYLOAD -> {
                buffer!!.feed(symbol)

                if (buffer!!.size() >= lengthBits!!) {
                    val bytes = BitShiftDecoder.decode(buffer!!, lengthBits!!)
                    dispatch(header!!, bytes)

                    reset()
                }
            }
        }
    }

    private fun dispatch(header: BitShiftHeader, data: ByteArray) {
        val receiver = BitShiftRegistry.get(header.packetId) ?: return
        try { receiver.onMessage(player, header.flags, data) } catch (e: Exception) { }
    }


    private fun reset() {
        state = BitShiftState.WAIT_MAGIC_0
        header = null
        lengthBits = null
        buffer = null
    }
}