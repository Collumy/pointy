package me.collumy.colib.utils

import me.collumy.pointy.Pointy.log
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType

object EntityAction {

    fun spawnDisplay(level: ClientLevel, pos: BlockPos, text: Component): Display.TextDisplay {
        val display = Display.TextDisplay(EntityType.TEXT_DISPLAY, level)

        display.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        display.backgroundColor = 0x10000000
        display.text = text
        display.billboardConstraints = Display.BillboardConstraints.CENTER

        level.addEntity(display)
        log("Создана сущность на ${display.x}, ${display.y}, ${display.z}")

        return display
    }
}