package net.skullian.chatsh.expansion.impl

import net.skullian.chatsh.expansion.Expander
import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.zenith.core.flavor.annotation.Service

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
@Service
object BraceExpander : Expander {
    override val id = "braces"
    override val priority = 10

    override fun expand(inputs: List<String>, ctx: ExpansionCtx): List<String> =
        inputs.flatMap { expandOne(it) }

    private fun expandOne(input: String): List<String> {
        val braceRange = findTopLevel(input) ?: return listOf(input)
        val (openIdx, closeIdx) = braceRange

        val prefix = input.take(openIdx)
        val inside = input.substring(openIdx + 1, closeIdx)
        val suffix = input.substring(closeIdx + 1)

        val alternatives = parseContent(inside)
            ?: return listOf(input)

        return alternatives.flatMap { alt ->
            expandOne("$prefix$alt$suffix")
        }
    }

    private fun findTopLevel(s: String): Pair<Int, Int>? {
        var depth = 0
        var openIdx = -1

        for (i in s.indices) {
            when (s[i]) {
                '{' -> {
                    if (depth++ == 0) openIdx = i
                }
                '}' -> {
                    if (--depth == 0 && openIdx >= 0) return openIdx to i
                }
            }
        }
        return null
    }

    private fun parseContent(inside: String): List<String>? {
        if (inside.isEmpty()) return null

        val rangeResult = tryParseRange(inside)
        if (rangeResult != null) return rangeResult

        val parts = splitTopLevel(inside, ',')
        if (parts.size < 2) return null

        return parts.flatMap { expandOne(it.trim()) }
    }

    private fun tryParseRange(content: String): List<String>? {
        if (splitTopLevel(content, ',').size > 1) return null

        val parts = content.split("..")
        if (parts.size !in 2..3) return null

        val start = parts[0].trim()
        val end = parts[1].trim()
        val stepStr = parts.getOrNull(2)?.trim()

        val startInt = start.toIntOrNull()
        val endInt = end.toIntOrNull()
        if (startInt != null && endInt != null) {
            val step = stepStr?.toIntOrNull() ?: 1
            if (step <= 0) return null
            val padWidth = maxOf(start.length, end.length).takeIf {
                (start.length > 1 && start.startsWith("0")) ||
                    (end.length > 1 && end.startsWith("0"))
            } ?: 0
            return buildIntRange(startInt, endInt, step, padWidth)
        }

        if (start.length == 1 && end.length == 1 && stepStr == null) {
            return buildCharRange(start[0], end[0])
        }

        return null
    }

    private fun buildIntRange(start: Int, end: Int, step: Int, padWidth: Int): List<String> {
        val range = if (start <= end) {
            generateSequence(start) { it + step }.takeWhile { it <= end }
        } else {
            generateSequence(start) { it - step }.takeWhile { it >= end }
        }
        return range.map { n ->
            if (padWidth > 0) n.toString().padStart(padWidth, '0') else n.toString()
        }.toList()
    }

    private fun buildCharRange(start: Char, end: Char): List<String> {
        return if (start <= end) {
            (start..end).map { it.toString() }
        } else {
            (end..start).map { it.toString() }.reversed()
        }
    }

    private fun splitTopLevel(s: String, delimiter: Char): List<String> {
        val parts = mutableListOf<String>()
        var depth = 0
        var start = 0

        for (i in s.indices) {
            when (s[i]) {
                '{' -> depth++
                '}' -> depth--
                delimiter -> if (depth == 0) {
                    parts += s.substring(start, i)
                    start = i + 1
                }
            }
        }

        parts += s.substring(start)
        return parts
    }
}
