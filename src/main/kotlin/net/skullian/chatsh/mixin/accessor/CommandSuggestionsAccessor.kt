package net.skullian.chatsh.mixin.accessor

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.client.gui.components.CommandSuggestions
import net.skullian.chatsh.expansion.brig.SuggestionProvider
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import java.util.concurrent.CompletableFuture

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
@Mixin(CommandSuggestions::class)
interface CommandSuggestionsAccessor {

    @get:Accessor("pendingSuggestions")
    @set:Accessor("pendingSuggestions")
    var pendingSuggestions: CompletableFuture<Suggestions>?

    @get:Accessor("currentParse")
    val currentParse: ParseResults<SuggestionProvider>?

    @get:Accessor("commandUsage")
    val commandUsage: List<FormattedCharSequence>

    @get:Accessor("keepSuggestions")
    val keepSuggestions: Boolean

}

