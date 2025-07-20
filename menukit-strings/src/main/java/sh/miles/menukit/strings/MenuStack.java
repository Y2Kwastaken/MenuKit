package sh.miles.menukit.strings;

import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import sh.miles.menukit.menu.MenuEventCallback;
import sh.miles.menukit.slot.MenuSlot;

import java.util.function.Consumer;

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
     */
    public static MenuStack of(ItemStack item, boolean cancel) {
        Preconditions.checkArgument(item != null, "The provided item must not be null");
        return new MenuStack(item, cancel ? MenuEventCallback.CLICK_CANCEL : MenuEventCallback.CLICK_NOTHING, cancel ? MenuEventCallback.DRAG_CANCEL : MenuEventCallback.DRAG_NOTHING);
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
        private Consumer<MenuEventCallback<InventoryClickEvent>> click = MenuEventCallback.CLICK_NOTHING;
        private Consumer<MenuEventCallback<InventoryDragEvent>> drag = MenuEventCallback.DRAG_NOTHING;

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
