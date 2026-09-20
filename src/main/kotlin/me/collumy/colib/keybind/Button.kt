package me.collumy.colib.keybind

import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

enum class Button(val code: Int, val isMouse: Boolean = false) {
    // Буквы
    A(GLFW.GLFW_KEY_A), B(GLFW.GLFW_KEY_B), C(GLFW.GLFW_KEY_C), D(GLFW.GLFW_KEY_D),
    E(GLFW.GLFW_KEY_E), F(GLFW.GLFW_KEY_F), G(GLFW.GLFW_KEY_G), H(GLFW.GLFW_KEY_H),
    I(GLFW.GLFW_KEY_I), J(GLFW.GLFW_KEY_J), K(GLFW.GLFW_KEY_K), L(GLFW.GLFW_KEY_L),
    M(GLFW.GLFW_KEY_M), N(GLFW.GLFW_KEY_N), O(GLFW.GLFW_KEY_O), P(GLFW.GLFW_KEY_P),
    Q(GLFW.GLFW_KEY_Q), R(GLFW.GLFW_KEY_R), S(GLFW.GLFW_KEY_S), T(GLFW.GLFW_KEY_T),
    U(GLFW.GLFW_KEY_U), V(GLFW.GLFW_KEY_V), W(GLFW.GLFW_KEY_W), X(GLFW.GLFW_KEY_X),
    Y(GLFW.GLFW_KEY_Y), Z(GLFW.GLFW_KEY_Z),

    // Цифры верхнего ряда (NUM_1, NUM_2 ...)
    KEY_0(GLFW.GLFW_KEY_0), KEY_1(GLFW.GLFW_KEY_1), KEY_2(GLFW.GLFW_KEY_2),
    KEY_3(GLFW.GLFW_KEY_3), KEY_4(GLFW.GLFW_KEY_4), KEY_5(GLFW.GLFW_KEY_5),
    KEY_6(GLFW.GLFW_KEY_6), KEY_7(GLFW.GLFW_KEY_7), KEY_8(GLFW.GLFW_KEY_8),
    KEY_9(GLFW.GLFW_KEY_9),

    // Модификаторы
    LEFT_ALT(GLFW.GLFW_KEY_LEFT_ALT), RIGHT_ALT(GLFW.GLFW_KEY_RIGHT_ALT),
    LEFT_SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT), RIGHT_SHIFT(GLFW.GLFW_KEY_RIGHT_SHIFT),
    LEFT_CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL), RIGHT_CONTROL(GLFW.GLFW_KEY_RIGHT_CONTROL),

    // Служебные
    SPACE(GLFW.GLFW_KEY_SPACE),
    ENTER(GLFW.GLFW_KEY_ENTER),
    TAB(GLFW.GLFW_KEY_TAB),
    ESCAPE(GLFW.GLFW_KEY_ESCAPE),
    BACKSPACE(GLFW.GLFW_KEY_BACKSPACE),
    CAPS_LOCK(GLFW.GLFW_KEY_CAPS_LOCK),

    // Функциональные
    F1(GLFW.GLFW_KEY_F1), F2(GLFW.GLFW_KEY_F2), F3(GLFW.GLFW_KEY_F3),
    F4(GLFW.GLFW_KEY_F4), F5(GLFW.GLFW_KEY_F5), F6(GLFW.GLFW_KEY_F6),
    F7(GLFW.GLFW_KEY_F7), F8(GLFW.GLFW_KEY_F8), F9(GLFW.GLFW_KEY_F9),
    F10(GLFW.GLFW_KEY_F10), F11(GLFW.GLFW_KEY_F11), F12(GLFW.GLFW_KEY_F12),

    // Кнопки мыши
    MOUSE_LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT, isMouse = true),
    MOUSE_RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT, isMouse = true),
    MOUSE_MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, isMouse = true),
    MOUSE_4(GLFW.GLFW_MOUSE_BUTTON_4, isMouse = true),
    MOUSE_5(GLFW.GLFW_MOUSE_BUTTON_5, isMouse = true),

    // Неназначенная кнопка
    NONE(InputConstants.UNKNOWN.value);

    /** Преобразует наш enum в ванильный тип InputConstants.Key */
    fun toVanillaKey(): InputConstants.Key {
        return if (isMouse) {
            InputConstants.Type.MOUSE.getOrCreate(code)
        } else {
            InputConstants.Type.KEYSYM.getOrCreate(code)
        }
    }
}