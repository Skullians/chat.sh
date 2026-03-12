package net.skullian.chatsh.mixin.accessor

import net.minecraft.client.gui.components.EditBox
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

/**
 * This is a class.
 *
 * @author Skullians
 * @since 12/03/2026
 */
@Mixin(EditBox::class)
interface EditBoxAccessor {
    @Accessor("formatters")
    fun getFormatters(): MutableList<EditBox.TextFormatter>
}
