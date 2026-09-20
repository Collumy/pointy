package me.collumy.colib.keybind

import net.minecraft.client.KeyMapping

class CustomKeybind internal constructor(
    val id: String,
    val button: Button,
    val category: String?,

    val isHidden: Boolean,
    val condition: () -> Boolean,

    val onPress: () -> Unit,
    val onRelease: (() -> Unit)?,
    val onHold: (() -> Unit)?
) {
    lateinit var keyMapping: KeyMapping
        internal set

    internal var wasPressed = false
}