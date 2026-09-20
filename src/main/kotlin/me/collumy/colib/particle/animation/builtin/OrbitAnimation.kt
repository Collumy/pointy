package me.collumy.colib.particle.animation.builtin

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Одна или несколько частиц вращаются вокруг origin по кругу заданного радиуса.
 * ticksPerRevolution — сколько тиков занимает один полный оборот.
 */
class OrbitAnimation(
    private val particleType: ParticleOptions,
    private val radius: Double,
    private val ticksPerRevolution: Int = 40,
    private val axis: Vec3 = Vec3(0.0, 1.0, 0.0), // ось вращения (по умолчанию вертикальная)
    private val particlesPerTick: Int = 1,
    private val verticalOffset: Double = 0.0, // можно постепенно поднимать/опускать (спираль)
    override val durationTicks: Int? = null // по умолчанию бесконечно, пока не остановят
) : ParticleAnimation {

    // строим базис плоскости, перпендикулярной axis
    private val right = buildPlaneBasis(axis).first
    private val forward = buildPlaneBasis(axis).second

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val effects = mutableListOf<ParticleEffect>()

        repeat(particlesPerTick) { i ->
            // смещаем фазу для нескольких частиц по кругу равномерно
            val phaseOffset = i.toDouble() / particlesPerTick
            val progress = (elapsedTicks.toDouble() / ticksPerRevolution) + phaseOffset
            val angle = progress * Math.PI * 2

            val offset = right.scale(cos(angle) * radius).add(forward.scale(sin(angle) * radius))
            val point = offset.add(0.0, verticalOffset * elapsedTicks, 0.0)

            effects.add(ParticleEffect(particleType, ParticleShape.Point(point), count = 1))
        }

        return effects
    }

    private fun buildPlaneBasis(axis: Vec3): Pair<Vec3, Vec3> {
        val normalizedAxis = axis.normalize()
        // берём произвольный вектор, не параллельный axis, чтобы построить перпендикуляр
        val arbitrary = if (abs(normalizedAxis.y) < 0.99) Vec3(0.0, 1.0, 0.0) else Vec3(1.0, 0.0, 0.0)
        val right = normalizedAxis.cross(arbitrary).normalize()
        val forward = normalizedAxis.cross(right).normalize()
        return right to forward
    }
}