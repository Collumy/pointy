package me.collumy.colib.particle

import me.collumy.colib.particle.animation.ParticleAnimation
import me.collumy.colib.particle.animation.builtin.BurstAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

object Particles {
    fun burst(type: ParticleOptions, shape: ParticleShape, count: Int): ParticleAnimation =
        BurstAnimation(type, shape, count)

    fun play(animation: ParticleAnimation, at: Vec3) = ParticleManager.play(animation, at)
}