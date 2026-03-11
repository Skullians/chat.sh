package net.skullian.chatsh.dispatcher

import net.minecraft.client.Minecraft
import net.skullian.chatsh.util.Adventure.formatted
import net.kyori.adventure.text.Component

fun Minecraft.feedback(message: String) =
    this.player?.sendMessage(message.formatted)

fun Minecraft.feedback(message: String, vararg placeholders: Pair<String, Any?>) =
    this.player?.sendMessage(message.formatted(*placeholders))

fun Minecraft.prefixed(message: String) =
    this.player?.sendMessage("<red>[chat.sh]</red> $message".formatted)

fun Minecraft.prefixed(message: String, vararg placeholders: Pair<String, Any?>) =
    this.player?.sendMessage("<red>[chat.sh]</red> $message".formatted(*placeholders))

fun Minecraft.feedback(component: Component) =
    this.player?.sendMessage(component)

fun Minecraft.prefixed(component: Component) =
    this.player?.sendMessage("<red>[chat.sh]</red> ".formatted.append(component))

fun Minecraft.sendCommand(cmd: String) {
    val conn = connection ?: return
    if (cmd.startsWith("/")) conn.sendCommand(cmd.removePrefix("/"))
    else conn.sendChat(cmd)
}
