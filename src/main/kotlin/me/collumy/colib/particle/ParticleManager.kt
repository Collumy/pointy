package me.collumy.colib.particle

import me.collumy.colib.particle.animation.ParticleAnimation
import me.collumy.colib.scheduler.Scheduler
import me.collumy.colib.scheduler.ticks
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3

object ParticleManager {

    private class RunningAnimation(
        val animation: ParticleAnimation,
        val origin: () -> Vec3,
        val duration: Int?,
        val condition: (() -> Boolean)?
    ) {
        var elapsed = 0
    }


    private val running = mutableListOf<RunningAnimation>()

    fun play(animation: ParticleAnimation, origin: () -> Vec3,
             duration: Int? = null, until: (() -> Boolean)? = null) {
        running.add(RunningAnimation(animation, origin, duration, until))
    }

    fun play(animation: ParticleAnimation, origin: Vec3,
             duration: Int? = null, until: (() -> Boolean)? = null) {
        play(animation, { origin }, duration, until)
    }



    /** Вызывать раз в клиентский тик */
    init {
        Scheduler.repeat(1.ticks) {
            val level = Minecraft.getInstance().level ?: return@repeat
            val iterator = running.iterator()

            while (iterator.hasNext()) {
                val run = iterator.next()
                val originPos = run.origin()

                run.animation.tick(run.elapsed, originPos).forEach { effect ->
                    effect.shape.points(originPos, effect.count).forEach { point ->
                        level.addParticle(
                            effect.type,
                            point.x,
                            point.y,
                            point.z,
                            effect.velocity.x * effect.speed,
                            effect.velocity.y * effect.speed,
                            effect.velocity.z * effect.speed
                        )
                    }
                }

                run.elapsed++

                val finishedByDuration = run.duration != null && run.elapsed >= run.duration
                val finishedByCondition = run.condition?.invoke() == true
                val finishedByAnimation = run.animation.isFinished(run.elapsed)

                if (finishedByDuration || finishedByCondition || finishedByAnimation) iterator.remove()
            }
        }
    }
}