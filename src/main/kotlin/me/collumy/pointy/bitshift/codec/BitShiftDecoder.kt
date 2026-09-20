package me.collumy.pointy.bitshift.codec

import me.collumy.pointy.bitshift.api.BitShiftProtocol

object BitShiftDecoder {

    fun decode(buffer: BitShiftBuffer, lengthBits: Int): ByteArray {
        return buffer.toByteArray(lengthBits)
    }

    fun encode(payload: ByteArray): List<BitShiftSymbol> {
        val symbols = ArrayList<BitShiftSymbol>()
        var bitPos = 0
        var currentSymbol = 0
        val bitsPerPacket = BitShiftProtocol.bitsPerPacket

        fun flushSymbol() {
            symbols.add(BitShiftSymbol.encode(currentSymbol))
            currentSymbol = 0
            bitPos = 0
        }

        for (byte in payload) {
            for (i in 0 until 8) {
                val bit = (byte.toInt() ushr i) and 1
                currentSymbol = currentSymbol or (bit shl bitPos)

                if (++bitPos == bitsPerPacket) {
                    flushSymbol()
                }
            }
        }

        if (bitPos > 0) flushSymbol()

        return symbols
    }

}