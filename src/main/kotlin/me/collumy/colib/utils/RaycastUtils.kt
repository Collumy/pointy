package me.collumy.colib.utils

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

sealed class SightResult {
    data class Block(val hit: BlockHitResult) : SightResult()
    data class EntityHit(val entity: Entity, val distance: Double) : SightResult()
}

object RaycastUtils {

    /**
     * Единый луч: проверяет и блоки, и сущности одновременно, возвращает то, что ближе.
     * Сущность за стеной никогда не будет возвращена — блок на пути её всегда перекрывает.
     */
    fun trace(maxDistance: Double = 128.0): SightResult? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null
        val level = client.level ?: return null

        val eyePos = player.eyePosition
        val look = player.lookAngle
        val endPos = eyePos.add(look.scale(maxDistance))

        // 1. Сначала находим ближайший БЛОК на пути — он определяет максимальную дистанцию,
        //    дальше которой сущности нас не интересуют (они физически загорожены)
        val blockHit = level.clip(ClipContext(
            eyePos, endPos,
            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
        )).takeIf { it.type == HitResult.Type.BLOCK }

        val blockDistance = blockHit?.location?.let { eyePos.distanceTo(it) } ?: maxDistance

        // 2. Ищем ближайшую сущность, но НЕ дальше, чем найденный блок — иначе это пинг сквозь стену
        var closestEntity: Entity? = null
        var closestEntityDistance = blockDistance

        for (entity in level.entitiesForRendering()) {
            if (entity == player) continue
            if (!entity.isPickable) continue

            val hitVec = clipAABB(entity.boundingBox.inflate(0.3), eyePos, endPos) ?: continue
            val distance = eyePos.distanceTo(hitVec)

            if (distance < closestEntityDistance) {
                closestEntity = entity
                closestEntityDistance = distance
            }
        }

        // 3. Сравниваем — что ближе, то и возвращаем
        return when {
            closestEntity != null -> SightResult.EntityHit(closestEntity, closestEntityDistance)
            blockHit != null -> SightResult.Block(blockHit)
            else -> null
        }
    }

    private fun clipAABB(box: AABB, start: Vec3, end: Vec3): Vec3? {
        return box.clip(start, end).orElse(null)
    }
}