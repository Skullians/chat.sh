package net.skullian.chatsh.mixin.accessor

import net.minecraft.client.gui.components.EditBox
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EditBox::class)
interface EditBoxAccessor {
    //? if >=1.21.11 {
    @Accessor("formatters")
    fun getFormatters(): MutableList<EditBox.TextFormatter>
    //?} else {
    /*@get:Accessor("formatter")
    val formatter: java.util.function.BiFunction<String, Int, FormattedCharSequence>
    *///?}
}