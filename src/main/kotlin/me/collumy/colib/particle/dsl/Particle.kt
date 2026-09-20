package me.collumy.colib.particle.dsl

import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import java.awt.Color

object Particle {

    /** Redstone/Dust particle with RGB int */
    fun dust(color: Int, scale: Float = 1.0f): ParticleOptions =
        DustParticleOptions(color, scale)

    /** Crit particle */
    fun crit(): ParticleOptions =
        ParticleTypes.CRIT

    /** Enchant particle */
    fun enchant(): ParticleOptions =
        ParticleTypes.ENCHANT

    /** Glow particle */
    fun glow(): ParticleOptions =
        ParticleTypes.GLOW

    /** Soul particle */
    fun soul(): ParticleOptions =
        ParticleTypes.SOUL

    /** Flame particle */
    fun flame(): ParticleOptions =
        ParticleTypes.FLAME

    /** Smoke particle */
    fun smoke(): ParticleOptions =
        ParticleTypes.SMOKE

    /** Bubble particle */
    fun bubble(): ParticleOptions =
        ParticleTypes.BUBBLE

    /** Custom particle type */
    fun custom(type: ParticleType<*>, options: ParticleOptions): ParticleOptions =
        options


    fun Color.toInt(): Int {
        return (this.red shl 16) or (this.green shl 8) or this.blue
    }
}
