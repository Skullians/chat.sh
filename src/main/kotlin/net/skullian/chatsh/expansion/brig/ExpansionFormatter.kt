package net.skullian.chatsh.expansion.brig

import com.mojang.brigadier.ParseResults
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.skullian.chatsh.ChatSh
import net.skullian.chatsh.expansion.ChatExpander
import net.skullian.chatsh.expansion.ChatExpander.hasShellSyntax

object ExpansionFormatter : EditBox.TextFormatter {

    private val ARGUMENT_STYLES = listOf(
        Style.EMPTY.withColor(ChatFormatting.AQUA),
        Style.EMPTY.withColor(ChatFormatting.YELLOW),
        Style.EMPTY.withColor(ChatFormatting.GREEN),
        Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE),
        Style.EMPTY.withColor(ChatFormatting.GOLD),
    )

    private val STYLE_LITERAL = Style.EMPTY.withColor(ChatFormatting.GRAY)
    private val STYLE_VALID = Style.EMPTY.withColor(ChatFormatting.AQUA)
    private val STYLE_PARTIAL = Style.EMPTY.withColor(ChatFormatting.GOLD)
    private val STYLE_INVALID = Style.EMPTY.withColor(ChatFormatting.RED)

    private var cachedInput: String? = null
    private var cachedResult: FormattedCharSequence? = null

    override fun format(raw: String, cursorOffset: Int): FormattedCharSequence? {
        if (!raw.startsWith("/") || !raw.hasShellSyntax()) {
            cachedInput = null; cachedResult = null
            return null
        }
        if (raw == cachedInput) return cachedResult

        val connection = Minecraft.getInstance().connection ?: return null

        val commands = try {
            ChatExpander.expand(raw, ChatSh.rootContext).commands
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (_: Exception) {
            cachedInput = null; cachedResult = null
            return null
        }

        if (commands.isEmpty() || (commands.size == 1 && commands[0] == raw.trim())) {
            cachedInput = null; cachedResult = null
            return null
        }

        val dispatcher = connection.commands
        val provider = connection.suggestionsProvider
        val parses: List<ParseResults<ClientSuggestionProvider>> = commands.map { cmd ->
            dispatcher.parse(cmd.removePrefix("/"), provider)
        }

        val validity = aggregate(parses)
        val styles = Array(raw.length) { STYLE_LITERAL }

        try {
            color(raw, commands.first(), parses.first(), styles)
        } catch (_: Exception) {}

        colorRegions(raw, styles, validity)

        val result = build(raw, styles)
        cachedInput = raw
        cachedResult = result

        return result
    }

    private fun color(
        input: String,
        expanded: String,
        parse: ParseResults<ClientSuggestionProvider>,
        styles: Array<Style>
    ) {
        val expandedRaw = expanded.removePrefix("/")
        val raw = input.removePrefix("/")
        val rawOffset = if (input.startsWith("/")) 1 else 0

        val mapSize = expandedRaw.length + 1
        val expandedToRaw = IntArray(mapSize) { (it + rawOffset).coerceAtMost(input.length) }

        var ei = 0
        var ri = 0
        while (ei < expandedRaw.length && ri < raw.length) {
            if (expandedRaw[ei] == raw[ri]) {
                expandedToRaw[ei] = ri + rawOffset
                ei++; ri++
            } else {
                val rawShellEnd = raw.indexOf(' ', ri).let { if (it < 0) raw.length else it }
                val expReplEnd = expandedRaw.indexOf(' ', ei).let { if (it < 0) expandedRaw.length else it }
                for (k in ei until expReplEnd.coerceAtMost(mapSize - 1)) {
                    expandedToRaw[k] = (ri + rawOffset).coerceAtMost(input.length)
                }
                ei = expReplEnd
                ri = rawShellEnd
            }
        }
        for (k in ei until mapSize) {
            expandedToRaw[k] = (ri + rawOffset).coerceAtMost(input.length)
        }

        var argIndex = 0
        for ((_, arg) in parse.context.lastChild.arguments) {
            val style = ARGUMENT_STYLES[argIndex++ % ARGUMENT_STYLES.size]
            val expStart = arg.range.start.coerceIn(0, expandedRaw.length)
            val expEnd = arg.range.end.coerceIn(0, expandedRaw.length)
            val rawStart = expandedToRaw[expStart].coerceIn(0, input.length)
            val rawEnd = expandedToRaw[expEnd].coerceIn(0, input.length)
            for (i in rawStart until rawEnd) styles[i] = style
        }
    }

    private enum class Validity(val style: Style) { VALID(STYLE_VALID), PARTIAL(STYLE_PARTIAL), INVALID(STYLE_INVALID) }

    private fun aggregate(parses: List<ParseResults<ClientSuggestionProvider>>): Validity {
        if (parses.isEmpty()) return Validity.INVALID
        val ok = parses.count { it.succeeded() }
        return when (ok) {
            parses.size -> Validity.VALID
            0 -> Validity.INVALID
            else-> Validity.PARTIAL
        }
    }

    private fun ParseResults<ClientSuggestionProvider>.succeeded(): Boolean {
        if (reader.canRead() && !reader.remaining.isBlank()) return false
        var ctx = context
        while (true) {
            if (ctx.command != null) return true
            ctx = ctx.child ?: return false
        }
    }

    private fun colorRegions(raw: String, styles: Array<Style>, validity: Validity) {
        val colour = validity.style
        var i = 0
        while (i < raw.length) {
            when {
                raw[i] == '{' -> {
                    val close = matchingBrace(raw, i)
                    val end = if (close >= 0) close + 1 else raw.length
                    for (j in i until end) styles[j] = colour
                    i = end
                }
                raw[i] == '$' -> {
                    val start = i++
                    while (i < raw.length && (raw[i].isLetterOrDigit() || raw[i] == '_')) i++
                    for (j in start until i) styles[j] = colour
                }
                raw[i] == ';' -> i++
                i + 1 < raw.length && raw[i] == '!' && raw[i + 1] == '!' -> {
                    styles[i] = colour; styles[i + 1] = colour
                    i += 2
                }
                raw[i] == '*' || raw[i] == '?' -> {
                    styles[i] = colour
                    i++
                }
                else -> i++
            }
        }
    }

    private fun matchingBrace(s: String, open: Int): Int {
        var depth = 0
        for (i in open until s.length) when (s[i]) {
            '{' -> depth++
            '}' -> if (--depth == 0) return i
        }
        return -1
    }

    private fun build(raw: String, styles: Array<Style>): FormattedCharSequence {
        val runs = mutableListOf<Pair<String, Style>>()
        var start = 0
        var cur = styles[0]
        for (i in 1 until raw.length) {
            if (styles[i] != cur) {
                runs += raw.substring(start, i) to cur
                start = i; cur = styles[i]
            }
        }
        runs += raw.substring(start) to cur
        return FormattedCharSequence.composite(
            runs.map { (text, style) -> FormattedCharSequence.forward(text, style) }
        )
    }
}
