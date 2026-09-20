package me.collumy.colib.particle.dsl

import me.collumy.colib.particle.ParticleEffect
import me.collumy.colib.particle.ParticleShape
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

class ParticleEffectBuilder {
    var type: ParticleOptions? = null
    var shape: ParticleShape = ParticleShape.Point(Vec3.ZERO)
    var count: Int = 1
    var velocity: Vec3 = Vec3.ZERO
    var speed: Double = 0.0

    fun build(): ParticleEffect {
        return ParticleEffect(
            type ?: error("ParticleEffect.type must be set"),
            shape,
            count,
            velocity,
            speed
        )
    }
}

fun effect(block: ParticleEffectBuilder.() -> Unit): ParticleEffect =
    ParticleEffectBuilder().apply(block).build()
