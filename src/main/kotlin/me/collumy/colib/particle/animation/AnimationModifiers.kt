package me.collumy.colib.particle.animation

import me.collumy.colib.particle.ParticleEffect
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

/** Ускоряет/замедляет время внутри анимации */
class SpeedModifier(
    private val base: ParticleAnimation,
    private val multiplier: Double
) : ParticleAnimation {
    override val durationTicks: Int? = base.durationTicks?.let { (it / multiplier).toInt() }

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> =
        base.tick((elapsedTicks * multiplier).toInt(), origin)
}

/** Пропускает часть тиков случайно — мерцающий эффект */
class FlickerModifier(
    private val base: ParticleAnimation,
    private val chance: Float = 0.3f
) : ParticleAnimation {
    override val durationTicks = base.durationTicks

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        if (Random.nextFloat() < chance) return emptyList()
        return base.tick(elapsedTicks, origin)
    }
}

/** Плавно уменьшает count/интенсивность к концу анимации */
class FadeModifier(
    private val base: ParticleAnimation,
    private val fadeInTicks: Int = 0,
    private val fadeOutTicks: Int = 0
) : ParticleAnimation {
    override val durationTicks = base.durationTicks

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val total = durationTicks ?: return base.tick(elapsedTicks, origin)
        val factor = when {
            elapsedTicks < fadeInTicks -> elapsedTicks.toFloat() / fadeInTicks
            elapsedTicks > total - fadeOutTicks -> (total - elapsedTicks).toFloat() / fadeOutTicks
            else -> 1f
        }.coerceIn(0f, 1f)

        return base.tick(elapsedTicks, origin).map { it.copy(count = (it.count * factor).toInt().coerceAtLeast(0)) }
    }
}