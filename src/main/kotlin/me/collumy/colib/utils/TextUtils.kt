package me.collumy.colib.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.util.ArrayDeque

object TextUtils {

    private val MINI_HEX_REGEX = Regex("^<#([0-9A-Fa-f]{6})>")
    private val MINI_COLORS_REGEX = Regex("^<(red|green|blue|yellow|gold|gray|white|aqua|dark_gray|light_purple)>")

    private val LEGACY_HEX_REGEX = Regex("&#([0-9A-Fa-f]{6})")
    private val LEGACY_COLORS_REGEX = Regex("(?i)&([0-9a-flur])")
    private val LEGACY_MAP = mapOf(
        '0' to "<black>", '1' to "<dark_blue>", '2' to "<dark_green>", '3' to "<dark_aqua>",
        '4' to "<dark_red>", '5' to "<dark_purple>", '6' to "<gold>", '7' to "<gray>",
        '8' to "<dark_gray>", '9' to "<blue>", 'a' to "<green>", 'b' to "<aqua>",
        'c' to "<red>", 'd' to "<light_purple>", 'e' to "<yellow>", 'f' to "<white>",
        'l' to "<b>", 'n' to "<u>", 'r' to "</>"
    )

    private sealed interface TagInfo {
        data class ColorHex(val hex: String) : TagInfo
        data class ColorNamed(val name: String) : TagInfo
        data object Bold : TagInfo
        data object Underlined : TagInfo
        data class Hover(val component: Component) : TagInfo
        data class Click(val command: String) : TagInfo
    }

    fun parse(input: String?): Component {
        if (input.isNullOrEmpty()) return Component.empty()

        val normalized = convertLegacyToMiniMessage(input)
        return parseWithStack(normalized)
    }

    fun String.toText(): Component = parse(this)

    private fun convertLegacyToMiniMessage(input: String): String {
        val hexConverted = LEGACY_HEX_REGEX.replace(input) { match -> "<#${match.groupValues[1]}>" }
        return LEGACY_COLORS_REGEX.replace(hexConverted) { match ->
            val code = match.groupValues[1][0].lowercaseChar()
            LEGACY_MAP[code] ?: match.value
        }
    }

    private fun parseWithStack(input: String): MutableComponent {
        val result = Component.empty()
        val buffer = StringBuilder()
        val tagStack = ArrayDeque<TagInfo>()

        var i = 0
        while (i < input.length) {

            // Закрытие последнего тега </>
            if (input.startsWith("</>", i)) {
                flushBuffer(result, buffer, tagStack)
                if (tagStack.isNotEmpty()) {
                    tagStack.removeLast()
                }
                i += 3
                continue
            }

            // Hover (Подсказка при наведении)
            if (input.startsWith("<hover:'", i)) {
                val quoteEnd = input.indexOf("'>", i + 8)
                if (quoteEnd != -1) {
                    flushBuffer(result, buffer, tagStack)
                    val hoverText = input.substring(i + 8, quoteEnd)

                    val parsedText = parse(hoverText)
                    tagStack.addLast(TagInfo.Hover(parsedText))

                    i = quoteEnd + 2
                    continue
                }
            }

            // Click (Подстановка команды)
            if (input.startsWith("<click:'", i)) {
                val quoteEnd = input.indexOf("'>", i + 8)
                if (quoteEnd != -1) {
                    flushBuffer(result, buffer, tagStack)
                    val command = input.substring(i + 8, quoteEnd)
                    tagStack.addLast(TagInfo.Click(command))
                    i = quoteEnd + 2
                    continue
                }
            }

            // Bold (Жирный)
            if (input.startsWith("<b>", i)) {
                flushBuffer(result, buffer, tagStack)
                tagStack.addLast(TagInfo.Bold)
                i += 3
                continue
            }

            // Underlined (Подчёркнутый)
            if (input.startsWith("<u>", i)) {
                flushBuffer(result, buffer, tagStack)
                tagStack.addLast(TagInfo.Underlined)
                i += 3
                continue
            }

            // Hex цвет (<#RRGGBB>)
            if (input.startsWith("<#", i)) {
                val match = MINI_HEX_REGEX.find(input.substring(i))
                if (match != null) {
                    flushBuffer(result, buffer, tagStack)
                    tagStack.addLast(TagInfo.ColorHex("#" + match.groupValues[1]))
                    i += match.value.length
                    continue
                }
            }

            // Именованный цвет
            if (input[i] == '<') {
                val match = MINI_COLORS_REGEX.find(input.substring(i))
                if (match != null) {
                    flushBuffer(result, buffer, tagStack)
                    tagStack.addLast(TagInfo.ColorNamed(match.groupValues[1]))
                    i += match.value.length
                    continue
                }
            }

            buffer.append(input[i])
            i++
        }

        flushBuffer(result, buffer, tagStack)
        return result
    }

    private fun flushBuffer(result: MutableComponent, buffer: StringBuilder, tagStack: ArrayDeque<TagInfo>) {
        if (buffer.isNotEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(buildStyle(tagStack)))
            buffer.clear()
        }
    }

    private fun buildStyle(tagStack: ArrayDeque<TagInfo>): Style {
        var style = Style.EMPTY

        for (tag in tagStack) {
            style = when (tag) {
                is TagInfo.ColorHex -> {
                    val rgb = tag.hex.substring(1).toIntOrNull(16)
                    if (rgb != null) style.withColor(TextColor.fromRgb(rgb)) else style
                }
                is TagInfo.ColorNamed -> {
                    val fmt = ChatFormatting.valueOf(tag.name.uppercase())
                    style.withColor(fmt)
                }
                is TagInfo.Bold -> style.withBold(true)
                is TagInfo.Underlined -> style.withUnderlined(true)

                is TagInfo.Hover -> style.withHoverEvent(HoverEvent.ShowText(tag.component))
                is TagInfo.Click -> style.withClickEvent(ClickEvent.SuggestCommand(tag.command))
            }
        }

        return style
    }

    operator fun Component.plus(other: Component): MutableComponent = this.copy().append(other)
    operator fun Component.plus(other: String): MutableComponent = this.copy().append(other.toText())
}