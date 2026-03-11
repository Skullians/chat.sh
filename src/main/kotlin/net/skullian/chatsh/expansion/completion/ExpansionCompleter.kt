package net.skullian.chatsh.expansion.completion

import net.skullian.chatsh.expansion.completion.data.CommandSuggestion
import net.skullian.chatsh.expansion.completion.data.CompletionHint
import net.skullian.chatsh.expansion.completion.data.HintType
import net.skullian.chatsh.expansion.completion.data.SuggestionSource
import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.chatsh.expansion.impl.BraceExpander

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
class ExpansionCompleter(
    private val ctx: ExpansionCtx
) {

    fun applyBrigadier(
        input: String,
        suggestions: List<String> // from brigadishart
    ): List<CommandSuggestion> {
        return suggestions.map { CommandSuggestion(it, SuggestionSource.BRIGADIER)  }
    }

    fun hints(input: String, pos: Int): List<CompletionHint> {
        return buildList {
            addAll(expansionHints(input))
            addAll(variableHints(input, pos))
            addAll(historyHints(input))
        }
    }

    private fun expansionHints(input: String): List<CompletionHint> {
        if (!input.contains("{")) return emptyList()

        return try {
            val expanded = BraceExpander.expand(listOf(input), ctx)
            if (expanded.size <= 1 || expanded == listOf(input)) return emptyList()

            val preview = expanded.take(5).joinToString(", ")
            val more = if (expanded.size > 5) " (+${expanded.size - 5} more)" else ""

            listOf(CompletionHint(
                type = HintType.EXPANSION_PREVIEW,
                label = "-> $preview$more",
                tooltip = "Press Enter to run ${expanded.size} command${if (expanded.size != 1) "s" else ""}",
                commandCount = expanded.size
            ))
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun variableHints(input: String, cursorPos: Int): List<CompletionHint> {
        val beforeCursor = input.take(minOf(cursorPos, input.length))

        val dollarIdx = beforeCursor.lastIndexOf('$')
        if (dollarIdx < 0) return emptyList()

        val afterDollar = beforeCursor.substring(dollarIdx + 1)
        if (afterDollar.contains(' ') || afterDollar.contains('\t')) return emptyList()

        val prefix = afterDollar.trimStart('{')
        val matchingVars = ctx.variables.keys.filter { it.startsWith(prefix) }

        return matchingVars.map { varName ->
            CompletionHint(
                type = HintType.VARIABLE,
                label = $$"$$varName = $${ctx.variables[varName]}",
                tooltip = "variable: $varName",
                insertText = varName.substring(prefix.length)
            )
        }
    }

    private fun historyHints(input: String): List<CompletionHint> {
        if (!input.startsWith("!!") && !input.startsWith("!")) return emptyList()

        return when {
            input == "!!" || input.startsWith("!! ") -> {
                val last = ctx.commandHistory.lastOrNull() ?: return emptyList()
                listOf(CompletionHint(
                    type = HintType.HISTORY,
                    label = "!! -> $last",
                    tooltip = "repeat last command",
                    insertText = last
                ))
            }
            input.startsWith("!") && input.length > 1 -> {
                val prefix = input.substring(1)
                val match = ctx.commandHistory.lastOrNull { it.startsWith(prefix) }
                    ?: return emptyList()
                listOf(CompletionHint(
                    type = HintType.HISTORY,
                    label = "!$prefix -> $match",
                    tooltip = "repeat: $match",
                    insertText = match
                ))
            }
            else -> emptyList()
        }
    }
}
