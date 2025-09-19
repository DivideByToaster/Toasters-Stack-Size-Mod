package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.BedItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (BedItem.class)
public abstract class BedItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 4; }
}
