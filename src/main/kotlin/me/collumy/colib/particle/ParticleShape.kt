package me.collumy.colib.particle

import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed interface ParticleShape {
    fun points(center: Vec3, count: Int): List<Vec3>

    data class Point(val offset: Vec3 = Vec3.ZERO) : ParticleShape {
        override fun points(center: Vec3, count: Int) = List(count) { center.add(offset) }
    }

    data class Sphere(val radius: Double) : ParticleShape {
        override fun points(center: Vec3, count: Int): List<Vec3> {
            return List(count) { i ->
                val phi = acos(1 - 2.0 * (i + 0.5) / count)
                val theta = Math.PI * (1 + sqrt(5.0)) * i
                Vec3(
                    center.x + radius * sin(phi) * cos(theta),
                    center.y + radius * cos(phi),
                    center.z + radius * sin(phi) * sin(theta)
                )
            }
        }
    }

    data class Circle(val radius: Double, val normal: Vec3 = Vec3(0.0, 1.0, 0.0)) : ParticleShape {
        override fun points(center: Vec3, count: Int): List<Vec3> {
            val n = normal.normalize()
            val arbitrary = if (abs(n.x) < 0.9) Vec3(1.0, 0.0, 0.0) else Vec3(0.0, 1.0, 0.0)

            val u = n.cross(arbitrary).normalize()
            val v = n.cross(u).normalize()

            return List(count) { i ->
                val angle = (i.toDouble() / count) * Math.PI * 2
                center.add(u.scale(cos(angle) * radius))
                      .add(v.scale(sin(angle) * radius))
            }
        }
    }


    data class Line(val from: Vec3, val to: Vec3) : ParticleShape {
        override fun points(center: Vec3, count: Int): List<Vec3> {
            return (0 until count).map { i ->
                from.lerp(to, i.toDouble() / (count - 1).coerceAtLeast(1))
            }
        }
    }

    data class Spiral(val radius: Double, val height: Double, val turns: Double = 2.0) : ParticleShape {
        override fun points(center: Vec3, count: Int): List<Vec3> {
            return List(count) { i ->
                val t = i.toDouble() / count
                val angle = t * turns * Math.PI * 2
                Vec3(
                    center.x + radius * cos(angle),
                    center.y + t * height,
                    center.z + radius * sin(angle)
                )
            }
        }
    }

    data class CubeOutline(val size: Double = 1.0) : ParticleShape {
        override fun points(center: Vec3, count: Int): List<Vec3> {
            val half = size / 2
            val edges = listOf(
                Vec3(-half, -half, -half), Vec3(half, -half, -half),
                Vec3(half, -half, half), Vec3(-half, -half, half),
                Vec3(-half, half, -half), Vec3(half, half, -half),
                Vec3(half, half, half), Vec3(-half, half, half)
            )

            return edges.map { center.add(it) }
        }
    }

}