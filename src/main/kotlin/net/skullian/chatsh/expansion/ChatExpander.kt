package net.skullian.chatsh.expansion

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.chatsh.expansion.data.ExpansionResult
import net.skullian.chatsh.expansion.exception.ExpansionException
import java.util.concurrent.TimeUnit

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
object ChatExpander {

    private val cache: Cache<String, ExpansionResult> = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build()

    fun expand(input: String, ctx: ExpansionCtx = ExpansionCtx()): ExpansionResult {
        ctx.commandHistory.add(input)

        if (input.isBlank()) return ExpansionResult(listOf(input))
        if (input.contains('$')) return compute(input, ctx)

        return cache.get(input) { compute(it, ctx) }
    }

    fun invalidate() = cache.invalidateAll()

    fun canExpand(input: String): Boolean {
        if (input.isBlank()) return false
        return ExpansionRegistry.get().any { it.canExpand(input) }
    }

    private fun compute(input: String, ctx: ExpansionCtx): ExpansionResult {
        val errors = mutableListOf<String>()
        var inputs = listOf(input)

        for (expander in ExpansionRegistry.get()) {
            try {
                inputs = expander.expand(inputs, ctx)
            } catch (err: ExpansionException) {
                errors.add("${expander.id} // ${err.message}")
            } catch (err: Exception) {
                errors.add("${expander.id} // Unknown error - ${err.message}")
            }
        }

        return ExpansionResult(inputs.distinct(), errors)
    }
}
