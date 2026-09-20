package me.collumy.pointy.bitshift.api

import me.collumy.pointy.bitshift.utils.BitShiftMagic
import me.collumy.pointy.bitshift.utils.BitShiftOffsets
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player

object BitShiftProtocol {

    /** Таблица смещений (611 элементов) */
    val offsets: Array<BlockPos> = BitShiftOffsets.offsets

    /** Количество бит, кодируемых смещением */
    val bitsOffset: Int = BitShiftOffsets.bitsOffset

    /** Сколько бит даёт один пакет */
    val bitsPerPacket: Int = bitsOffset

    /** Максимальный символ (для magic) */
    val maxSymbol: Int = 1 shl bitsPerPacket

    /** Magic-последовательность (2 пакета) */
    val magic: IntArray = BitShiftMagic.magic

    /** Преобразование позиции глаз игрока в BlockPos */
    fun eyeBlockPos(player: Player): BlockPos {
        val eye = player.eyePosition
        return BlockPos(Mth.floor(eye.x), Mth.floor(eye.y), Mth.floor(eye.z))
    }

    /** Кодирование symbol → (offsetIndex, directionBits) */
    fun decodeSymbol(eye: BlockPos, pos: BlockPos): Int {
        val offset = pos.subtract(eye)
        return BitShiftOffsets.indexOf(offset) ?: -1
    }
}
