package me.collumy.pointy.point

import me.collumy.colib.particle.ParticleManager
import me.collumy.colib.particle.animation.ParticleAnimation
import me.collumy.colib.particle.dsl.Particle.dust
import me.collumy.colib.particle.dsl.animation
import me.collumy.colib.scheduler.Scheduler
import me.collumy.colib.scheduler.ms
import me.collumy.colib.utils.EntityAction
import me.collumy.colib.utils.TextUtils.toText
import me.collumy.pointy.Pointy.log
import me.collumy.pointy.PointyConfig
import me.collumy.pointy.core.PointAnimations
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object PointRenderer {

    data class ActivePoint(
        val pos: BlockPos? = null,
        val targetEntityId: Int? = null,
        val senderName: String,
        var createdAt: Long = System.currentTimeMillis(),

        val color: Int? = null,
        var display: Display.TextDisplay? = null,
        var isStarted: Boolean = false
    )

    private val activePoints = ArrayList<ActivePoint>()
    private const val CHECK_INTERVAL = 100
    private const val POINT_LASTS = 5000

    fun render(pos: BlockPos, senderName: String, color: Int? = null) {
        log("блок $pos упомянут $senderName")
        activePoints.add(ActivePoint(pos, senderName = senderName, color = color))
    }

    fun render(entityId: Int, senderName: String, color: Int? = null) {
        log("сущность $entityId упомянута $senderName")
        activePoints.add(ActivePoint(targetEntityId = entityId, senderName = senderName, color = color))
    }


    init {
        Scheduler.repeat(CHECK_INTERVAL.ms) {
            val level = Minecraft.getInstance().level ?: return@repeat
            val iterator = activePoints.iterator()

            while (iterator.hasNext()) {
                val point = iterator.next()

                // удаляем через 5 секунд
                if (System.currentTimeMillis() - point.createdAt > POINT_LASTS) {
                    point.display?.remove(Entity.RemovalReason.KILLED)
                    log("Сущность ${point.display} была удалена")
                    iterator.remove()
                    continue
                }

                when {
                    point.pos != null -> renderBlockPoint(level, point)
                    point.targetEntityId != null -> renderEntityPoint(level, point)
                }
            }
        }

    }

    private fun renderBlockPoint(level: ClientLevel, point: ActivePoint) {
        val pos = point.pos ?: return

        if (point.display == null) {
            val configText = PointyConfig.block.text
            val name = configText.replace("{0}", point.senderName).toText()
            point.display = EntityAction.spawnDisplay(level, pos, name)
        }

        point.display!!.setPos(pos.x + 0.5, pos.y + 1.2, pos.z + 0.5)

        if (!point.isStarted) {
            point.isStarted = true

            if (PointyConfig.sound.isEnabled) {
                playSound(level, Vec3(pos.x + 0.5, pos.y + 1.2, pos.z + 0.5))
            }

            ParticleManager.play(
                PointAnimations[point.color, false],
                origin = Vec3(pos.x + 0.5, pos.y + 1.2, pos.z + 0.5),
                POINT_LASTS / 50
            )
        }
    }

    private fun renderEntityPoint(level: ClientLevel, point: ActivePoint) {
        val entity = level.getEntity(point.targetEntityId!!) ?: return

        if (point.display == null) {
            val configText = PointyConfig.entity.text
            val name = configText.replace("{0}", point.senderName).toText()
            point.display = EntityAction.spawnDisplay(level, entity.blockPosition(), name)
        }

        point.display!!.setPos(entity.x, entity.y + entity.bbHeight + 0.5, entity.z)

        if (!point.isStarted) {
            point.isStarted = true

            if (PointyConfig.sound.isEnabled) {
                playSound(level, Vec3(entity.x, entity.y, entity.z))
            }

            ParticleManager.play(
                PointAnimations[point.color, true],
                origin = { Vec3(entity.x, entity.y + 0.2, entity.z) },
                POINT_LASTS / 50
            )
        }
    }

    private fun playSound(level: ClientLevel, pos: Vec3) {
        val id = Identifier.tryParse(PointyConfig.sound.sound) ?: return
        val holder = BuiltInRegistries.SOUND_EVENT.get(id)

        val soundEvent = holder.orElse(null)?.value() ?: return
        level.playLocalSound(
            pos.x, pos.y, pos.z,
            soundEvent, SoundSource.PLAYERS,
            1.0f, 1.0f, false
        )
    }
}