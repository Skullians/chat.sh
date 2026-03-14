package net.skullian.chatsh.expansion.brig

import com.mojang.brigadier.ParseResults

//? if >=1.21.8 {
/*import net.minecraft.client.multiplayer.ClientSuggestionProvider
typealias SuggestionProvider = ClientSuggestionProvider
*///?} else {
import net.minecraft.commands.SharedSuggestionProvider
typealias SuggestionProvider = SharedSuggestionProvider
//?}

fun ParseResults<SuggestionProvider>.isExecutable(): Boolean {
    var ctx = context
    while (true) {
        if (ctx.command != null) return true
        ctx = ctx.child ?: return false
    }
}

fun ParseResults<SuggestionProvider>.succeeded(): Boolean =
    (!reader.canRead() || reader.remaining.isBlank()) && isExecutable()