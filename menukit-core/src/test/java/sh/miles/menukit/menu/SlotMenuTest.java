package sh.miles.menukit.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sh.miles.menukit.impl.SlotMenuManager;
import sh.miles.menukit.slot.MenuSlot;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SlotMenu}, focused on the event routing that decides which slot, if any, an inventory event belongs
 * to.
 */
class SlotMenuTest {

    private static final int TOP_SIZE = 9;
    private static final int PAGES = 2;

    private MockedStatic<SlotMenuManager> managerStatics;
    private SlotMenuManager manager;
    private Player player;
    private UUID playerId;
    private InventoryView view;
    private Inventory topInventory;
    private TestMenu menu;

    @BeforeEach
    void setUp() {
        this.manager = mock(SlotMenuManager.class);
        this.managerStatics = mockStatic(SlotMenuManager.class);
        this.managerStatics.when(SlotMenuManager::menuManager).thenReturn(this.manager);

        this.playerId = UUID.randomUUID();
        this.player = mock(Player.class);
        when(this.player.getUniqueId()).thenReturn(this.playerId);

        this.topInventory = mock(Inventory.class);
        when(this.topInventory.getSize()).thenReturn(TOP_SIZE);

        this.view = mock(InventoryView.class);
        when(this.view.getTopInventory()).thenReturn(this.topInventory);
        when(this.view.getPlayer()).thenReturn(this.player);

        this.menu = new TestMenu(this.player, viewer -> this.view, PAGES);
    }

    @AfterEach
    void tearDown() {
        this.managerStatics.close();
    }

    /**
     * Files a slot mock into the menu at the given index on the visible page.
     *
     * @param index the slot index
     * @return the mocked slot
     */
    private MenuSlot registerSlot(final int index) {
        final MenuSlot slot = mock(MenuSlot.class);
        when(slot.getPage()).thenReturn(0);
        when(slot.getSlot()).thenReturn(index);
        when(slot.getContent()).thenReturn(mock(ItemStack.class));
        this.menu.getInventory().setItem(slot);
        return slot;
    }

    private InventoryClickEvent clickOn(final Inventory clicked, final int slot) {
        final InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getClickedInventory()).thenReturn(clicked);
        when(event.getSlot()).thenReturn(slot);
        return event;
    }

    private InventoryDragEvent dragOver(final Integer... rawSlots) {
        final InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getRawSlots()).thenReturn(Set.of(rawSlots));
        return event;
    }

    @Test
    void constructorRejectsNullArguments() {
        final Function<Player, InventoryView> factory = viewer -> this.view;

        assertThrows(IllegalArgumentException.class, () -> new TestMenu(null, factory, PAGES));
        assertThrows(IllegalArgumentException.class, () -> new TestMenu(this.player, null, PAGES));
    }

    @Test
    void sizesItsInventoryFromTheTopInventoryOfTheView() {
        assertEquals(TOP_SIZE, this.menu.getInventory().getPageSize());
        assertEquals(PAGES, this.menu.getInventory().getPages());
    }

    @Test
    void handleClickDispatchesToTheClickedSlot() {
        final MenuSlot slot = registerSlot(3);

        this.menu.handleClick(clickOn(this.topInventory, 3));

        verify(slot).click(any());
    }

    @Test
    void handleClickIgnoresClicksInThePlayerInventory() {
        final MenuSlot slot = registerSlot(3);

        this.menu.handleClick(clickOn(mock(Inventory.class), 3));

        verify(slot, never()).click(any());
    }

    @Test
    void handleClickIgnoresClicksOutsideAnyInventory() {
        final MenuSlot slot = registerSlot(3);

        this.menu.handleClick(clickOn(null, 3));

        verify(slot, never()).click(any());
    }

    @Test
    void handleDragDispatchesToEverySlotOfTheMenuItCovers() {
        final MenuSlot first = registerSlot(1);
        final MenuSlot second = registerSlot(2);

        this.menu.handleDrag(dragOver(1, 2));

        verify(first).drag(any());
        verify(second).drag(any());
    }

    @Test
    void handleDragIgnoresRawSlotsBelongingToThePlayerInventory() {
        final MenuSlot slot = registerSlot(3);

        // raw slots at or past the top inventory size address the players own inventory, and are not menu slots at all
        assertDoesNotThrow(() -> this.menu.handleDrag(dragOver(TOP_SIZE, TOP_SIZE + 20)));

        verify(slot, never()).drag(any());
    }

    @Test
    void handleDragSplitsAcrossBothInventories() {
        final MenuSlot slot = registerSlot(4);

        assertDoesNotThrow(() -> this.menu.handleDrag(dragOver(4, TOP_SIZE + 5)));

        verify(slot).drag(any());
    }

    @Test
    void handleCloseUnregistersTheViewer() {
        this.menu.handleClose(mock(InventoryCloseEvent.class));

        verify(this.manager).unregister(this.playerId);
    }

    @Test
    void openReloadsRegistersAndOpensTheView() {
        when(this.player.getOpenInventory()).thenReturn(mock(InventoryView.class));

        this.menu.open();

        assertEquals(1, this.menu.reloads);
        verify(this.manager).register(this.player, this.menu);
        verify(this.player).openInventory(this.view);
    }

    @Test
    void openRefusesToReopenTheSameView() {
        when(this.player.getOpenInventory()).thenReturn(this.view);

        assertThrows(IllegalStateException.class, () -> this.menu.open());

        assertEquals(0, this.menu.reloads);
        verify(this.manager, never()).register(any(), any());
    }

    /**
     * Minimal concrete menu that records how often it was reloaded.
     */
    private static final class TestMenu extends SlotMenu<InventoryView> {

        private int reloads;

        private TestMenu(final Player player, final Function<Player, InventoryView> viewFactory, final int pageCount) {
            super(player, viewFactory, pageCount);
        }

        @Override
        protected void reload(final InventoryView view) {
            this.reloads++;
        }
    }
}
