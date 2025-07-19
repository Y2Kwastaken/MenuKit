package sh.miles.menukit.slot;

import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import sh.miles.menukit.menu.MenuEventCallback;
import sh.miles.menukit.util.PagedInventory;

import java.util.function.Consumer;

/**
 * Standard implementation for {@link MenuSlot}
 *
 * @since 1.0.0-SNAPSHOT
 */
public class MenuSlotImpl implements MenuSlot {

    private final int index;
    private final int page;
    private final PagedInventory inventory;
    private final Consumer<MenuEventCallback<InventoryClickEvent>> click;
    private final Consumer<MenuEventCallback<InventoryDragEvent>> drag;

    private ItemStack content = ItemStack.empty();

    /**
     * Creates a MenuSlotImpl
     *
     * @param index     the index the menu slot is at
     * @param page      the page the menu slot is on
     * @param inventory the inventory the slot is in
     * @param click     the event that occurs on click
     * @param drag      the event that occurs on drags including this slot
     */
    MenuSlotImpl(
            final int index,
            final int page,
            final PagedInventory inventory,
            final Consumer<MenuEventCallback<InventoryClickEvent>> click,
            final Consumer<MenuEventCallback<InventoryDragEvent>> drag) {
        this.index = index;
        this.page = page;
        this.inventory = inventory;
        this.click = click;
        this.drag = drag;
    }

    @Override
    public ItemStack getContent() {
        return this.content.clone();
    }

    @Override
    public void setContent(final ItemStack item) {
        Preconditions.checkArgument(
                item != null, "The provided item must not be null use ItemStack#empty() for an empty item");
        this.content = item;
        inventory.setItem(this);
    }

    @Override
    public boolean hasContent() {
        return !this.content.isEmpty();
    }

    @Override
    public void click(final MenuEventCallback<InventoryClickEvent> callback) {
        this.click.accept(callback);
    }

    @Override
    public void drag(final MenuEventCallback<InventoryDragEvent> callback) {
        this.drag.accept(callback);
    }

    @Override
    public int getSlot() {
        return this.index;
    }

    @Override
    public int getPage() {
        return this.page;
    }
}
