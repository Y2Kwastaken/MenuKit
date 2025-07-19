package sh.miles.menukit.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import sh.miles.menukit.menu.SlotMenu;

/**
 * Listener implementation for MenuManager
 * <p>
 * Note this is an internal class managed by {@link SlotMenu}
 *
 * @since 1.0.0-SNAPSHOT
 */
class SlotMenuListener implements Listener {

    private final SlotMenuManager menuManager;

    /**
     * Creates a new SlotMenuListener
     *
     * @param menuManager the manager associated with this listener
     * @since 1.0.0-SNAPSHOT
     */
    public SlotMenuListener(final SlotMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        menuManager.getMenu(event.getView().getPlayer().getUniqueId()).ifPresent((menu) -> menu.handleClick(event));
    }

    @EventHandler
    public void onDrag(final InventoryDragEvent event) {
        menuManager.getMenu(event.getView().getPlayer().getUniqueId()).ifPresent(menu -> menu.handleDrag(event));
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        menuManager.getMenu(event.getView().getPlayer().getUniqueId()).ifPresent(menu -> menu.handleOpen(event));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        menuManager.getMenu(event.getView().getPlayer().getUniqueId()).ifPresent(menu -> menu.handleClose(event));
    }
}
