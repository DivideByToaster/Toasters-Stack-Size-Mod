package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.BoatItem;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;

@Mixin (BoatItem.class)
public abstract class BoatItemMixin
extends ItemMixin
{
    @Override
    protected Integer getNewMaxCount() { return 4; }
}
