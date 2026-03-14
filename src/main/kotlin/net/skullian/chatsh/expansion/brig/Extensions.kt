package net.skullian.chatsh.expansion.brig

import com.mojang.brigadier.ParseResults
import net.minecraft.client.multiplayer.ClientSuggestionProvider

fun ParseResults<ClientSuggestionProvider>.isExecutable(): Boolean {
    var ctx = context
    while (true) {
        if (ctx.command != null) return true
        ctx = ctx.child ?: return false
    }
}

fun ParseResults<ClientSuggestionProvider>.succeeded(): Boolean =
    (!reader.canRead() || reader.remaining.isBlank()) && isExecutable()