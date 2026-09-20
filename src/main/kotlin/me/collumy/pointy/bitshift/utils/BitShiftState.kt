package me.collumy.pointy.bitshift.utils

enum class BitShiftState {
    WAIT_MAGIC_0,
    WAIT_MAGIC_1,
    READ_HEADER,
    READ_LENGTH,
    READ_PAYLOAD
}