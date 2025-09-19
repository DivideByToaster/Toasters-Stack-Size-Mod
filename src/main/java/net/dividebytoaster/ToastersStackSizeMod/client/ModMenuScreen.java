package net.dividebytoaster.ToastersStackSizeMod.client;

/// Imports - Minecraft
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

/// Imports - Local
import static net.dividebytoaster.ToastersStackSizeMod.client.ModMenu.CLIENT;
import net.dividebytoaster.ToastersStackSizeMod.client.widgets.SettingsList;
import static net.dividebytoaster.ToastersStackSizeMod.client.widgets.ScrollBar.Cache;

public class ModMenuScreen
extends Screen
{
    protected final Screen parent;

    protected TextWidget   titleLabel;
    protected SettingsList settings;
    protected ButtonWidget buttonDone;
    protected TextWidget   iconLabel;

    protected final Cache settingsCache;

    public static boolean modified = false;

    public ModMenuScreen(Screen parent)
    {
        super(Text.of("Waa"));
        this.parent = parent;
        settingsCache = new Cache();
    }

    @Override
    protected void init()
    {
        titleLabel_init();
        iconLabel_init();
        buttonDone_init();
        settings_init();
    }

    private void titleLabel_init()
    {
        titleLabel = new TextWidget(Text.literal("Stack Size Options"), CLIENT.textRenderer);

        int width = titleLabel.getWidth();
        titleLabel.setPosition((this.width - width) / 2, 12);

        addDrawableChild(titleLabel);
    }

    private void iconLabel_init()
    {
        iconLabel = new TextWidget(Text.literal("Relaunch the game to apply changes."), CLIENT.textRenderer);
        iconLabel.setTextColor(0xFFFFFF00);

        int x = (width - iconLabel.getWidth()) / 2;
        int y = (titleLabel.getY() + titleLabel.getHeight() + 10 + SettingsList.ADD_HEIGHT) + 1
              + ((SettingsList.ROW_OFFSET - SettingsList.ADD_HEIGHT - iconLabel.getHeight()) / 2);
        iconLabel.setPosition(x, y);

        addDrawableChild(iconLabel);
    }

    private void buttonDone_init()
    {
        int width = (200 + (this.width % 2));
        int height = 20;

        int x = (this.width - width) / 2;
        int y = this.height - height - 6;

        ButtonWidget.Builder builder = ButtonWidget.builder
        (
            Text.literal("Done"),
            (buttonWidget) -> close()
        );
        builder.dimensions(x, y, width, height);
        buttonDone = builder.build();
        buttonDone.setNavigationOrder(Integer.MAX_VALUE);

        addDrawableChild(buttonDone);
    }

    private void settings_init()
    {
        int x = 0;
        int y = titleLabel.getY() + titleLabel.getHeight() + 10;

        int width = this.width;
        int height = buttonDone.getY() - y - 6;

        settings = new SettingsList(this, settingsCache, x, y, width, height);

        addDrawableChild(settings);
        for (ClickableWidget widget : settings.widgetsNew)
            addDrawableChild(widget);
    }

    @Override
    public void close()
    {
        CLIENT.setScreen(parent);
        settingsCache.clear();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        return settings.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        settings.refresh();
        iconLabel.visible = modified;
        for (ClickableWidget widget : settings.widgetsOld)
            remove(widget);
        for (ClickableWidget widget : settings.widgetsNew)
            addDrawableChild(widget);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
};
