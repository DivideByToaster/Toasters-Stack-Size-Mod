package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.EggItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (EggItem.class)
public abstract class EggItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 64; }
}
