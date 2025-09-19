package net.dividebytoaster.ToastersStackSizeMod.mixins;

/// Imports - Java
import java.lang.Integer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

/// Imports - Utility
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;

/// Imports - Minecraft
import net.dividebytoaster.ToastersStackSizeMod.ModMain;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeyedValue;

/// Imports - Mixins - Opcodes
import org.objectweb.asm.Opcodes;

/// Imports - Mixins - Injections
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Imports - Local
import static net.dividebytoaster.ToastersStackSizeMod.ModMain.*;

/**********************************************************************************************************************\
> Mod Helpers
\**********************************************************************************************************************/

@Mixin (Item.class)
public abstract class ItemMixin
{
    // Various private variable fields for an `Item.Settings` object
    @Unique
    private static final HashMap<String, Field> SETTINGS_FIELDS = settingsFields();

    // Hard-coded fields for an `Item.Settings` object, used within this mixin class
    @Unique
    private static HashMap<String, Field> settingsFields()
    {
        HashMap<String, Field> fields = new HashMap<>();

        // `Item.Settings`
        {
            Field[] fieldsSettings = Item.Settings.class.getDeclaredFields();

            // `components`
            fields.put("components", fieldsSettings[2]);

            // `component.component`
            {
                Field[] fieldsComponents = fields.get("components").getType().getDeclaredFields();
                fields.put("components.components", fieldsComponents[0]);
            }

            // `translationKey`
            fields.put("translationKey", fieldsSettings[6]);

            // `translationKey`
            fields.put("registryKey", fieldsSettings[5]);
        }

        // Return the map of fields
        return fields;
    }

    // Caches any stackable item into `ITEMS` for the GUI settings list
    @Unique
    private void addItemToCache(boolean hasDamage)
    {
        if (!hasDamage)
            ITEMS.put(translationKey, (Item) (Object) this);
    }

    // Abstract method to override certain items' max counts
    @Unique
    protected Integer getNewMaxCount() { return null; }

/**********************************************************************************************************************\
> Code Injections - Variables and Methods
\**********************************************************************************************************************/

    @Shadow @Final @Mutable
    public static int MAX_MAX_COUNT = ModMain.getMaxCount();

    @Shadow @Final @Mutable
    protected String translationKey;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;translationKey:Ljava/lang/String;", opcode = Opcodes.PUTFIELD), method = "<init>")
    private void init(Item.Settings settings, CallbackInfo info)
    {
        setTranslationKey(settings);

        // Attempt to update the max stack size
        try
        {
            boolean hasDamage = hasDamage(settings);
            setNewMaxCount(hasDamage, settings);
            addItemToCache(hasDamage);
        }

        // Log an error if fields had issues
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            LOGGER.error("Failed to modify stack size for {} ({}): {}", this, translationKey, e);
        }
    }

/**********************************************************************************************************************\
> Code Injections - Helper Methods
\**********************************************************************************************************************/

    // Given an `Item.Settings` object, set the `Item.translationKey` field
    @Unique
    private void setTranslationKey(Item.Settings settings)
    {
        String name = "";
        try
        {
            Field field;

            field = SETTINGS_FIELDS.get("translationKey");
            RegistryKeyedValue<Item, String> translationKey = (RegistryKeyedValue<Item, String>) getPrivateAttribute(settings, field);
            if (translationKey == null) return;

            field = SETTINGS_FIELDS.get("registryKey");
            RegistryKey<Item> registryKey = (RegistryKey<Item>) getPrivateAttribute(settings, field);
            if (registryKey == null) return;

            name = (String) translationKey.get((RegistryKey) Objects.requireNonNull(registryKey, "Item id not set"));
        }

        // Log an error if fields had issues
        catch (IllegalAccessException e)
        {
            LOGGER.error("Failed to obtain name of {}: {}", this, e);
        }

        // Assign the name before moving on
        finally
        {
            translationKey = name;
        }
    }

    // Returns `true` if an item has the `DAMAGE` component, which prevents it from being stacked
    @Unique
    private static boolean hasDamage(Item.Settings settings)
    throws NoSuchFieldException, IllegalAccessException
    {
        Field field;

        // Get `settings.component`
        field = SETTINGS_FIELDS.get("components");
        ComponentMap.Builder builder = (ComponentMap.Builder) getPrivateAttribute(settings, field);
        if (builder == null) return true;

        // Get `settings.component.component`
        field = SETTINGS_FIELDS.get("components.components");
        Reference2ObjectMap<ComponentType<?>, Object> components = (Reference2ObjectMap<ComponentType<?>, Object>) getPrivateAttribute(builder, field);
        if (components == null) return true;

        // Extract some values from `settings.component.component` for requirement-checking
        // int max_stack_size = (int) components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
        // int damage         = (int) components.getOrDefault(DataComponentTypes.DAMAGE, 0);

        // Return if the settings contains damage
        return components.containsKey(DataComponentTypes.DAMAGE);
    }

    // Assigns the item's stack count based off of various conditions
    @Unique
    private void setNewMaxCount(boolean hasDamage, Item.Settings settings)
    {
        // Determine the item's max count from its ID
        Integer new_max_count;
        if (CONFIG.containsKey(translationKey) && CONFIG.get(translationKey) != null)
        {
            new_max_count = Integer.min(CONFIG.get(translationKey), MAX_MAX_COUNT);
        }
        else
        {
            switch (translationKey)
            {
            ////// 1 → 4
                // Buckets
                case "item.minecraft.lava_bucket":
                case "item.minecraft.milk_bucket":
                case "item.minecraft.powder_snow_bucket":
                case "item.minecraft.water_bucket":
                // Cake
                case "block.minecraft.cake":
                // Enchanted Books
                case "item.minecraft.enchanted_book":
                // Goat Horns
                case "item.minecraft.goat_horn":
                // Potions
                case "item.minecraft.lingering_potion":
                case "item.minecraft.potion":
                case "item.minecraft.splash_potion":
                // Saddle
                case "item.minecraft.saddle":
                // Soups and Stews
                case "item.minecraft.beetroot_soup":
                case "item.minecraft.mushroom_stew":
                case "item.minecraft.rabbit_stew":
                case "item.minecraft.suspicious_stew":
                {
                    new_max_count = 4;
                    break;
                }

            ////// 16 → 64
                // Honey Bottle
                case "item.minecraft.honey_bottle":
                // Snowball
                case "item.minecraft.snowball":
                {
                    new_max_count = 64;
                    break;
                }

                // Non-Trivial
                default:
                {
                ////// Pre-Defined
                    new_max_count = getNewMaxCount();
                    if (new_max_count != null) break;

                ////// 1 → 4
                    if
                    (
                        // Horse Armor
                        translationKey.matches("^item\\.minecraft\\.[a-zA-Z0-9_]+_horse_armor$")
                            // Banner Patterns
                            || translationKey.matches("^item\\.minecraft\\.[a-zA-Z0-9_]+_banner_pattern$")
                            // Music Discs
                            || translationKey.matches("^item\\.minecraft\\.music_disc_[a-zA-Z0-9_]+$")
                    )
                    {
                        new_max_count = 4;
                        break;
                    }

                ////// Unchanged
                    return;
                }
            }
        }

        // Check the requirements, updating the max stack size if successful
        if (!hasDamage)
            settings.maxCount(new_max_count);
    }
}
