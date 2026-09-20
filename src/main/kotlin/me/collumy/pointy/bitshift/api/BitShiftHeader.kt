package me.collumy.pointy.bitshift.api

data class BitShiftHeader(
    val packetId: Int,
    val flags: Int
) {

    init {
        require(packetId in 0..31) { "packetId must be 0..31" }
        require(flags in 0..63) { "flags must be 0..63" }
    }

    fun encode(): Int {
        return (packetId and 0x3F) or ((flags and 0x1F) shl 6)
    }

    companion object {
        fun decode(value: Int): BitShiftHeader {
            val packetId = value and 0x3F
            val flags = (value ushr 6) and 0x1F
            return BitShiftHeader(packetId, flags)
        }
    }
}