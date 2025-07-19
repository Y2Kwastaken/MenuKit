package sh.miles.menukit.util;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import sh.miles.menukit.slot.MenuSlot;

/**
 * A Paged Inventory Wrapper
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class PagedInventory {

    private final Inventory inventory;
    private final PagedArray<MenuSlot> pagedArray;

    /**
     * Creates a new instance of PagedInventory
     *
     * @param inventory the inventory to use
     * @param pages     the amount of pages this PagedInventory will have
     * @since 1.0.0-SNAPSHOT
     */
    public PagedInventory(Inventory inventory, int pages) {
        this.inventory = inventory;
        this.pagedArray = new PagedArray<>(inventory.getSize(), pages);
    }

    /**
     * Sets an item on a specific page
     *
     * @param item the item to put
     * @throws IllegalArgumentException thrown if the item is null
     * @since 1.0.0-SNAPSHOT
     */
    public void setItem(MenuSlot item) throws IllegalArgumentException {
        Preconditions.checkArgument(item != null, "the provided item slot must not be null");
        final int page = item.getPage();
        final int slot = item.getSlot();
        pagedArray.set(page, slot, item);
        if (pagedArray.getCurrentPage(slot) == page) {
            inventory.setItem(slot, item.getContent());
        }
    }

    /**
     * Gets the slot at the given index
     *
     * @param slot the slot
     * @return the slot at that position
     * @since 1.0.0-SNAPSHOT
     */
    public MenuSlot getSlot(int slot) {
        final MenuSlot itemSlot = pagedArray.get(slot);
        return itemSlot == null ? MenuSlot.DUMMY : itemSlot;
    }

    /**
     * Gets the slot on the provided page at the given index
     *
     * @param slot the slot
     * @param page the page
     * @return the slot at that position
     * @since 1.0.0-SNAPSHOT
     */
    public MenuSlot getSlot(int page, int slot) {
        final MenuSlot itemSlot = pagedArray.get(page, slot);
        return itemSlot == null ? MenuSlot.DUMMY : itemSlot;
    }

    /**
     * Gets the current page for the given slot
     *
     * @param slot the slot
     * @return the page that slot is on
     * @since 1.0.0-SNAPSHOT
     */
    public int getCurrentPage(int slot) {
        return this.pagedArray.getCurrentPage(slot);
    }

    /**
     * Sets the current page for all items in the paged array
     *
     * @param page the page to swap to
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPage(int page) {
        this.pagedArray.setCurrentPage(page);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, this.pagedArray.get(i).getContent());
        }
    }

    /**
     * Sets the current page for a specific slot
     *
     * @param slot the slot
     * @param page the page to set that slot to
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPageFor(int page, int slot) {
        this.pagedArray.setCurrentPageFor(slot, page);
        inventory.setItem(slot, this.pagedArray.get(slot).getContent());
    }

    /**
     * Sets the current page for an array of slots
     *
     * @param page  the page to set the slots to
     * @param slots the slots to set
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPageFor(int page, int[] slots) {
        for (final int slot : slots) {
            this.setCurrentPageFor(page, slot);
        }
    }

    /**
     * Updates the MenuSlot contents in the backing inventory
     *
     * @param slot the slot to update
     * @since 1.0.0-SNAPSHOT
     */
    public void update(MenuSlot slot) {
        if (this.getCurrentPage(slot.getSlot()) != slot.getPage()) {
            return;
        }

        this.inventory.setItem(slot.getSlot(), slot.getContent());
    }

    /**
     * Gets the number of pages this PagedInventory has
     *
     * @return the page count
     * @since 1.0.0-SNAPSHOT
     */
    public int getPages() {
        return this.pagedArray.getPages();
    }

    /**
     * Gets the size of each page
     *
     * @return the page size
     * @since 1.0.0-SNAPSHOT
     */
    public int getPageSize() {
        return this.pagedArray.getPageSize();
    }
}
