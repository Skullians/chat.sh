package net.skullian.chatsh.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.skullian.chatsh.ChatSh
import net.skullian.chatsh.expansion.ChatExpander

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
object ExpansionRenderer {

    private const val PANEL_HEIGHT = 13
    private const val PADDING_X = 4
    private const val PADDING_Y = 2
    private const val MAX_PREVIEW_COMMANDS = 5

    private const val BG_COLOR = 0xB0000000.toInt()
    private const val ACCENT_COLOR = 0xFF55FF55.toInt()
    private const val ARROW_COLOR = 0xFFAAAAAA.toInt()
    private const val CMD_COLOR = 0xFFFFFFFF.toInt()
    private const val DOT_COLOR = 0xFF666666.toInt()
    private const val MORE_COLOR = 0xFF888888.toInt()
    private const val COUNT_COLOR = 0xFF55FF55.toInt()

    fun render(graphics: GuiGraphics, screenWidth: Int, screenHeight: Int) {
        val client = Minecraft.getInstance()
        val font = client.font

        val input = client.screen?.let {
            try {
                val field = it.javaClass.getDeclaredField("input")
                    .also { f -> f.isAccessible = true }
                (field.get(it) as? net.minecraft.client.gui.components.EditBox)?.value
            } catch (_: Exception) { null }
        } ?: return

        if (input.isBlank() || !input.hasShellSyntax()) return

        val result = ChatExpander.expand(input, ChatSh.rootContext)
        if (!result.isMultiCommand) return

        val commands = result.commands
        val panelY = screenHeight - 14 - PANEL_HEIGHT - 1 // shove this shit above the input box

        graphics.fill(2, panelY, screenWidth - 2, panelY + PANEL_HEIGHT, BG_COLOR)

        var x = 2 + PADDING_X

        x = drawString(graphics, font, "chat.sh", x, panelY + PADDING_Y, ACCENT_COLOR)
        x += 4

        x = drawString(graphics, font, "→", x, panelY + PADDING_Y, ARROW_COLOR)
        x += 4

        x = drawString(graphics, font, "(${commands.size})", x, panelY + PADDING_Y, COUNT_COLOR)
        x += 6

        val preview = commands.take(MAX_PREVIEW_COMMANDS)
        preview.forEachIndexed { i, cmd ->
            if (i > 0) {
                x = drawComponent(graphics, font,
                    Component.literal(" · ").withStyle { it.withBold(true) },
                    x, panelY + PADDING_Y, DOT_COLOR
                )
            }
            val label = if (cmd.length > 30) cmd.take(27) + "…" else cmd
            x = drawString(graphics, font, label, x, panelY + PADDING_Y, CMD_COLOR)

            if (x > screenWidth - 60) {
                val remaining = commands.size - (i + 1)
                if (remaining > 0) {
                    x += 4
                    drawString(graphics, font, "+$remaining more", x, panelY + PADDING_Y, MORE_COLOR)
                }
                return@forEachIndexed
            }
        }

        if (commands.size > MAX_PREVIEW_COMMANDS) {
            x += 4
            drawString(graphics, font, "+${commands.size - MAX_PREVIEW_COMMANDS} more", x, panelY + PADDING_Y, MORE_COLOR)
        }
    }

    private fun drawString(
        graphics: GuiGraphics,
        font: Font,
        text: String,
        x: Int,
        y: Int,
        color: Int
    ): Int {
        graphics.drawString(font, text, x, y, color, false)
        return x + font.width(text)
    }

    private fun drawComponent(
        graphics: GuiGraphics,
        font: Font,
        component: Component,
        x: Int,
        y: Int,
        color: Int
    ): Int {
        graphics.drawString(font, component, x, y, color, false)
        return x + font.width(component)
    }

    private fun String.hasShellSyntax() =
        contains("{") || contains("$") || contains(";") || startsWith("!!")
}
