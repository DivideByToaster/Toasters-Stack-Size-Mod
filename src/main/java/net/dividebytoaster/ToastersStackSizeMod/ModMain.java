package net.dividebytoaster.ToastersStackSizeMod;

/// Imports - Java
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/// Imports - Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Imports - JSON
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/// Imports - Minecraft
import net.minecraft.item.Item;

/// Imports - Fabric
import net.fabricmc.loader.api.FabricLoader;

/// Imports - Annotations
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

/**********************************************************************************************************************\
> Properties
\**********************************************************************************************************************/

public class ModMain
implements net.fabricmc.api.ModInitializer
{
    /// Nothing to do (all mixins)!
    @Override
    public void onInitialize()
    {}

    /// Standard Mod Stuff
    public static final String MOD_ID = "toasters-stack-size-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /// Mod Helpers
    public static final Map<String, Item>    ITEMS  = new HashMap<>(); // Populated by ItemMixIn
    public static final Map<String, Integer> CONFIG = initConfig();

    /// Config Helper Variables
    public  static final String  CONFIG_MAX_KEY     = "MAX";
    private static final int     CONFIG_MAX_DEFAULT = (1 << 8);
    private static       boolean CONFIG_ERROR;

    // Returns `true` if an error occurred while loading `CONFIG`
    public static boolean hadConfigError() { return CONFIG_ERROR; }

    // Returns the maximum possible item stack size
    public static int getMaxCount()
    {
        if (CONFIG_ERROR || !CONFIG.containsKey(CONFIG_MAX_KEY))
            return CONFIG_MAX_DEFAULT;
        return CONFIG.get(CONFIG_MAX_KEY);
    }

/**********************************************************************************************************************\
> Setup Methods
\**********************************************************************************************************************/

    @Unique
    private static File getConfigFile()
    {
        Path config_dir = FabricLoader.getInstance().getConfigDir().resolve("dividebytoaster");
        if (config_dir.toFile().mkdirs())
        {
            LOGGER.debug("Created config directory: {}", config_dir);
        }

        // Open the config file, creating one if needed
        return config_dir.resolve("stack-size.json").toFile();
    }

    @Unique
    private static HashMap<String, Integer> initConfig()
    {
        HashMap<String, Integer> config = new HashMap<>();

        // Open the config file, creating one if needed
        File    configFile = getConfigFile();
        Boolean created    = null;
        try
        {
            created = configFile.createNewFile();

            ObjectMapper mapper = new ObjectMapper();
            if (created)
            {
                config.put(CONFIG_MAX_KEY, CONFIG_MAX_DEFAULT);
                mapper.writeValue(configFile, config);
            }
            else
            {
                MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
                config = mapper.readValue(configFile, type);
                if (!config.containsKey(CONFIG_MAX_KEY))
                {
                    config.put(CONFIG_MAX_KEY, CONFIG_MAX_DEFAULT);
                    mapper.writeValue(configFile, config);
                }
            }
            CONFIG_ERROR = false;
        }
        catch (IOException e)
        {
            if (created == null)
            {
                LOGGER.error("Failed to create config file: {}", configFile);
            }
            else if (created)
            {
                LOGGER.error("Failed to initialize config file: {}", configFile);
            }
            else
            {
                LOGGER.error("Failed to parse JSON object from config file: {}", configFile);
            }
            CONFIG_ERROR = true;
        }

        // If a config was provided and no errors occurred, validate it
        if (!config.isEmpty() && !CONFIG_ERROR)
        {
            final int MAX_MAX_COUNT = config.get(CONFIG_MAX_KEY);

            // Compare each max count against the global max count
            boolean modified = false;
            for (String key : config.keySet())
            {
                if (config.get(key) > MAX_MAX_COUNT)
                {
                    LOGGER.debug("Adjusted Value ({}): {}, was {}", key, MAX_MAX_COUNT, config.get(key));
                    config.put(key, MAX_MAX_COUNT);
                    modified = true;
                }
            }

            // If the data was invalid at a point, adjust it and save it back
            if (modified) updateConfigFile(config);
        }

        // Return the map
        LOGGER.debug("Imported Config: {}", config);
        return config;
    }

    @Unique
    public static void updateConfigFile(Map<String, Integer> config)
    {
        File configFile = getConfigFile();
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(configFile, config);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to update config file: {}", configFile);
        }
    }

    @Unique
    public static void updateConfigFile() { updateConfigFile(CONFIG); }

/**********************************************************************************************************************\
> Helper Methods
\**********************************************************************************************************************/

    // Illegally obtain an object's private attribute value
    @Unique
    public static Object getPrivateAttribute(@NotNull Object object, @NotNull Field field)
    throws IllegalAccessException
    {
        try
        {
            field.setAccessible(true);
            return field.get(object);
        }
        finally
        {
            field.setAccessible(false);
        }
    }

    // Illegally assigns an object's private attribute value
    @Unique
    public static void setPrivateAttribute(@NotNull Object object, @NotNull Field field, Object value)
    throws IllegalAccessException
    {
        try
        {
            field.setAccessible(true);
            field.set(object, value);
        }
        finally
        {
            field.setAccessible(false);
        }
    }

    // Print a type's private attributes (for debugging purposes)
    @Unique
    public static <T> void printAttributes(Class<T> c)
    {
        Field[] fields = c.getDeclaredFields();
        String  format = "%" + String.valueOf(String.valueOf(fields.length - 1).length()) + "d";
        LOGGER.info("{}", c.getName());
        for (int i = 0; i < fields.length; ++i)
            LOGGER.info("{}: {}", String.format(format, i), fields[i]);
    }
}
