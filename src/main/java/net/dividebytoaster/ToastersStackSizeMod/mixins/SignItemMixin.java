package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.SignItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (SignItem.class)
public abstract class SignItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 64; }
}
