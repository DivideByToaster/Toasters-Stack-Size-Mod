package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.inventory.Inventory;

/// Imports - Mixins
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Imports - Local
import net.dividebytoaster.ToastersStackSizeMod.ModMain;

@Mixin (Inventory.class)
public interface InventoryMixIn
{
    @ModifyReturnValue (at = @At("RETURN"), method = "getMaxCountPerStack")
    default int getMaxCountPerStack(int original) { return ModMain.getMaxCount(); }
}
