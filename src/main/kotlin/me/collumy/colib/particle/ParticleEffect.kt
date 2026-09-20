package me.collumy.colib.particle

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

data class ParticleEffect(
    val type: ParticleOptions,
    val shape: ParticleShape,
    val count: Int = 1,
    val velocity: Vec3 = Vec3.ZERO,
    val speed: Double = 0.0
)