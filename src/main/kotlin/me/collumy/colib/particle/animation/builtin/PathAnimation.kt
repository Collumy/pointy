package me.collumy.colib.particle.animation.builtin

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

/** Функция, задающая точку на пути от 0.0 (начало) до 1.0 (конец) */
fun interface ParticlePath {
    fun pointAt(t: Double): Vec3

    companion object {
        fun straight(from: Vec3, to: Vec3) = ParticlePath { t -> from.lerp(to, t) }

        fun bezier(from: Vec3, control: Vec3, to: Vec3) = ParticlePath { t ->
            val a = from.lerp(control, t)
            val b = control.lerp(to, t)
            a.lerp(b, t)
        }

        fun arc(from: Vec3, to: Vec3, height: Double) = ParticlePath { t ->
            val base = from.lerp(to, t)
            val arcHeight = height * 4 * t * (1 - t) // парабола, максимум в середине пути
            base.add(0.0, arcHeight, 0.0)
        }
    }
}

/**
 * Частицы движутся вдоль заданного ParticlePath относительно origin (который трактуется
 * как точка отсчёта "from" пути — path задаётся в локальных координатах от origin).
 *
 * durationTicksTotal — за сколько тиков одна частица проходит путь целиком
 * particlesPerTick — сколько новых частиц запускать в путь каждый тик (для "потока")
 */
class PathAnimation(
    private val particleType: ParticleOptions,
    private val path: ParticlePath,
    private val durationTicksTotal: Int,
    private val particlesPerTick: Int = 1,
    private val loop: Boolean = false,
    override val durationTicks: Int? = null // null или явный лимит на весь эффект (для нескольких запусков потока)
) : ParticleAnimation {

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val effects = mutableListOf<ParticleEffect>()

        repeat(particlesPerTick) { i ->
            // каждая "порция" частиц стартует со сдвигом по времени, чтобы поток был непрерывным
            val startOffset = i.toDouble() / particlesPerTick * durationTicksTotal
            var localTime = elapsedTicks + startOffset

            if (loop) {
                localTime %= durationTicksTotal
            } else if (localTime > durationTicksTotal) {
                return@repeat // эта частица уже долетела и не перезапускается
            }

            val t = (localTime / durationTicksTotal).coerceIn(0.0, 1.0)
            val point = path.pointAt(t)

            effects.add(ParticleEffect(particleType, ParticleShape.Point(point), count = 1))
        }

        return effects
    }
}