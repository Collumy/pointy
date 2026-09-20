package me.collumy.pointy.bitshift.codec

import me.collumy.pointy.bitshift.api.BitShiftProtocol
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

data class BitShiftSymbol(val value: Int) {

    /** Смещение как BlockPos */
    fun offset(): BlockPos = BitShiftProtocol.offsets[value]

    companion object {
        fun encode(offsetIndex: Int): BitShiftSymbol {
            return BitShiftSymbol(offsetIndex)
        }
    }
}