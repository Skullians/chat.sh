package net.skullian.chatsh.expansion.brig

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import net.skullian.chatsh.mixin.accessor.CommandSuggestionsAccessor
import java.util.concurrent.CompletableFuture

/**
 * This is a class.
 *
 * @author Skullians
 * @since 12/03/2026
 */
object BrigadierCtx {

    @Volatile private var commandUsage: List<*> = emptyList<Any>()
    @Volatile private var currentParse: ParseResults<ClientSuggestionProvider>? = null
    @Volatile private var pendingSuggestions: CompletableFuture<Suggestions>? = null

    fun update(accessor: CommandSuggestionsAccessor) {
        currentParse = accessor.currentParse
        pendingSuggestions = accessor.pendingSuggestions
        commandUsage = accessor.commandUsage
    }

    fun clear() {
        currentParse = null
        pendingSuggestions = null
        commandUsage = emptyList<Any>()
    }

    fun getCompletions(): Suggestions? {
        val future = pendingSuggestions ?: return null
        if (!future.isDone) return null
        return future.get()
    }

    fun getCompletionsAt(cursorPos: Int): Suggestions? {
        val parse = currentParse ?: return null
        val dispatcher = Minecraft.getInstance().connection?.commands ?: return null
        val future = dispatcher.getCompletionSuggestions(parse, cursorPos)
        if (!future.isDone) return null
        return future.get()
    }

    fun getSuggestionsListHeight(): Int {
        val future = pendingSuggestions?.takeIf { it.isDone } ?: return 0
        val suggestions = runCatching { future.get() }.getOrNull() ?: return 0
        if (!suggestions.isEmpty) {
            val visibleRows = minOf(suggestions.list.size, 10)
            return visibleRows * 12
        }
        // no suggestions, but usage/err lines shown instead
        return commandUsage.size * 12
    }

    fun getParse(): ParseResults<ClientSuggestionProvider>? = currentParse
}