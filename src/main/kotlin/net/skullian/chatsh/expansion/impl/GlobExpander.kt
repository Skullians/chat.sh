package net.skullian.chatsh.expansion.impl

import net.minecraft.client.Minecraft
import net.skullian.chatsh.expansion.ChatExpander.containsGlob
import net.skullian.chatsh.expansion.Expander
import net.skullian.chatsh.expansion.brig.BrigadierCtx
import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.chatsh.expansion.pattern.GlobPattern
import net.skullian.zenith.core.flavor.annotation.Service

/**
 * This is a class.
 *
 * @author Skullians
 * @since 12/03/2026
 */
@Service
object GlobExpander : Expander {
    override val id = "glob"
    override val priority = 20

    override fun expand(inputs: List<String>, ctx: ExpansionCtx): List<String> =
        inputs.flatMap { expandOne(it) }

    private fun expandOne(input: String): List<String> {
        if (!input.containsGlob()) return listOf(input)

        val client = Minecraft.getInstance()
        if (client.connection == null) return listOf(input)

        val tokens = tokenise(input)
        val index = tokens.indexOfFirst { it.text.containsGlob() }
        if (index < 0) return listOf(input)

        val token = tokens[index]

        val end = token.start
        val prefix = input.substring(0, end)
        val suffix = input.substring(token.end)

        val suggestions = BrigadierCtx.getCompletionsAt(token.start)
            ?: return listOf(input)

        if (suggestions.isEmpty) return listOf(input)

        val pattern = GlobPattern.compile(token.text)
        val matches = suggestions.list
            .map { it.text }
            .filter { suggestion ->
                val pathOnly = suggestion.substringAfter(':')
                pattern.matches(suggestion) || pattern.matches(pathOnly)
            }
            .map { suggestion ->
                when {
                    ':' !in suggestion -> suggestion
                    prefix.contains(':') -> suggestion
                    else -> suggestion.substringAfter(':')
                }
            }

        if (matches.isEmpty()) return listOf(input)

        return matches.flatMap { match -> expandOne("$prefix$match$suffix") }
    }

    private fun tokenise(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < input.length) {
            if (input[i] == ' ') { i++; continue }

            val start = i
            val sb = StringBuilder()

            when (input[i]) {
                '"', '\'' -> {
                    val quote = input[i++]
                    while (i < input.length && input[i] != quote) sb.append(input[i++])
                    if (i < input.length) i++
                }
                else -> {
                    while (i < input.length && input[i] != ' ') sb.append(input[i++])
                }
            }

            tokens += Token(sb.toString(), start, i)
        }

        return tokens
    }

    private data class Token(val text: String, val start: Int, val end: Int)

}