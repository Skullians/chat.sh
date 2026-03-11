package net.skullian.chatsh.expansion.completion.data

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
data class CompletionHint(
    val type: HintType,
    val label: String,
    val tooltip: String = "",
    val insertText: String? = null,
    val commandCount: Int = 1
)
