package net.dividebytoaster.ToastersStackSizeMod.client.widgets;

/// Imports - Java
import java.lang.reflect.Field;

/// Imports - Minecraft
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

/// Imports - Annotations
import org.jetbrains.annotations.NotNull;

/// Imports - Local
import static net.dividebytoaster.ToastersStackSizeMod.ModMain.*;

/**********************************************************************************************************************\
> Core Attributes
\**********************************************************************************************************************/

public abstract class ScrollBar
extends ScrollableWidget
{
    // Simple cache used to maintain scroll position during screen resizing
    public static class Cache
    {
        public Double scrollY = null;

        public void clear()
        {
            scrollY = null;
        }
    }
    protected final Cache cache;

    public ScrollBar(@NotNull Cache cache, int x, int y, int width, int height)
    {
        super(x, y, width, height, Text.empty());
        this.cache = cache;

        // Check if the scroll position was cached
        if (cache.scrollY != null)
            setScrollY(cache.scrollY);
    }

/**********************************************************************************************************************\
> Overrides - Main Methods
\**********************************************************************************************************************/

    @Override
    public void setScrollY(double scrollY)
    {
        super.setScrollY(scrollY);

        // Cache the scroll position in case the window is resized
        cache.scrollY = getScrollY();
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        // Scroll selection's min and max screen X-positions
        int x2 = getX() + width;
        int x1 = x2 - SCROLLBAR_WIDTH;

        // Scroll selection's min and max screen Y-positions
        int y1 = getY();
        int y2 = y1 + height;

        // Call with the calculated parameters
        onClick(mouseX, mouseY, x1, x2, y1, y2);
    }

/**********************************************************************************************************************\
> Overrides - Helper Methods
\**********************************************************************************************************************/

    protected void onClick(double mouseX, double mouseY, int x1, int x2, int y1, int y2)
    {
        // Only proceed if the mouse clicked somewhere in the scroll selection
        if
        (
            x1 <= mouseX && mouseX <= x2
            &&
            y1 <= mouseY && mouseY <= y2
        )
        {
            // Say that as long as the mouse is held down, the scrollbar is being dragged
            try
            {
                Field field = ScrollableWidget.class.getDeclaredField("scrollbarDragged");
                setPrivateAttribute(this, field, true);
            }
            catch (NoSuchFieldException | IllegalAccessException ignored)
            {}

            // The scrollbar's total height, in screen units
            double h1 = getScrollbarThumbHeight();

            // The total scroll bar resolution
            double ch = getContentsHeightWithPadding();

            // The scroll section's screen height
            double dh = (y2 - y1 - h1);

            // The scroll bar's Y-position on the screen
            double sh = ((1 - ((getMaxScrollY() - getScrollY()) / ch)) * dh) + y1;

            // If the mouse is clicking the scroll bar, do not further adjust it
            if (mouseY < sh || sh + h1 < mouseY)
            {
                // Half of the scrollbar's height
                double h2 = h1 / 2;

                // If the mouse clicked near the top of the scroll selection, move it to the top
                if (mouseY < y1 + h2)
                {
                    setScrollY(0);
                }

                // If the mouse clicked near the bottom of the scroll selection, move it to the bottom
                else if (mouseY > y2 - h2)
                {
                    setScrollY(getMaxScrollY());
                }

                // Otherwise, center the scroll bar on the mouse
                else
                {
                    double f = 1 - ((mouseY - y1 - h2) / dh);
                    setScrollY(getMaxScrollY() - f * ch);
                }
            }
        }
    }
}
