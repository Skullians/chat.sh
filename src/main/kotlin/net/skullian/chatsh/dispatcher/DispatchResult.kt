package net.skullian.chatsh.dispatcher

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
enum class DispatchResult {
    /** A regular cmd */
    NORMAL,
    /** Input was expanded into several commands */
    EXPANDED,
    /** chat.sh command, server did not receive anything */
    HANDLED_BUILTIN,
    /** failed / cancelled expansion */
    CANCELLED
}
