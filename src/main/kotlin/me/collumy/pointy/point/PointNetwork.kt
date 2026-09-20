package me.collumy.pointy.point

import me.collumy.colib.utils.RaycastUtils
import me.collumy.colib.utils.SightResult
import me.collumy.pointy.Pointy.log
import me.collumy.pointy.PointyConfig
import me.collumy.pointy.bitshift.BitShiftReceiver
import me.collumy.pointy.bitshift.BitShiftSender
import net.jpountz.lz4.LZ4FrameOutputStream
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import java.nio.ByteBuffer

object PointNetwork : BitShiftReceiver(1) {

    private const val  BLOCK_FLAG = 0
    private const val ENTITY_FLAG = 1

    private var lastPointAt = 0L
    private const val POINT_COOLDOWN = 2500L

    fun point() {
        val now = System.currentTimeMillis()
        if (now - lastPointAt < POINT_COOLDOWN) return
        lastPointAt = now

        val player = Minecraft.getInstance().player ?: return
        if (player.isSpectator) return

        when (val result = RaycastUtils.trace(20.0)) {
            is SightResult.Block -> point(result.hit.blockPos)
            is SightResult.EntityHit -> point(result.entity)
            else -> return
        }
    }

    private fun point(pos: BlockPos) {
        val buf = ByteBuffer.allocate(14 + if (PointyConfig.sendColor) 4 else 0)
        buf.putInt(pos.x)
        buf.putShort(pos.y.toShort())
        buf.putInt(pos.z)
        if (PointyConfig.sendColor) buf.putInt(PointyConfig.block.color)

        PointRenderer.render(pos, PointyConfig.pointedByYou)
        BitShiftSender.send(this, BLOCK_FLAG, buf.array())
    }

    private fun point(entity: Entity) {
        val buf = ByteBuffer.allocate(4 + if (PointyConfig.sendColor) 4 else 0)
        buf.putInt(entity.id)
        if (PointyConfig.sendColor) buf.putInt(PointyConfig.entity.color)

        PointRenderer.render(entity.id, PointyConfig.pointedByYou)
        BitShiftSender.send(this, ENTITY_FLAG, buf.array())
    }

    override fun onMessage(sender: Player, flags: Int, data: ByteArray) {
        when (flags) {
            BLOCK_FLAG -> {
                val buf = ByteBuffer.wrap(data)
                val x = buf.int
                val y = buf.short.toInt()
                val z = buf.int
                val color = if (buf.remaining() >= 4) buf.int else null

                val pos = BlockPos(x, y, z)
                PointRenderer.render(pos, sender.gameProfile.name, color)
                log("${sender.gameProfile.name} упомянул блок $pos")
            }
            ENTITY_FLAG -> {
                val buf = ByteBuffer.wrap(data)
                val id = buf.int
                val color = if (buf.remaining() >= 4) buf.int else null

                val level = Minecraft.getInstance().level ?: return
                val entity = level.getEntity(id)
                if (entity == null) {
                    log("Сущность $id не найдена")
                    return
                }

                PointRenderer.render(entity.id, sender.gameProfile.name, color)
                log("${sender.gameProfile.name} упомянул сущность $entity")
            }
        }
    }

}
