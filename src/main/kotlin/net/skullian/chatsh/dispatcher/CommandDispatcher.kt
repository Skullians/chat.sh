package net.skullian.chatsh.dispatcher

import net.minecraft.client.Minecraft
import net.skullian.chatsh.expansion.ChatExpander
import net.skullian.chatsh.expansion.data.ExpansionCtx

class CommandDispatcher(
    private val ctx: ExpansionCtx
) {

    fun dispatch(raw: String): DispatchResult {
        val client = Minecraft.getInstance()
        val input = raw.trim()

        val history = expandHistory(input)

        if (history != input) {
            client.prefixed(
                HISTORY_EXPANDING,
                "commands" to history
            )
        }

        val result = ChatExpander.expand(history, ctx)

        result.errors.forEach { error ->
            client.prefixed(error)
        }

        if (result.commands.isEmpty())
            return DispatchResult.CANCELLED

        if (result.commands.size > MAX_COMMANDS) {
            client.prefixed(
                TOO_MANY_COMMANDS,
                "count" to result.commands.size,
                "max" to MAX_COMMANDS
            )
            return DispatchResult.CANCELLED
        }

        if (result.commands.size > 1) {
            client.prefixed(
                COMMAND_SENDING,
                "count" to result.commands.size
            )
        }

        result.commands.forEach { cmd ->
            client.sendCommand(cmd)
        }

        return if (result.commands.size > 1)
            DispatchResult.EXPANDED
        else
            DispatchResult.NORMAL
    }

    private fun expandHistory(input: String): String {
        if (!input.startsWith("!")) return input

        return when {
            input == "!!" ->
                ctx.commandHistory.lastOrNull() ?: input

            input.length > 1 -> {
                val prefix = input.substring(1)
                ctx.commandHistory.lastOrNull { it.startsWith(prefix) } ?: input
            }

            else -> input
        }
    }

    companion object {

        private const val MAX_COMMANDS = 50

        private const val HISTORY_EXPANDING =
            "<gray>Expanding:</gray> <commands>"

        private const val COMMAND_SENDING =
            "<gray>Sending <white><count></white> commands...</gray>"

        private const val TOO_MANY_COMMANDS =
            "<red>Expansion produced <count> commands (max <max>). Aborted.</red>"
    }
}
