package me.collumy.pointy.bitshift.utils

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.floor
import kotlin.math.log2

object BitShiftOffsets {

    /** Все валидные смещения (611 штук) */
    val offsets: Array<BlockPos> = buildOffsets()

    /** Количество бит, кодируемых смещением */
    val bitsOffset: Int = floor(log2(offsets.size.toDouble())).toInt()

    /** Быстрый поиск смещения → индекс */
    private val indexMap: HashMap<BlockPos, Int> =
        offsets.withIndex().associateTo(HashMap()) { (i, pos) -> pos to i }

    /** Получить индекс смещения или null */
    fun indexOf(pos: BlockPos): Int? = indexMap[pos]

    private fun buildOffsets(): Array<BlockPos> {
        val corners = buildList {
            for (x in 0..1) for (y in 0..1) for (z in 0..1)
                add(Vec3(x.toDouble(), y.toDouble(), z.toDouble()))
        }

        val reach = intArrayOf(0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6)
        val result = ArrayList<BlockPos>()

        for (dx in reach) for (dy in reach) {
            axis@ for (dz in reach) {
                val offset = BlockPos(dx, dy, dz)
                val center = Vec3.atCenterOf(offset)

                for (corner in corners) {
                    if (corner.distanceToSqr(center) > 36.0) continue@axis
                }

                result.add(offset)
            }
        }

        return result.toTypedArray()
    }
}