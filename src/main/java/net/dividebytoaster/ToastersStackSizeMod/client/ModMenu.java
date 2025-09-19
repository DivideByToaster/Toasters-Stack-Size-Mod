package net.dividebytoaster.ToastersStackSizeMod.client;

/// Imports - Minecraft
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/// Imports - External - ModMenu
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenu
implements ModMenuApi
{
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory()
    {
        return ModMenuScreen::new;
    }
}
