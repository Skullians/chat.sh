package net.skullian.chatsh.expansion

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
object ExpansionRegistry {
    private val expanders = mutableListOf<Expander>()

    fun register(expander: Expander) {
        expanders.add(expander)
        expanders.sortBy { it.priority }
    }

    fun unregister(id: String) {
        expanders.removeIf { it.id == id }
    }

    fun get(): List<Expander> = expanders.toList()
}
