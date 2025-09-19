package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Minecraft
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/// Imports - Mixins
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.*;

/// Imports - Local
import net.dividebytoaster.ToastersStackSizeMod.ModMain;

@Mixin (ItemStack.class)
public abstract class ItemStackMixIn
{
    @Shadow @Final @Mutable
    public static MapCodec<ItemStack> MAP_CODEC = MapCodec.recursive
    (
        "ItemStack",
        (Codec<ItemStack> codec) ->
        RecordCodecBuilder.mapCodec
        (
            (instance) ->
            instance.group
            (
                Item.ENTRY_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry),
                Codecs.rangedInt(1, ModMain.getMaxCount()).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter
                (
                    (stack) ->
                    {
                        return ((MergedComponentMap) ((ItemStack) stack).getComponents()).getChanges();
                    }
                )
            )
            .apply(instance, ItemStack::new)
        )
    );

    @Shadow @Final @Mutable
    public static Codec<ItemStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
}
