package me.collumy.pointy.mixin

import me.collumy.pointy.bitshift.api.BitShiftReader
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientPacketListener::class)
abstract class BitShiftMixin {

    @Unique
    private val readers = HashMap<Int, BitShiftReader>()

    @Unique
    private fun readerFor(id: Int): BitShiftReader {
        val level = Minecraft.getInstance().level ?: error("No level")
        val player = level.getEntity(id) as? Player ?: error("No player for id=$id")
        return readers.getOrPut(id) { BitShiftReader(player) }
    }

    @Inject(method = ["handleBlockDestruction"], at = [At("RETURN")])
    private fun onBlockDestruction(packet: ClientboundBlockDestructionPacket, info: CallbackInfo) {
        if (packet.progress != 255) return

        val reader = readers[packet.id] ?: run {
            val level = Minecraft.getInstance().level ?: return
            val player = level.getEntity(packet.id) as? Player ?: return
            BitShiftReader(player).also { readers[packet.id] = it }
        }

        reader.feed(packet.pos)
    }

    @Inject(method = ["handleRespawn", "handleLogin"], at = [At("RETURN")])
    private fun onStateReset(info: CallbackInfo) { readers.clear() }
}