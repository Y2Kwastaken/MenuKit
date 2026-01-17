package sh.miles.menukit.strings;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import sh.miles.menukit.menu.MenuEventCallback;
import sh.miles.menukit.slot.MenuSlot;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static sh.miles.menukit.menu.MenuEventCallback.CLICK_CANCEL;
import static sh.miles.menukit.menu.MenuEventCallback.CLICK_NOTHING;
import static sh.miles.menukit.menu.MenuEventCallback.DRAG_CANCEL;
import static sh.miles.menukit.menu.MenuEventCallback.DRAG_NOTHING;

/**
 * Represents an ItemStack with a couple other action properties
 *
 * @since 1.0.0-SNAPSHOT
 */
public record MenuStack(ItemStack item, Consumer<MenuEventCallback<InventoryClickEvent>> click,
                        Consumer<MenuEventCallback<InventoryDragEvent>> drag) {

    @Override
    public ItemStack item() {
        return item.clone();
    }

    public MenuSlot.Builder transfer() {
        return MenuSlot.builder().content(this.item).drag(this.drag).click(this.click);
    }

    /**
     * Copies the contents of this builder to a {@link MenuSlot.Builder} builder
     *
     * @param builder the builder to copy to
     * @return the given builder
     * @since 1.0.0-SNAPSHOT
     */
    public MenuSlot.Builder copyTo(MenuSlot.Builder builder) {
        return builder.content(this.item).drag(this.drag).click(this.click);
    }

    /**
     * Creates a simple MenuStack that takes in an item and allows for events to be automatically cancelled or not act
     *
     * @param item   the item to display
     * @param cancel true to cancel drag and click events
     * @return the created stack
     * @since 1.0.0-SNAPSHOT
     */
    public static MenuStack of(ItemStack item, boolean cancel) {
        return of(item, cancel, false);
    }

    /**
     * Creates a simple MenuStack that takes in an item and allows the ability to automatically cancel and hide the
     * tooltip
     *
     * @param item    the item to display
     * @param cancel  true to cancel drag and click events
     * @param tooltip true to hide the tooltips
     * @return the created stack
     * @since 1.2.0-SNAPSHOT
     */
    @SuppressWarnings("UnstableApiUsage")
    public static MenuStack of(ItemStack item, boolean cancel, boolean tooltip) {
        Preconditions.checkArgument(item != null, "The provided item must not be null");
        if (tooltip) {
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        }
        return new MenuStack(item, cancel ? CLICK_CANCEL : CLICK_NOTHING, cancel ? DRAG_CANCEL : DRAG_NOTHING);
    }

    /**
     * Creates a simple MenuStack that takes in an item type and allows the ability to automatically cancel its events
     * and hide the tooltip
     *
     * @param type    the type
     * @param cancel  true to cancel drag and click events
     * @param tooltip true to hide tooltips
     * @return the created stack
     * @since 1.3.0-SNAPSHOT
     */
    public static MenuStack of(ItemType type, boolean cancel, boolean tooltip) {
        return of(type.createItemStack(), cancel, tooltip);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for MenuStack
     *
     * @since 1.0.0-SNAPSHOT
     */
    public static class Builder {
        private ItemStack content = ItemStack.empty();
        private Consumer<MenuEventCallback<InventoryClickEvent>> click = CLICK_NOTHING;
        private Consumer<MenuEventCallback<InventoryDragEvent>> drag = DRAG_NOTHING;

        private Builder() {
        }

        /**
         * Sets the click callback for this builder.
         *
         * @param click the click callback
         * @return this builder
         * @since 1.0.0-SNAPSHOT
         */
        public Builder click(final Consumer<MenuEventCallback<InventoryClickEvent>> click) {
            Preconditions.checkArgument(click != null, "The provided click event must not be null");
            this.click = click;
            return this;
        }

        /**
         * Sets the drag callback for this builder.
         *
         * @param drag the drag callback
         * @return this builder
         * @since 1.0.0-SNAPSHOT
         */
        public Builder drag(final Consumer<MenuEventCallback<InventoryDragEvent>> drag) {
            Preconditions.checkArgument(drag != null, "The provided drag event must not be null");
            this.drag = drag;
            return this;
        }

        /**
         * Sets the content of this slot
         *
         * @param itemStack the item content
         * @return this builder
         * @since 1.0.0-SNAPSHOT
         */
        public Builder content(final ItemStack itemStack) {
            Preconditions.checkArgument(itemStack != null, "The provided item must not be null");
            this.content = itemStack;
            return this;
        }

        /**
         * Sets the content of this slot
         *
         * @param type the type to set as the content
         * @return this builder
         * @since 1.3.0-SNAPSHOT
         */
        public Builder content(final ItemType type) {
            Preconditions.checkArgument(type != null, "The provided item type must not be null");
            this.content = type.createItemStack();
            return this;
        }

        /**
         * Sets the content of this slot
         *
         * @param stackFunc the stack creating function
         * @since 1.2.0-SNAPSHOT
         * @deprecated see {@link #content(ItemType, Consumer)}
         */
        @Deprecated(forRemoval = true)
        public Builder content(final Supplier<ItemStack> stackFunc) {
            Preconditions.checkArgument(stackFunc != null, "the provided stack function must not be null");
            this.content = stackFunc.get();
            return this;
        }

        /**
         * Sets the content of this slot
         *
         * @param type   the type of the item to set
         * @param modify any pre modifications
         * @return this builder
         * @since 1.3.0-SNAPSHOT
         */
        public Builder content(final ItemType type, final Consumer<ItemStack> modify) {
            Preconditions.checkArgument(type != null, "The provided type must not be null");
            Preconditions.checkArgument(modify != null, "Modification function must not be null");
            this.content = type.createItemStack();
            modify.accept(this.content);
            return this;
        }

        /**
         * Builds this MenuStack builder
         *
         * @return the MenuStack
         * @since 1.0.0-SNAPSHOT
         */
        public MenuStack build() {
            return new MenuStack(this.content, this.click, this.drag);
        }
    }
}
