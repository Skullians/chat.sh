package net.skullian.chatsh.expansion

import net.skullian.chatsh.expansion.data.ExpansionCtx

fun Expander.canExpand(input: String): Boolean {
    return try {
        val result = expand(listOf(input), ExpansionCtx())
        result != listOf(input)
    } catch (_: Exception) {
        false
    }
}
