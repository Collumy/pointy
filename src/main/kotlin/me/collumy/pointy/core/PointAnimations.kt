package me.collumy.pointy.core

import me.collumy.colib.particle.ParticleShape
import me.collumy.colib.particle.animation.ParticleAnimation
import me.collumy.colib.particle.dsl.Particle.dust
import me.collumy.colib.particle.dsl.animation
import me.collumy.pointy.PointyConfig

object PointAnimations {

    val animations = listOf("orbit", "pulse", "spark", "outline")
    fun have(id: String) = id in animations

    operator fun get(haveColor: Int?, isEntity: Boolean = false): ParticleAnimation {
        val animation = if (isEntity) PointyConfig.entity.animation else PointyConfig.block.animation
        val defaultColor = if (isEntity) PointyConfig.entity.color else PointyConfig.block.color
        val color = haveColor ?: defaultColor

        return when (animation) {
            "orbit" -> animation {
                orbit(
                    type = dust(color),
                    radius = 0.7,
                    ticksPerRevolution = 40,
                    particlesPerTick = 2
                )
            }
            "pulse" -> animation {
                flicker {
                    pulse(
                        type = dust(color),
                        interval = 10,
                        maxRadius = 1.0
                    )
                }
            }
            "spark" -> animation {
                loop(5) {
                    spark(
                        type = dust(color),
                        radius = 1.0
                    )
                }
            }
            "outline" -> animation {
                loop(5) {
                    burst(
                        type = dust(color),
                        shape = ParticleShape.CubeOutline(),
                        20
                    )
                }
            }

            else -> animation {
                orbit(
                    type = dust(color),
                    radius = 0.7,
                    ticksPerRevolution = 40,
                    particlesPerTick = 2
                )
            }
        }
    }

}