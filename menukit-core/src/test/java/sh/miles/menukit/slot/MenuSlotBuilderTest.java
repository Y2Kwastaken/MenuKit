package sh.miles.menukit.slot;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import sh.miles.menukit.menu.MenuEventCallback;
import sh.miles.menukit.util.PagedInventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MenuSlot.Builder} and the {@link MenuSlotImpl} it produces.
 */
class MenuSlotBuilderTest {

    private static final int SIZE = 9;
    private static final int PAGES = 3;

    private MockedStatic<ItemStack> itemStackStatics;
    private ItemStack empty;
    private ItemStack content;
    private Inventory inventory;
    private PagedInventory paged;

    @BeforeEach
    void setUp() {
        this.empty = stack();
        this.itemStackStatics = mockStatic(ItemStack.class);
        this.itemStackStatics.when(ItemStack::empty).thenReturn(this.empty);
        this.content = stack();

        this.inventory = mock(Inventory.class);
        when(this.inventory.getSize()).thenReturn(SIZE);
        this.paged = new PagedInventory(this.inventory, PAGES);
    }

    @AfterEach
    void tearDown() {
        this.itemStackStatics.close();
    }

    /**
     * Builds an item stack mock that clones to itself, so that the defensive copying inside {@link MenuSlotImpl} stays
     * invisible to identity assertions.
     *
     * @return the mocked stack
     */
    private ItemStack stack() {
        final ItemStack stack = mock(ItemStack.class);
        when(stack.clone()).thenReturn(stack);
        return stack;
    }

    @Test
    void buildRejectsAMissingInventory() {
        final MenuSlot.Builder builder = MenuSlot.builder().index(0).page(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void settersRejectNull() {
        final MenuSlot.Builder builder = MenuSlot.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.inventory(null));
        assertThrows(IllegalArgumentException.class, () -> builder.content(null));
        assertThrows(IllegalArgumentException.class, () -> builder.click(null));
        assertThrows(IllegalArgumentException.class, () -> builder.drag(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, SIZE})
    void buildRejectsAnIndexOutsideTheInventory(final int index) {
        final MenuSlot.Builder builder = MenuSlot.builder().inventory(this.paged).page(0).index(index);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, PAGES})
    void buildRejectsAPageOutsideTheInventory(final int page) {
        final MenuSlot.Builder builder = MenuSlot.builder().inventory(this.paged).index(0).page(page);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void buildRejectsTheUnsetCoordinateDefaults() {
        assertThrows(IllegalArgumentException.class, () -> MenuSlot.builder().inventory(this.paged).build());
    }

    @Test
    void buildExposesTheConfiguredCoordinatesAndContent() {
        final MenuSlot slot = MenuSlot.builder()
                .inventory(this.paged)
                .index(4)
                .page(1)
                .content(this.content)
                .build();

        assertEquals(4, slot.getSlot());
        assertEquals(1, slot.getPage());
        assertSame(this.content, slot.getContent());
        assertTrue(slot.hasContent());
    }

    @Test
    void contentDefaultsToAnEmptyStack() {
        final MenuSlot slot = MenuSlot.builder().inventory(this.paged).index(0).page(0).build();

        assertSame(this.empty, slot.getContent());
    }

    @Test
    void buildRegistersTheSlotIntoTheInventoryItWasGiven() {
        final MenuSlot slot = MenuSlot.builder()
                .inventory(this.paged)
                .index(4)
                .page(1)
                .content(this.content)
                .build();

        assertSame(slot, this.paged.getSlot(1, 4), "building a slot must file it into its inventory, no set call needed");
    }

    @Test
    void buildWritesThroughWhenTheSlotLandsOnAVisiblePage() {
        MenuSlot.builder().inventory(this.paged).index(4).page(0).content(this.content).build();

        verify(this.inventory).setItem(4, this.content);
    }

    @Test
    void setContentReplacesTheStoredContentAndRepaints() {
        final MenuSlot slot = MenuSlot.builder().inventory(this.paged).index(4).page(0).build();
        final ItemStack replacement = stack();

        slot.setContent(replacement);

        assertSame(replacement, slot.getContent());
        verify(this.inventory).setItem(4, replacement);
    }

    @Test
    void setContentRejectsNull() {
        final MenuSlot slot = MenuSlot.builder().inventory(this.paged).index(0).page(0).build();

        assertThrows(IllegalArgumentException.class, () -> slot.setContent(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void defaultCallbacksDoNothing() {
        final MenuSlot slot = MenuSlot.builder().inventory(this.paged).index(0).page(0).build();
        final MenuEventCallback<InventoryClickEvent> click = mock(MenuEventCallback.class);
        final MenuEventCallback<InventoryDragEvent> drag = mock(MenuEventCallback.class);

        slot.click(click);
        slot.drag(drag);

        verifyNoInteractions(click);
        verifyNoInteractions(drag);
    }

    @Test
    @SuppressWarnings("unchecked")
    void disableInteractionsCancelsBothClicksAndDrags() {
        final MenuSlot slot = MenuSlot.builder()
                .inventory(this.paged)
                .index(0)
                .page(0)
                .disableInteractions()
                .build();
        final MenuEventCallback<InventoryClickEvent> click = mock(MenuEventCallback.class);
        final MenuEventCallback<InventoryDragEvent> drag = mock(MenuEventCallback.class);

        slot.click(click);
        slot.drag(drag);

        verify(click).cancel();
        verify(drag).cancel();
    }

    @Test
    @SuppressWarnings("unchecked")
    void disableInteractionsOverridesCallbacksSetBeforeIt() {
        final MenuSlot slot = MenuSlot.builder()
                .inventory(this.paged)
                .index(0)
                .page(0)
                .click(callback -> {
                    throw new AssertionError("the replaced click callback must not run");
                })
                .disableInteractions()
                .build();
        final MenuEventCallback<InventoryClickEvent> click = mock(MenuEventCallback.class);

        slot.click(click);

        verify(click).cancel();
    }
}
