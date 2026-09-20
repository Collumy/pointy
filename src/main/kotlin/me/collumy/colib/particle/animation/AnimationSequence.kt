package me.collumy.colib.particle.animation

import me.collumy.colib.particle.ParticleEffect
import net.minecraft.world.phys.Vec3

/** Анимации идут одна за другой */
class SequentialAnimation(private val steps: List<ParticleAnimation>) : ParticleAnimation {
    override val durationTicks: Int? =
        if (steps.any { it.durationTicks == null }) null
        else steps.sumOf { it.durationTicks!! }

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        var remaining = elapsedTicks
        for (step in steps) {
            val stepDuration = step.durationTicks ?: return step.tick(remaining, origin)
            if (remaining < stepDuration) return step.tick(remaining, origin)
            remaining -= stepDuration
        }
        return emptyList()
    }
}

/** Анимации идут одновременно, партиклы суммируются */
class ParallelAnimation(private val branches: List<ParticleAnimation>) : ParticleAnimation {
    override val durationTicks: Int? =
        if (branches.any { it.durationTicks == null }) null
        else branches.maxOf { it.durationTicks!! }

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> =
        branches.flatMap { it.tick(elapsedTicks, origin) }
}

/** Анимация повторяется N раз или бесконечно */
class LoopAnimation(
    private val base: ParticleAnimation,
    private val times: Int? = null,          // null = бесконечно
    private val cooldownTicks: Int = 0       // пауза между циклами
) : ParticleAnimation {
    private val cycleLength = base.durationTicks ?: error("Cannot loop an animation with no fixed duration")
    private val fullCycle = cycleLength + cooldownTicks

    override val durationTicks: Int? = times?.let { fullCycle * it }

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val cyclePos = elapsedTicks % fullCycle
        return if (cyclePos < cycleLength) base.tick(cyclePos, origin) else emptyList()
    }
}
