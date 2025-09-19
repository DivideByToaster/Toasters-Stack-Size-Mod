package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.MinecartItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (MinecartItem.class)
public abstract class MinecartItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 4; }
}
