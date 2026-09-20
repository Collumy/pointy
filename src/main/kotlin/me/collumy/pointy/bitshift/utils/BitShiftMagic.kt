package me.collumy.pointy.bitshift.utils

import me.collumy.pointy.bitshift.api.BitShiftProtocol

object BitShiftMagic {

    val magic: IntArray = intArrayOf(
        BitShiftProtocol.maxSymbol - 1,
        BitShiftProtocol.maxSymbol - 2
    )
}