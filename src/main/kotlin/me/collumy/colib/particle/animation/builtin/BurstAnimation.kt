package me.collumy.colib.particle.animation.builtin

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

class BurstAnimation(
    private val particleType: ParticleOptions,
    private val shape: ParticleShape,
    private val count: Int,
    override val durationTicks: Int = 1 // один моментальный спавн
) : ParticleAnimation {
    override fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect> {
        if (elapsedTicks != 0) return emptyList()
        return listOf(ParticleEffect(particleType, shape, count))
    }
}