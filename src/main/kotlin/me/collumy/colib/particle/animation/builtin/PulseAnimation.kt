package me.collumy.colib.particle.animation.builtin

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

class PulseAnimation(
    private val particleType: ParticleOptions,
    private val intervalTicks: Int,
    private val burstCount: Int = 20,
    private val maxRadius: Double = 2.0,
    private val cycles: Int? = null, // null = бесконечно
    private val segments: Int = 24
) : ParticleAnimation {

    override val durationTicks: Int? = cycles?.let { it * intervalTicks }

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val phase = elapsedTicks % intervalTicks
        val progress = phase.toDouble() / intervalTicks // 0.0 → 1.0 внутри цикла

        // спавним только в начале каждого цикла, но радиус кольца растёт по progress
        // чтобы визуально это выглядело как расширяющаяся волна, спавним каждый тик,
        // но с уменьшающимся count ближе к концу цикла (fade-out эффекта расширения)
        if (progress > 0.8) return emptyList() // последние 20% цикла — пауза перед следующим пульсом

        val currentRadius = maxRadius * (progress / 0.8)
        val fadeFactor = 1.0 - (progress / 0.8) // ближе к границе — меньше частиц
        val count = (burstCount * fadeFactor).toInt().coerceAtLeast(1)

        return listOf(
            ParticleEffect(
                type = particleType,
                shape = ParticleShape.Circle(radius = currentRadius),
                count = count
            )
        )
    }
}