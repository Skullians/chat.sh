package net.skullian.chatsh.expansion.data

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
data class ExpansionResult(
    val commands: List<String>,
    val errors: List<String> = emptyList()
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val isMultiCommand: Boolean get() = commands.size > 1
}
