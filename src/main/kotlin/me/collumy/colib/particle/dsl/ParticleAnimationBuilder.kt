package me.collumy.colib.particle.dsl

import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.FadeModifier
import me.collumy.colib.particle.animation.FlickerModifier
import me.collumy.colib.particle.animation.LoopAnimation
import me.collumy.colib.particle.animation.ParallelAnimation
import me.collumy.colib.particle.animation.ParticleAnimation
import me.collumy.colib.particle.animation.SequentialAnimation
import me.collumy.colib.particle.animation.SpeedModifier
import me.collumy.colib.particle.animation.builtin.BurstAnimation
import me.collumy.colib.particle.animation.builtin.OrbitAnimation
import me.collumy.colib.particle.animation.builtin.ParticlePath
import me.collumy.colib.particle.animation.builtin.PathAnimation
import me.collumy.colib.particle.animation.builtin.PulseAnimation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

class AnimationBuilder {
    private val animations = mutableListOf<ParticleAnimation>()

    fun orbit(
        type: ParticleOptions,
        radius: Double,
        ticksPerRevolution: Int = 40,
        axis: Vec3 = Vec3(0.0, 1.0, 0.0),
        particlesPerTick: Int = 1,
        verticalOffset: Double = 0.0,
        duration: Int? = null
    ) {
        animations += OrbitAnimation(
            particleType = type,
            radius = radius,
            ticksPerRevolution = ticksPerRevolution,
            axis = axis,
            particlesPerTick = particlesPerTick,
            verticalOffset = verticalOffset,
            durationTicks = duration
        )
    }

    fun burst(type: ParticleOptions, shape: ParticleShape, count: Int) {
        animations += BurstAnimation(type, shape, count)
    }

    fun spark(
        type: ParticleOptions,
        count: Int = 8,
        radius: Double = 0.3
    ) {
        val shape = ParticleShape.Sphere(radius)
        animations += BurstAnimation(type, shape, count)
    }

    fun pulse(
        type: ParticleOptions,
        interval: Int,
        burstCount: Int = 20,
        maxRadius: Double = 2.0,
        cycles: Int? = null,
        segments: Int = 24
    ) {
        animations += PulseAnimation(type, interval, burstCount, maxRadius, cycles, segments)
    }

    fun path(
        type: ParticleOptions,
        path: ParticlePath,
        durationTicksTotal: Int,
        particlesPerTick: Int = 1,
        loop: Boolean = false,
        duration: Int? = null
    ) {
        animations += PathAnimation(type, path, durationTicksTotal, particlesPerTick, loop, duration)
    }

    fun flicker(chance: Float = 0.3f, block: AnimationBuilder.() -> Unit) {
        val inner = AnimationBuilder().apply(block).build()
        animations += FlickerModifier(ParallelAnimation(inner), chance)
    }

    fun speed(multiplier: Double) {
        animations.replaceAll { SpeedModifier(it, multiplier) }
    }

    fun loop(cooldownTicks: Int = 0, block: AnimationBuilder.() -> Unit) {
        val inner = AnimationBuilder().apply(block).build()
        animations += LoopAnimation(ParallelAnimation(inner), 999, cooldownTicks)
    }

    fun parallel(block: AnimationBuilder.() -> Unit) {
        val inner = AnimationBuilder().apply(block).build()
        animations += ParallelAnimation(inner)
    }

    fun sequential(block: AnimationBuilder.() -> Unit) {
        val inner = AnimationBuilder().apply(block).build()
        animations += SequentialAnimation(inner)
    }

    fun build(): List<ParticleAnimation> = animations
}

fun animation(block: AnimationBuilder.() -> Unit): ParticleAnimation =
    ParallelAnimation(AnimationBuilder().apply(block).build())
