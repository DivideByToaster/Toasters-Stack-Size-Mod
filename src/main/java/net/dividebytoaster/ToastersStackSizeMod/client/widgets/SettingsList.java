package net.dividebytoaster.ToastersStackSizeMod.client.widgets;

/// Imports - Java
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/// Imports - Minecraft
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import static net.minecraft.component.DataComponentTypes.DAMAGE;
import net.minecraft.text.Text;

/// Imports - Annotations
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/// Imports - Local
import static net.dividebytoaster.ToastersStackSizeMod.ModMain.*;
import static net.dividebytoaster.ToastersStackSizeMod.client.ModMenu.CLIENT;
import net.dividebytoaster.ToastersStackSizeMod.client.ModMenuScreen;

public class SettingsList
extends ScrollBar
{
    public static final int COLOR_DARKEN      = 0x7F000000;
    public static final int COLOR_DARKEN_MORE = 0xBF000000;

    public static final int ADD_HEIGHT = 30;
    public static final int ROW_OFFSET = 12 + ADD_HEIGHT;

    protected final ModMenuScreen parent;

    public    ArrayList<ClickableWidget> widgetsOld;
    public    ArrayList<ClickableWidget> widgetsNew;
    protected Row                        adder;
    protected ArrayList<Row>             rows;

    protected int x0;
    protected int width0;

    public SettingsList(@NotNull ModMenuScreen parent, @NotNull Cache cache, int x, int y, int width, int height)
    {
        super(cache, x + width - SCROLLBAR_WIDTH, y, SCROLLBAR_WIDTH, height);
        this.parent = parent;

        widgetsOld = new ArrayList<>();
        widgetsNew = new ArrayList<>();
        adder      = Row.forAdding(this);
        rows       = new ArrayList<>();

        widgetsNew.addAll(Arrays.asList(adder.button, adder.keyField, adder.valField));
        for (String key : CONFIG.keySet())
        {
            if (ITEMS.containsKey(key))
                rows.add(Row.forList(this, key));
        }

        x0     = x;
        width0 = width;
    }

/**********************************************************************************************************************\
> List Management Methods
\**********************************************************************************************************************/

    // Add a new row from the `adder` row's parameters
    protected void add()
    {
        if (adder.buttonInvalid())
            return;
        String  key = adder.keyField.getText();
        int     val = Integer.parseInt(adder.valField.getText());

        boolean exists = CONFIG.containsKey(key);
        CONFIG.put(key, val);
        updateConfigFile();
        if (exists)
        {
            for (Row row : rows)
            {
                if (key.equals(row.key))
                {
                    row.key = key;
                    break;
                }
            }
        }
        else
        {
            rows.add(Row.forList(this, key));
        }
        ModMenuScreen.modified = true;
    }

    // Remove an existing row
    protected void remove(Row row)
    {
        CONFIG.remove(row.keyField.getText());
        updateConfigFile();
        rows.remove(row);
        if (getScrollY() > getMaxScrollY())
            setScrollY(getMaxScrollY());
        ModMenuScreen.modified = true;
    }

/**********************************************************************************************************************\
> Helper Calculations
\**********************************************************************************************************************/

    // With the entire row bounds in mind, calculate how many rows from the list can fit on the screen
    protected int rowsPerScreen()
    {
        return ((height - ROW_OFFSET) / Row.BOUNDS);
    }

    // Return the total number of rows
    protected int rowCount()
    {
        return (rows == null ? 0 : rows.size());
    }

    // Return the total height for the list sub-window in the GUI
    protected int adjustedHeight()
    {
        return rowsPerScreen() * Row.BOUNDS;
    }

    // Return the scrollbar's width (hard-coded)
    protected int getScrollbarWidth()
    {
        return 8;
    }

    // Return the start of the scrollbar in the list sub-window
    protected int getScrollbarX()
    {
        return (this.getRight() - getScrollbarWidth());
    }

/**********************************************************************************************************************\
> Rendering Helper Methods
\**********************************************************************************************************************/

    // Initialize the `adder` row for rendering
    protected void renderAdder(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        adder.prepareToRender
        (
            x0     + Row.SPACING,
            getY() + Row.SPACING,
            width0 - getScrollbarWidth() - (Row.SPACING * 2)
        );
    }

    // Initialize the `rows` list for rendering
    protected void renderRows(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        int y1 = getY();
        int y2 = y1 + ROW_OFFSET;

        int s1 = (int) getScrollY();
        int s2 = s1 + rowsPerScreen();
        for (int i = 0; i < rows.size(); ++i)
        {
            if (s1 <= i && i < s2)
            {
                rows.get(i).prepareToRender
                (
                    x0 + Row.SPACING,
                    (y2 + ((i - s1) * Row.BOUNDS) + Row.SPACING),
                    width0 - getScrollbarWidth() - (Row.SPACING * 2)
                );
            }
            else
            {
                rows.get(i).clear();
            }
        }
    }

    // Refresh the list of widgets to render based on which ones should be visible
    public void refresh()
    {
        {
            ArrayList<ClickableWidget> widgets = widgetsNew;
            widgetsNew = widgetsOld;
            widgetsOld = widgets;

            widgetsNew.clear();
            widgetsNew.addAll(Arrays.asList(adder.button, adder.keyField, adder.valField));
        }
        for (Row row : rows)
        {
            if (row.rendering)
            {
                widgetsNew.add(row.button);
                widgetsNew.add(row.keyField);
                widgetsNew.add(row.valField);
            }
        }
    }

/**********************************************************************************************************************\
> Overrides
\**********************************************************************************************************************/

    @Override
    protected int getContentsHeightWithPadding()
    {
        return Integer.max(rowsPerScreen(), rowCount());
    }

    @Override
    public int getMaxScrollY()
    {
        return Integer.max(0, rowCount() - rowsPerScreen());
    }

    @Override
    public int getScrollbarThumbHeight()
    {
        return (int) Math.ceil(adjustedHeight() * (double) rowsPerScreen() / rowCount());
    }

    @Override
    public int getScrollbarThumbY()
    {
        return (getY() + ROW_OFFSET) + (int) Math.floor((adjustedHeight() - getScrollbarThumbHeight()) * (double) getScrollY() / getMaxScrollY());
    }

    @Override
    protected double getDeltaYPerScroll()
    {
        return 1;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        // Scroll selection's min and max screen X-positions
        int x1 = getX();
        int x2 = x1 + getScrollbarWidth();

        // Scroll selection's min and max screen Y-positions
        int y1 = getY() + ROW_OFFSET;
        int y2 = y1 + adjustedHeight();

        // Call with the calculated parameters
        onClick(mouseX, mouseY, x1, x2, y1, y2);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        Boolean scrollbarDragged;
        try
        {
            Field field = ScrollableWidget.class.getDeclaredField("scrollbarDragged");
            scrollbarDragged = (Boolean) getPrivateAttribute(this, field);
            if (scrollbarDragged == null)
                scrollbarDragged = false;
        }
        catch (NoSuchFieldException | IllegalAccessException ignored)
        {
            scrollbarDragged = false;
        }

        if (scrollbarDragged)
        {
            int h = adjustedHeight();
            int y = getY() + ROW_OFFSET;
            if (mouseY < y)
            {
                setScrollY(0);
            }
            else if (mouseY > y + h)
            {
                setScrollY(getMaxScrollY());
            }
            else
            {
                double e = (double) getMaxScrollY() / (adjustedHeight() - getScrollbarThumbHeight());
                setScrollY(getScrollY() + deltaY * e);
            }
            return true;
        }
        if (isValidClickButton(button))
        {
            onDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        }
        return false;
    }

    @Override
    protected void drawScrollbar(DrawContext context)
    {
        if (overflows())
        {
            int x      = getScrollbarX();
            int y      = getScrollbarThumbY();
            int width  = getScrollbarWidth();
            int height = getScrollbarThumbHeight();
            context.drawGuiTexture
            (
                RenderLayer::getGuiTextured,
                Identifier.ofVanilla("widget/scroller_background"),
                x,
                getY() + ROW_OFFSET,
                width,
                adjustedHeight()
            );
            context.drawGuiTexture
            (
                RenderLayer::getGuiTextured,
                Identifier.ofVanilla("widget/scroller"),
                x,
                y,
                width,
                height
            );
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        int x1 = x0;
        int x2 = x1 + width0;

        int y1 = getY();
        int y2 = y1 + ADD_HEIGHT;
        int y3 = y1 + ROW_OFFSET;
        int y4 = y3 + adjustedHeight();

        if (height >= ADD_HEIGHT)
        {
            context.fill(x1, y1, x2, y2, COLOR_DARKEN);
            renderAdder(context, mouseX, mouseY, deltaTicks);
        }
        if (height >= ROW_OFFSET + Row.BOUNDS)
        {
            // if (ModMenuScreen.modified)
            //     context.fill(x1, y2, x2, y3, COLOR_DARKEN_MORE);
            context.fill(x1, y3, x2, y4, COLOR_DARKEN);
            renderRows(context, mouseX, mouseY, deltaTicks);
        }
        drawScrollbar(context);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder)
    {
        // (TODO)
    }

/**********************************************************************************************************************\
> Helper Classes - Row
\**********************************************************************************************************************/

    // Represents a simple GUI row with a button, a text box, and a numeric text box
    public static class Row
    {
        protected static final int HEIGHT  = 20;
        protected static final int SPACING =  5;
        protected static final int BOUNDS  = HEIGHT + (SPACING * 2);

        protected static final int VAL_CHARS = String.valueOf(getMaxCount()).length();
        protected static final int VAL_WIDTH = 8 + (6 * VAL_CHARS);

        protected static final int MAX_KEY_LENGTH  = getMaxKeyLength();

        protected final SettingsList parent;
        protected       String       key;
        protected       boolean      rendering;

        public final ButtonWidget    button;
        public final TextFieldWidget keyField;
        public final TextFieldWidget valField;

        protected Row(SettingsList parent, String key, boolean forList)
        {
            this.parent = parent;
            this.key    = key;
            rendering   = false;

            button = getButton(forList);

            keyField = new TextBox(this, false);
            keyField.setEditable(!forList);
            keyField.setMaxLength(MAX_KEY_LENGTH);

            valField = new TextBox(this, true);
            valField.setMaxLength(VAL_CHARS);
            valField.setEditable(!forList);

            // If the config had an error while loading, disable the button and all fields
            if (hadConfigError())
            {
                button.active   = false;
                keyField.active = false;
                valField.active = false;
            }

            // Otherwise, disable the text fields if for the list
            else if (forList)
            {
                keyField.active = false;
                valField.active = false;
            }
        }

        protected ButtonWidget getButton(boolean forList)
        {
            int size = HEIGHT;
            ButtonWidget.Builder builder = ButtonWidget.builder
            (
                Text.literal
                (
                    forList
                    ?   "-"
                    :   "+"
                ),
                forList
                ?   (button) ->
                    {
                        button.setFocused(false);
                        parent.remove(this);
                    }
                :   (button) ->
                    {
                        button.setFocused(false);
                        parent.add();
                    }
            );
            builder.size(size, size);
            return builder.build();
        }

        protected static int getMaxKeyLength()
        {
            int maxKeyLength = 0;
            for (String cfgKey : ITEMS.keySet())
            {
                if (maxKeyLength < cfgKey.length())
                    maxKeyLength = cfgKey.length();
            }
            return maxKeyLength;
        }

        public static Row forAdding(SettingsList parent)
        {
            return new Row(parent, "", false);
        }

        public static Row forList(SettingsList parent, String key)
        {
            return new Row(parent, key, true);
        }

        public void prepareToRender(int x, int y, int width)
        {
            boolean forAdder = key.isEmpty();

            rendering = true;

            button.active = !(forAdder && buttonInvalid());
            button.setPosition(x, y);

            valField.setWidth(VAL_WIDTH);
            valField.setPosition(x + width - VAL_WIDTH, y);
            if (!forAdder)
                valField.setText(CONFIG.get(key).toString());

            keyField.setWidth(width - HEIGHT - VAL_WIDTH - (SPACING * 2));
            keyField.setPosition(x + HEIGHT + SPACING, y);
            if (!forAdder)
                keyField.setText(key);
        }

        public void clear()
        {
            rendering = false;
        }

        protected boolean buttonInvalid()
        {
            try
            {
                String key = keyField.getText();
                int    val = Integer.parseInt(valField.getText());
                return
                (
                    val <= 0
                    ||
                    val > getMaxCount()
                    ||
                    (CONFIG.containsKey(key) && CONFIG.get(key) == val)
                    ||
                    !ITEMS.containsKey(key)
                    ||
                    ITEMS.get(key).getComponents().contains(DAMAGE)
                );
            }
            catch (NumberFormatException e)
            {
                return true;
            }
        }

/**********************************************************************************************************************\
> Helper Classes - Row's Text Boxes
\**********************************************************************************************************************/

        protected static class TextBox extends TextFieldWidget
        {
            protected final Row     parent;
            protected final boolean isNumeric;
            protected       String  cache;

            public TextBox(Row parent, boolean isNumeric)
            {
                super(CLIENT.textRenderer, 0, Row.HEIGHT, Text.empty());
                this.parent    = parent;
                this.isNumeric = isNumeric;
                cache          = "";
                setChangedListener
                (
                    new Consumer<String>()
                    {
                        @Override
                        public void accept(String s)
                        {
                            if (isNumeric)
                            {
                                if (s.matches("^(|[1-9][0-9]{0,4})$"))
                                {
                                    cache = s;
                                }
                                else
                                {
                                    int cursor = getCursor() - 1;
                                    setText(cache);
                                    setCursor(cursor, false);
                                }
                            }
                        }
                    }
                );
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers)
            {
                if (active && isFocused() && keyCode == 257) // [Enter]
                {
                    if (parent.buttonInvalid()) return false;
                    playClickSound(CLIENT.getSoundManager());
                    parent.button.onPress();
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }
}
