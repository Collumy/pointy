package me.collumy.pointy.bitshift.codec

import me.collumy.pointy.bitshift.api.BitShiftProtocol

class BitShiftBuffer {

    private val bits = ArrayList<Int>()

    fun feed(symbol: BitShiftSymbol) {
        val value = symbol.value
        val count = BitShiftProtocol.bitsPerPacket

        for (i in 0 until count) {
            val bit = (value ushr i) and 1
            bits.add(bit)
        }
    }

    fun size(): Int = bits.size

    fun toByteArray(limitBits: Int): ByteArray {
        val out = ArrayList<Byte>()
        var current = 0
        var filled = 0
        val total = minOf(limitBits, bits.size)

        for (i in 0 until total) {
            val bit = bits[i]
            current = current or (bit shl filled)
            if (++filled == 8) {
                out.add(current.toByte())
                current = 0
                filled = 0
            }
        }

        if (filled > 0) {
            out.add(current.toByte())
        }

        return out.toByteArray()
    }
}