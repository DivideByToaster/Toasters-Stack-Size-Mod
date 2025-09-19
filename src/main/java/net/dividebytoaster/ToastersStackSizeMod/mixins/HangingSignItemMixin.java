package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.HangingSignItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (HangingSignItem.class)
public abstract class HangingSignItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 64; }
}
