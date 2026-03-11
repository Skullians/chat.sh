package net.skullian.chatsh.expansion

import net.skullian.chatsh.expansion.data.ExpansionCtx

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
interface Expander {
    val id: String
    /** The expander with the lowest priority runs first, etc **/
    val priority: Int

    fun expand(inputs: List<String>, ctx: ExpansionCtx): List<String>
}
