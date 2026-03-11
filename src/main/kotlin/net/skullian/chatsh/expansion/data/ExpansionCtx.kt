package net.skullian.chatsh.expansion.data

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
data class ExpansionCtx(
    val variables: MutableMap<String, String> = mutableMapOf(),
    val commandHistory: MutableList<String> = mutableListOf(),
    val lastOutput: String? = null
) {
    fun withVariable(name: String, value: String): ExpansionCtx =
        copy(variables = (variables + (name to value)).toMutableMap())
}
