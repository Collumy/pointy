package me.collumy.colib.keybind

import me.collumy.colib.scheduler.Scheduler
import me.collumy.colib.scheduler.ticks
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier

object KeybindManager {

    private val registeredKeybinds = mutableListOf<CustomKeybind>()
    private var isInitialized = false

    /**
     * Инициализация подписки на событие тика клиента Fabric.
     * Вызывается один раз при старте мода.
     */
     init {
        isInitialized = true

        Scheduler.repeat(1.ticks) {
            val client = Minecraft.getInstance()
            if (client.player == null) return@repeat

            for (bind in registeredKeybinds) {
                val isCurrentlyDown = bind.keyMapping.isDown

                if (!bind.condition()) {
                    bind.wasPressed = false
                    continue
                }

                // 1. Событие Удержания (Hold)
                if (isCurrentlyDown && bind.onHold != null) {
                    bind.onHold.invoke()
                }

                // 2. Одиночное нажатие (Press) - срабатывает 1 раз при нажатии
                if (isCurrentlyDown && !bind.wasPressed) {
                    bind.wasPressed = true
                    bind.onPress.invoke()
                }
                // 3. Отпускание клавиши (Release)
                else if (!isCurrentlyDown && bind.wasPressed) {
                    bind.wasPressed = false
                    bind.onRelease?.invoke()
                }
            }
        }
    }

    /**
     * Регистрация клавиши в настройках управления Minecraft
     */
    fun register(bind: CustomKeybind): CustomKeybind {
        val cat = bind.category
        val category = if (bind.category == null) KeyMapping.Category.MISC else
            KeyMapping.Category.register(Identifier.bySeparator(cat, '.'))

        val vanillaKey = bind.button.toVanillaKey()
        val mapping = KeyMapping(
            "key.${bind.id}",
            vanillaKey.type,
            vanillaKey.value,
            category
        )

        // Если hidden — просто НЕ регистрируем через хелпер
        bind.keyMapping = if (bind.isHidden) mapping else KeyBindingHelper.registerKeyBinding(mapping)

        registeredKeybinds.add(bind)
        return bind
    }

    fun register(id: String, defaultButton: Button, block: KeybindBuilder.() -> Unit): CustomKeybind {
        val builder = KeybindBuilder(id, defaultButton).apply(block)
        return register(CustomKeybind(
            builder.id, builder.button, builder.category, builder.isHidden,
            builder.condition, builder.onPress, builder.onRelease, builder.onHold
        ))
    }

    class KeybindBuilder(val id: String, val button: Button) {
        var category: String? = null
        var isHidden: Boolean = false
        var condition: () -> Boolean = { true }

        internal var onPress: () -> Unit = {}
        internal var onRelease: (() -> Unit)? = null
        internal var onHold: (() -> Unit)? = null

        fun onPress(action: () -> Unit) { onPress = action }
        fun onRelease(action: () -> Unit) { onRelease = action }
        fun onHold(action: () -> Unit) { onHold = action }
    }
}