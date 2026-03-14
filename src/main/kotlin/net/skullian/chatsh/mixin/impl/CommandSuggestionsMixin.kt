package net.skullian.chatsh.mixin.impl

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.skullian.chatsh.ChatSh
import net.skullian.chatsh.expansion.ChatExpander
import net.skullian.chatsh.expansion.ChatExpander.containsGlob
import net.skullian.chatsh.expansion.ChatExpander.hasShellSyntax
import net.skullian.chatsh.expansion.brig.ExpansionFormatter
import net.skullian.chatsh.expansion.brig.GlobSuggestionInjector
import net.skullian.chatsh.expansion.brig.isExecutable
import net.skullian.chatsh.mixin.accessor.CommandSuggestionsAccessor
import net.skullian.chatsh.mixin.accessor.EditBoxAccessor
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.concurrent.CompletableFuture

@Mixin(CommandSuggestions::class)
abstract class CommandSuggestionsMixin {

    @Shadow lateinit var input: EditBox
    @Shadow @Final private val fillColor: Int = 0
    @Shadow private val commandUsage: MutableList<FormattedCharSequence> = mutableListOf()

    @Unique private var injectionTask: CompletableFuture<*>? = null

    @Inject(method = ["<init>"], at = [At("TAIL")])
    private fun onInit(ci: CallbackInfo) {
        //? if >=1.21.11 {
        /*(input as EditBoxAccessor).getFormatters().add(0, ExpansionFormatter)
        *///?} else {
        val acc = input as EditBoxAccessor
        GlobSuggestionInjector.install(input, acc)
        //?}
    }

    @Inject(method = ["updateCommandInfo"], at = [At("RETURN")])
    private fun onUpdateCommandInfo(ci: CallbackInfo) {
        if (input.value.hasShellSyntax()) {
            commandUsage.clear()
        }
        injectGlobSuggestions()
    }

    private fun injectGlobSuggestions() {
        val raw = input.value
        if (!raw.containsGlob()) return

        val accessor = this as CommandSuggestionsAccessor
        if (accessor.keepSuggestions) return

        val cursorPos = input.cursorPosition

        val lastSpace = raw.lastIndexOf(' ', cursorPos - 1)
        val currentToken = raw.substring(if (lastSpace < 0) 0 else lastSpace + 1, cursorPos)
        if (currentToken.containsGlob()) return

        val connection = Minecraft.getInstance().connection ?: return
        val dispatcher = connection.commands
        val provider = connection.suggestionsProvider
        val suffixLen = raw.length - cursorPos

        // cancel any ongoing task
        injectionTask?.cancel(false)

        // workaround for mixins to not die with lambdas
        injectionTask = GlobSuggestionInjector.start(
            raw, cursorPos, currentToken, suffixLen,
            dispatcher, provider,
            accessor, input, this as CommandSuggestions
        )
    }

    @Inject(method = ["renderUsage"], at = [At("HEAD")], cancellable = true)
    private fun onRenderUsage(graphics: GuiGraphics, ci: CallbackInfo) {
        val raw = input.value
        if (!raw.hasShellSyntax()) return

        ci.cancel()

        val connection = Minecraft.getInstance().connection ?: return
        val dispatcher = connection.commands
        val provider = connection.suggestionsProvider

        val commands = ChatExpander.expand(raw, ChatSh.rootContext).commands
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (commands.size < 2) return

        val results: List<Pair<String, String?>> = commands.map { cmd ->
            val parse = dispatcher.parse(cmd.removePrefix("/"), provider)
            val error: String? = when {
                parse.reader.canRead() && !parse.reader.remaining.isBlank() ->
                    "Unexpected: '${parse.reader.remaining.take(16)}'"
                !parse.isExecutable() -> "Incomplete command"
                else -> null
            }
            cmd to error
        }

        val invalid = results.filter { it.second != null }
        if (invalid.isEmpty()) return

        val mc = Minecraft.getInstance()
        val font = mc.font

        val lines = mutableListOf<FormattedCharSequence>()
        lines += FormattedCharSequence.forward(
            "[chat.sh] ${invalid.size}/${commands.size} expansions invalid",
            Style.EMPTY.withColor(ChatFormatting.GRAY)
        )
        for ((cmd, error) in invalid.take(5)) {
            val label = if (cmd.length > 28) cmd.take(25) + "..." else cmd
            lines += FormattedCharSequence.forward(
                "  $label  →  $error",
                Style.EMPTY.withColor(ChatFormatting.RED)
            )
        }
        if (invalid.size > 5) {
            lines += FormattedCharSequence.forward(
                "  ... and ${invalid.size - 5} more",
                Style.EMPTY.withColor(ChatFormatting.DARK_RED)
            )
        }

        val maxWidth = lines.maxOf { font.width(it) }
        val x = 2
        val baseY = (mc.screen?.height ?: return) - 14 - 13

        lines.forEachIndexed { idx, line ->
            val y = baseY - 12 * (lines.size - idx)
            graphics.fill(x - 1, y, x + maxWidth + 1, y + 12, fillColor)
            graphics.drawString(font, line, x, y + 2, -1)
        }
    }
}
