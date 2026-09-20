package me.collumy.pointy.bitshift

import net.minecraft.world.entity.player.Player

abstract class BitShiftReceiver(val packetId: Int) {

    init {
        require(packetId in 0..31) { "packetId must be 0..31" }
    }

    open fun onMessage(sender: Player, flags: Int, data: ByteArray) {}
}