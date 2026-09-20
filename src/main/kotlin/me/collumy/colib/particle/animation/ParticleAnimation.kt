package me.collumy.colib.particle.animation

import me.collumy.colib.particle.ParticleEffect
import net.minecraft.world.phys.Vec3

interface ParticleAnimation {
    /** сколько всего длится анимация в тиках, null = бесконечно (пока не остановят вручную) */
    val durationTicks: Int?

    /** вызывается каждый тик с прогрессом от старта; возвращает что заспавнить в этом тике */
    fun tick(elapsedTicks: Int, origin: Vec3): List<ParticleEffect>

    fun isFinished(elapsedTicks: Int): Boolean =
        durationTicks != null && elapsedTicks >= durationTicks!!
}