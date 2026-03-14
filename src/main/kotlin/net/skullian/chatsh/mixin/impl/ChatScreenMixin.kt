package net.skullian.chatsh.mixin.impl

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.Screen
import net.skullian.chatsh.ChatSh
import net.skullian.chatsh.dispatcher.DispatchResult
import net.skullian.chatsh.expansion.ChatExpander.hasShellSyntax
import net.skullian.chatsh.expansion.brig.BrigadierCtx
import net.skullian.chatsh.mixin.accessor.CommandSuggestionsAccessor
import net.skullian.chatsh.screen.ExpansionRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
@Mixin(ChatScreen::class)
abstract class ChatScreenMixin {

    @Shadow
    protected lateinit var input: EditBox
    @Shadow
    lateinit var commandSuggestions: CommandSuggestions

    @Suppress("CAST_NEVER_SUCCEEDS")
    private val asScreen get() =
        this as Screen

    @Inject(
        method = ["handleChatInput"],
        at = [At("HEAD")],
        cancellable = true
    )
    fun onMessage(message: String, addToHistory: Boolean, ci: CallbackInfo) {
        if (!message.hasShellSyntax()) return

        when (val result = ChatSh.rootDispatcher.dispatch(message)) {
            DispatchResult.NORMAL -> {
                ci.cancel()
                Minecraft.getInstance().setScreen(null)
            }
            DispatchResult.EXPANDED -> {
                ci.cancel()

                if (addToHistory) Minecraft.getInstance().gui.chat.addRecentChat(message) // still add to cmd history
                Minecraft.getInstance().setScreen(null)
            }
            DispatchResult.HANDLED_BUILTIN,
            DispatchResult.CANCELLED -> {
                ci.cancel()
                if (result != DispatchResult.CANCELLED) {
                    Minecraft.getInstance().setScreen(null)
                }
            }
        }
    }

    @Inject(
        method = ["onEdited"],
        at = [At("RETURN")]
    )
    private fun onEdited(text: String, ci: CallbackInfo) {
        val accessor = commandSuggestions as CommandSuggestionsAccessor
        BrigadierCtx.update(accessor)
    }

    @Inject(
        method = ["render"],
        at = [At("HEAD")]
    )
    private fun onRender(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float, ci: CallbackInfo) {
        ExpansionRenderer.render(graphics, input.value, asScreen.width, asScreen.height)
    }

    @Inject(
        method = ["removed"],
        at = [At("RETURN")]
    )
    private fun onRemoved(ci: CallbackInfo) {
        BrigadierCtx.clear()

        //? if >=1.21.11 {
        if (input.value.hasShellSyntax()) { // temporary fix that will be permanent
            Minecraft.getInstance().gui.chat.discardDraft()
        }
        //?}
    }
}
