package me.collumy.colib.particle.animation.builtin

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

/**
 * Оставляет частицы позади движущегося origin — каждый тик спавнит частицу
 * в ТЕКУЩЕЙ точке, создавая видимость следа за счёт того, что предыдущие
 * частицы естественным образом остаются висеть в воздухе (не двигаются).
 *
 * particlesPerTick — сколько частиц спавнить за тик (плотность следа)
 * jitter — небольшой случайный разброс, чтобы след не выглядел как идеальная линия
 */
class TrailAnimation(
    private val particleType: ParticleOptions,
    private val particlesPerTick: Int = 1,
    private val jitter: Double = 0.05,
    override val durationTicks: Int? = null
) : ParticleAnimation {

    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        val shape = if (jitter > 0.0) {
            ParticleShape.Sphere(radius = jitter)
        } else {
            ParticleShape.Point()
        }

        return listOf(ParticleEffect(particleType, shape, count = particlesPerTick))
    }
}