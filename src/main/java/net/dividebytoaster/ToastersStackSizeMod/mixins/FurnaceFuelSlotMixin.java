package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.FurnaceFuelSlot;

/// Imports - Mixins
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (FurnaceFuelSlot.class)
public class FurnaceFuelSlotMixin
{
    @Inject(at = @At("RETURN"), method = "isBucket", cancellable = true)
    private static void isBucket(ItemStack stack, CallbackInfoReturnable<Boolean> infoReturn)
    {
        if (stack.isOf(Items.LAVA_BUCKET))
            infoReturn.setReturnValue(true);
    }
}
