package net.skullian.chatsh.expansion.brig

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.context.StringRange
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.skullian.chatsh.ChatSh
import net.skullian.chatsh.expansion.ChatExpander
import net.skullian.chatsh.expansion.brig.ExpansionFormatter
import net.skullian.chatsh.mixin.accessor.CommandSuggestionsAccessor
import net.skullian.chatsh.mixin.accessor.EditBoxAccessor
import java.util.concurrent.CompletableFuture

/**
 * handles glob suggestion injections, lambdas + jvm make mixins die when defined in mixins
 */
internal object GlobSuggestionInjector {

    //? if <1.21.11 {
    fun install(input: EditBox, acc: EditBoxAccessor) {
        val prev = acc.formatter
        input.setFormatter(java.util.function.BiFunction { s, i -> ExpansionFormatter.format(s, i) ?: prev.apply(s, i) })
    }
    //?}

    fun start(
        raw: String,
        cursorPos: Int,
        currentToken: String,
        suffixLen: Int,
        dispatcher: CommandDispatcher<SuggestionProvider>,
        provider: SuggestionProvider,
        accessor: CommandSuggestionsAccessor,
        input: EditBox,
        commandSuggestions: CommandSuggestions,
    ): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val expansions = try {
                ChatExpander.expand(raw, ChatSh.rootContext).commands
                    .map { it.trimStart() }.filter { it.isNotEmpty() }
            } catch (_: Exception) { return@supplyAsync null }

            val expandedRaw = expansions.firstOrNull() ?: return@supplyAsync null
            if (expandedRaw.trim() == raw.trim()) return@supplyAsync null

            val expanded = expandedRaw.removePrefix("/")
            val slashOffset = if (expandedRaw.startsWith("/")) 1 else 0
            val cursor = (expandedRaw.length - slashOffset - suffixLen).coerceAtLeast(0)

            val parse = dispatcher.parse(StringReader(expanded), provider)
            val future = dispatcher.getCompletionSuggestions(parse, cursor)
            if (!future.isDone) return@supplyAsync null

            val suggestions = runCatching { future.get() }.getOrNull() ?: return@supplyAsync null
            if (suggestions.isEmpty) return@supplyAsync null

            val rawRange = StringRange(cursorPos - currentToken.length, cursorPos)
            Suggestions(rawRange, suggestions.list.map { sug ->
                Suggestion(rawRange, sug.text, sug.tooltip)
            })
        }.thenAcceptAsync({ remapped ->
            if (remapped == null || input.value != raw) return@thenAcceptAsync
            accessor.pendingSuggestions = CompletableFuture.completedFuture(remapped)
            commandSuggestions.showSuggestions(false)
            BrigadierCtx.update(accessor)
        }, Minecraft.getInstance())
    }
}
