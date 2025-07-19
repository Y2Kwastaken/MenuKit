package sh.miles.menukit.menu;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.Nullable;
import sh.miles.menukit.impl.SlotMenuManager;
import sh.miles.menukit.slot.MenuSlot;
import sh.miles.menukit.util.PagedInventory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Menu constructed with an array of {@link MenuSlot}'s
 *
 * <p>This class is intended to be extended purely
 *
 * @since 1.0.0-SNAPSHOT
 */
public abstract class SlotMenu<V extends InventoryView> {

	protected final Player viewer;
	protected final PagedInventory inventory;
	protected final V bukkitView;

	protected SlotMenu(final Player player, final Function<Player, V> viewFactory, final int pageCount) {
		Preconditions.checkArgument(player != null, "A non null player must be provided");
		Preconditions.checkArgument(viewFactory != null, "A non null view factory must be provided");
		this.viewer = player;
		this.bukkitView = viewFactory.apply(player);
		this.inventory = new PagedInventory(bukkitView.getTopInventory(), pageCount);
	}

	public final MenuSlot createSlot(Consumer<MenuSlot.Builder> builder) {
		final MenuSlot.Builder slotBuilder =
				MenuSlot.builder().inventory(this.inventory).page(0);
		builder.accept(slotBuilder);
		return slotBuilder.build();
	}

	/**
	 * Handles the click event for this menu
	 *
	 * <p>If this method is overridden and the super method is not called behavior may not occur as
	 * normally expected. It is recommended to always call the super logic or reimplement it
	 * accordingly
	 *
	 * @param event the click event
	 * @since 1.0.0-SNAPSHOT
	 */
	public void handleClick(final InventoryClickEvent event) {
		if (event.getClickedInventory() != null
				&& event.getClickedInventory().equals(this.bukkitView.getTopInventory())) {
			final int slot = event.getSlot();
			this.inventory.getSlot(slot).click(new MenuEventCallback<>(event, this));
		}
	}

	/**
	 * Handles the drag event for this menu
	 *
	 * <p>If this method is overridden and the super method is not called behavior may not occur as
	 * normally expected. It is recommended to always call the super logic or reimplement it
	 * accordingly
	 *
	 * @param event the drag event
	 * @since 1.0.0-SNAPSHOT
	 */
	public void handleDrag(final InventoryDragEvent event) {
		if (event.getInventory().equals(this.bukkitView.getTopInventory())) {
			for (final Integer slot : event.getInventorySlots()) {
				inventory.getSlot(slot).drag(new MenuEventCallback<>(event, this));
			}
		}
	}

	/**
	 * Handles the open event for this menu
	 *
	 * <p>This method has no base functionality
	 *
	 * @param event the open event
	 * @since 1.0.0-SNAPSHOT
	 */
	public void handleOpen(final InventoryOpenEvent event) {}

	/**
	 * Handles the close event for this menu
	 *
	 * <p>When overriding this method ensure to call the super method or memory leaks could occur
	 *
	 * @param event the close event
	 * @since 1.0.0-SNAPSHOT
	 */
	public void handleClose(final InventoryCloseEvent event) {
		SlotMenuManager.menuManager().unregister(this.bukkitView.getPlayer().getUniqueId());
	}

	/**
	 * Opens the menu for the player and registers it to the menu manager
	 *
	 * @throws IllegalStateException thrown if the player already has the menu open
	 * @since 1.0.0-SNAPSHOT
	 */
	public void open() throws IllegalStateException {
		if (this.bukkitView == viewer.getOpenInventory()) {
			throw new IllegalStateException("Can not re-open same menu twice");
		}

		reload(this.bukkitView);
		SlotMenuManager.menuManager().register(viewer, this);
		viewer.openInventory(this.bukkitView);
	}

	/**
	 * Gets the bukkit view of this menu
	 *
	 * @return the view
	 * @since 1.0.0-SNAPSHOT
	 */
	@Nullable
	public InventoryView getBukkitView() {
		return this.bukkitView;
	}

	/**
	 * Function called on first load or any load of this menu.
	 *
	 * <p>Reloads should contain all initialization logic
	 *
	 * @since 1.0.0-SNAPSHOT
	 */
	protected abstract void reload(final V view);
}
