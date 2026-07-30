package sh.miles.menukit.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sh.miles.menukit.slot.MenuSlot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PagedInventory}, the layer that keeps a single live {@link Inventory} in sync with the page each
 * slot is currently showing.
 *
 * <p>{@link ItemStack#empty()} reaches through to the server internals, so its statics are mocked for the duration of
 * every test rather than a server being stood up.
 */
class PagedInventoryTest {

    private static final int SIZE = 3;
    private static final int PAGES = 3;

    private MockedStatic<ItemStack> itemStackStatics;
    private ItemStack empty;
    private Inventory inventory;
    private PagedInventory paged;

    @BeforeEach
    void setUp() {
        this.empty = mock(ItemStack.class);
        this.itemStackStatics = mockStatic(ItemStack.class);
        this.itemStackStatics.when(ItemStack::empty).thenReturn(this.empty);

        this.inventory = mock(Inventory.class);
        when(this.inventory.getSize()).thenReturn(SIZE);
        this.paged = new PagedInventory(this.inventory, PAGES);
    }

    @AfterEach
    void tearDown() {
        this.itemStackStatics.close();
    }

    /**
     * Builds a slot mock that reports itself as living at the given page and index.
     *
     * @param page       the page the slot claims
     * @param index      the slot index the slot claims
     * @param hasContent whether the slot reports holding an item
     * @return the mocked slot, with a distinct content stack of its own
     */
    private MenuSlot slot(final int page, final int index, final boolean hasContent) {
        final MenuSlot slot = mock(MenuSlot.class);
        when(slot.getPage()).thenReturn(page);
        when(slot.getSlot()).thenReturn(index);
        when(slot.hasContent()).thenReturn(hasContent);
        when(slot.getContent()).thenReturn(mock(ItemStack.class));
        return slot;
    }

    @Test
    void takesItsPageSizeFromTheInventory() {
        assertEquals(SIZE, this.paged.getPageSize());
        assertEquals(PAGES, this.paged.getPages());
    }

    @Test
    void everySlotStartsOnPageZero() {
        for (int slot = 0; slot < SIZE; slot++) {
            assertEquals(0, this.paged.getCurrentPage(slot));
        }
    }

    @Test
    void unsetSlotsReadBackAsTheDummy() {
        assertSame(MenuSlot.DUMMY, this.paged.getSlot(0));
        assertSame(MenuSlot.DUMMY, this.paged.getSlot(2, 0));
    }

    @Test
    void setItemRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> this.paged.setItem(null));
    }

    @Test
    void setItemStoresTheSlotOnItsOwnPage() {
        final MenuSlot item = slot(2, 1, true);

        this.paged.setItem(item);

        assertSame(item, this.paged.getSlot(2, 1));
        assertSame(MenuSlot.DUMMY, this.paged.getSlot(0, 1));
    }

    @Test
    void setItemWritesThroughWhenItsPageIsShowing() {
        final MenuSlot item = slot(0, 1, true);

        this.paged.setItem(item);

        verify(this.inventory).setItem(1, item.getContent());
    }

    @Test
    void setItemDoesNotWriteThroughForAHiddenPage() {
        final MenuSlot item = slot(1, 1, true);

        this.paged.setItem(item);

        verify(this.inventory, never()).setItem(anyInt(), any());
    }

    @Test
    void removeItemDropsTheStoredSlot() {
        this.paged.setItem(slot(0, 1, true));

        this.paged.removeItem(0, 1);

        assertSame(MenuSlot.DUMMY, this.paged.getSlot(0, 1));
    }

    @Test
    void removeItemClearsTheInventoryWhenItsPageIsShowing() {
        this.paged.setItem(slot(0, 1, true));
        clearInvocations(this.inventory);

        this.paged.removeItem(0, 1);

        verify(this.inventory).setItem(1, this.empty);
    }

    @Test
    void removeItemLeavesTheInventoryAloneForAHiddenPage() {
        this.paged.setItem(slot(1, 1, true));
        clearInvocations(this.inventory);

        this.paged.removeItem(1, 1);

        verify(this.inventory, never()).setItem(anyInt(), any());
    }

    @Test
    void updateWritesThroughWhenTheSlotsPageIsShowing() {
        final MenuSlot item = slot(0, 2, true);

        this.paged.update(item);

        verify(this.inventory).setItem(2, item.getContent());
    }

    @Test
    void updateIsIgnoredWhenTheSlotsPageIsHidden() {
        final MenuSlot item = slot(1, 2, true);

        this.paged.update(item);

        verify(this.inventory, never()).setItem(anyInt(), any());
    }

    @Test
    void setCurrentPageMovesAndRepaintsEverySlot() {
        final MenuSlot onPageOne = slot(1, 1, true);
        this.paged.setItem(onPageOne);
        clearInvocations(this.inventory);

        this.paged.setCurrentPage(1);

        for (int slot = 0; slot < SIZE; slot++) {
            assertEquals(1, this.paged.getCurrentPage(slot));
        }
        verify(this.inventory).setItem(0, this.empty);
        verify(this.inventory).setItem(1, onPageOne.getContent());
        verify(this.inventory).setItem(2, this.empty);
    }

    @Test
    void setCurrentPageForMovesOnlyTheGivenSlot() {
        final MenuSlot onPageOne = slot(1, 1, true);
        this.paged.setItem(onPageOne);
        clearInvocations(this.inventory);

        this.paged.setCurrentPageFor(1, 1);

        assertEquals(1, this.paged.getCurrentPage(1));
        assertEquals(0, this.paged.getCurrentPage(0));
        assertEquals(0, this.paged.getCurrentPage(2));
        verify(this.inventory).setItem(1, onPageOne.getContent());
        verify(this.inventory, never()).setItem(eq(0), any());
        verify(this.inventory, never()).setItem(eq(2), any());
    }

    @Test
    void setCurrentPageForMovesEverySlotGivenToTheVarargsForm() {
        this.paged.setCurrentPageFor(2, 0, 2);

        assertEquals(2, this.paged.getCurrentPage(0));
        assertEquals(0, this.paged.getCurrentPage(1));
        assertEquals(2, this.paged.getCurrentPage(2));
    }

    @Test
    void setPageWithFallbackPrefersTheTargetPageWhereItHasContent() {
        final MenuSlot target = slot(1, 0, true);
        this.paged.setItem(target);
        this.paged.setItem(slot(0, 1, true));
        this.paged.setItem(slot(0, 2, true));

        this.paged.setPageWithFallback(1, 0);

        assertEquals(1, this.paged.getCurrentPage(0));
    }

    @Test
    void setPageWithFallbackFallsBackWhereTheTargetPageIsEmpty() {
        final MenuSlot fallback = slot(0, 1, true);
        this.paged.setItem(fallback);
        clearInvocations(this.inventory);

        this.paged.setPageWithFallback(1, 0);

        assertEquals(0, this.paged.getCurrentPage(1), "slot 1 holds nothing on page 1, so it must stay on the fallback");
        verify(this.inventory).setItem(1, fallback.getContent());
    }

    @Test
    void setPageWithFallbackTreatsAContentlessSlotAsEmpty() {
        final MenuSlot contentless = slot(1, 0, false);
        this.paged.setItem(contentless);

        this.paged.setPageWithFallback(1, 0);

        assertEquals(0, this.paged.getCurrentPage(0), "a stored slot holding no item must still count as empty");
    }

    @Test
    void setPageWithFallbackForOnlyTouchesTheGivenSlots() {
        this.paged.setItem(slot(1, 0, true));
        this.paged.setItem(slot(1, 2, true));

        this.paged.setPageWithFallbackFor(1, 0, 0);

        assertEquals(1, this.paged.getCurrentPage(0));
        assertEquals(0, this.paged.getCurrentPage(2), "slot 2 was not listed, so it must not move");
    }
}
