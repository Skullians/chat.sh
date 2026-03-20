package net.skullian.chatsh.expansion.impl

import com.mojang.brigadier.StringReader
import net.minecraft.client.Minecraft
import net.skullian.chatsh.expansion.ChatExpander.containsGlob
import net.skullian.chatsh.expansion.Expander
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
        inputs.flatMap { expandOne(it, mutableSetOf()) }

    private fun expandOne(input: String, seen: MutableSet<String>): List<String> {
        if (!input.containsGlob()) return listOf(input)
        if (!seen.add(input)) return listOf(input)

        val connection = Minecraft.getInstance().connection ?: return listOf(input)

        val tokens = tokenise(input)
        val index = tokens.indexOfFirst { it.text.containsGlob() }
        if (index < 0) return listOf(input)

        val token = tokens[index]
        val prefix = input.take(token.start)
        val suffix = input.substring(token.end)

        val stripped = prefix.trimStart('/')
        val dispatcher = connection.commands
        val provider = connection.suggestionsProvider
        val reader = StringReader(stripped)
        val parse = dispatcher.parse(reader, provider)

        val pos = stripped.length
        val future = dispatcher.getCompletionSuggestions(parse, pos)
        if (!future.isDone) return listOf(input)

        val suggestions = runCatching { future.get() }.getOrNull() ?: return listOf(input)
        if (suggestions.isEmpty) return listOf(input)

        val pattern = GlobPattern.compile(token.text)
        val matches = suggestions.list
            .map { it.text }
            .filter { suggestion ->
                val path = suggestion.substringAfter(':')
                pattern.matches(suggestion) || pattern.matches(path)
            }
            .map { suggestion ->
                when {
                    ':' !in suggestion -> suggestion
                    prefix.contains(':') -> suggestion
                    else -> suggestion.substringAfter(':')
                }
            }

        if (matches.isEmpty()) return listOf(input)

        return matches.flatMap { match -> expandOne("$prefix$match$suffix", seen) }
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
                else -> while (i < input.length && input[i] != ' ') sb.append(input[i++])
            }
            tokens += Token(sb.toString(), start, i)
        }
        return tokens
    }

    private data class Token(val text: String, val start: Int, val end: Int)
}
