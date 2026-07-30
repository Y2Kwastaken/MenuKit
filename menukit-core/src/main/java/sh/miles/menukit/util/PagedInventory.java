package sh.miles.menukit.util;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sh.miles.menukit.slot.MenuSlot;

/**
 * A wrapper around a single {@link Inventory} and a {@link PagedArray} instance to simulate the idea of a "PagedArray".
 * This class utilizes the extra bundled data of {@link MenuSlot} to bundle components together and assignt hem to their
 * proper pages.
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class PagedInventory {

    private final Inventory inventory;
    private final PagedArray<MenuSlot> pagedArray;

    /**
     * Creates a new instance of PagedInventory.
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
     * Sets an item on a specific page.
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
        writeIfVisible(page, slot, item.getContent());
    }

    /**
     * Removes an item from a given page and slot
     *
     * @param page the page to remove from
     * @param slot the slot to remove from
     * @since 2.1.0-SNAPSHOT
     */
    public void removeItem(int page, int slot) {
        pagedArray.set(page, slot, null);
        writeIfVisible(page, slot, ItemStack.empty());
    }

    /**
     * Gets the slot at the given index.
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
     * Gets the slot on the provided page at the given index.
     *
     * @param page the page
     * @param slot the slot
     * @return the slot at that position
     * @since 1.0.0-SNAPSHOT
     */
    public MenuSlot getSlot(int page, int slot) {
        final MenuSlot itemSlot = pagedArray.get(page, slot);
        return itemSlot == null ? MenuSlot.DUMMY : itemSlot;
    }

    /**
     * Gets the current page for the given slot.
     *
     * @param slot the slot
     * @return the page that slot is on
     * @since 1.0.0-SNAPSHOT
     */
    public int getCurrentPage(int slot) {
        return this.pagedArray.getCurrentPage(slot);
    }

    /**
     * Sets the current page for all items in the paged array.
     *
     * @param page the page to swap to
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPage(int page) {
        this.pagedArray.setCurrentPage(page);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, this.getSlot(i).getContent());
        }
    }

    /**
     * Swaps to a page, taking any slot that page leaves empty from the fallback page instead.
     *
     * <p>A slot holding no item counts as empty.
     *
     * @param page         the page to swap to
     * @param fallbackPage the page to take empty slots from
     * @since 2.1.0-SNAPSHOT
     */
    public void setPageWithFallback(int page, int fallbackPage) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            this.setCurrentPageFor(this.getSlot(page, slot).hasContent() ? page : fallbackPage, slot);
        }
    }

    /**
     * Sets the current page for a specific slot.
     *
     * @param slot the slot
     * @param page the page to set that slot to
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPageFor(int page, int slot) {
        this.pagedArray.setCurrentPageFor(page, slot);
        inventory.setItem(slot, this.getSlot(slot).getContent());
    }

    /**
     * Sets the current page for an array of slots.
     *
     * @param page  the page to set the slots to
     * @param slots the slots to set
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPageFor(int page, int... slots) {
        for (final int slot : slots) {
            this.setCurrentPageFor(page, slot);
        }
    }

    /**
     * Swaps the given slots to a page, taking any that page leaves empty from the fallback page instead.
     *
     * @param page         the page to swap the given slots to
     * @param fallbackPage the page to take empty slots from
     * @param slots        the slots to swap
     * @since 2.1.0-SNAPSHOT
     */
    public void setPageWithFallbackFor(int page, int fallbackPage, int... slots) {
        for (final int slot : slots) {
            this.setCurrentPageFor(this.getSlot(page, slot).hasContent() ? page : fallbackPage, slot);
        }
    }

    /**
     * Updates the MenuSlot contents in the backing inventory.
     *
     * @param slot the slot to update
     * @since 1.0.0-SNAPSHOT
     */
    public void update(MenuSlot slot) {
        writeIfVisible(slot.getPage(), slot.getSlot(), slot.getContent());
    }

    /**
     * Gets the number of pages this PagedInventory has.
     *
     * @return the page count
     * @since 1.0.0-SNAPSHOT
     */
    public int getPages() {
        return this.pagedArray.getPages();
    }

    /**
     * Gets the size of each page.
     *
     * @return the page size
     * @since 1.0.0-SNAPSHOT
     */
    public int getPageSize() {
        return this.pagedArray.getPageSize();
    }

    /**
     * Writes the given content into the backing inventory, but only if the given page is the one that slot is currently
     * showing. Writes for any other page are kept in the paged array alone until that page is swapped to.
     *
     * @param page    the page the content belongs to
     * @param slot    the slot to write
     * @param content the content to write
     */
    private void writeIfVisible(int page, int slot, ItemStack content) {
        if (this.pagedArray.getCurrentPage(slot) == page) {
            this.inventory.setItem(slot, content);
        }
    }
}
