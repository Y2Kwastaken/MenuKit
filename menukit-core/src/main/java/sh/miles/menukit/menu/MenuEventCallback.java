package sh.miles.menukit.menu;

import com.google.common.base.Preconditions;
import java.util.function.Consumer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;

/**
 * Occurs when a menu slot is clicked
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class MenuEventCallback<T extends InventoryEvent> {

	public static final Consumer<MenuEventCallback<InventoryClickEvent>> CLICK_NOTHING = (e) -> {};
	public static final Consumer<MenuEventCallback<InventoryDragEvent>> DRAG_NOTHING = (e) -> {};
	public static final Consumer<MenuEventCallback<InventoryClickEvent>> CLICK_CANCEL = MenuEventCallback::cancel;
	public static final Consumer<MenuEventCallback<InventoryDragEvent>> DRAG_CANCEL = MenuEventCallback::cancel;

	private final SlotMenu<?> menu;
	private final T event;

	MenuEventCallback(final T event, final SlotMenu<?> menu) {
		Preconditions.checkArgument(event != null, "The provided event must not be null");
		Preconditions.checkArgument(menu != null, "The provided menu must not be null");
		this.menu = menu;
		this.event = event;
	}

	/**
	 * Gets the player involved in this callback
	 *
	 * @return the player
	 * @throws IllegalStateException thrown if the menu event callback is somehow triggered by a non
	 *     player
	 * @since 1.0.0-SNAPSHOT
	 */
	public Player getPlayer() throws IllegalStateException {
		final HumanEntity human = event.getView().getPlayer();
		if (!(human instanceof Player)) {
			throw new IllegalStateException("event causing menu event callback is not a player!?");
		}
		return (Player) human;
	}

	/**
	 * Gets the event for this callback
	 *
	 * @return the event
	 * @since 1.0.0-SNAPSHOT
	 */
	public T getEvent() {
		return event;
	}

	/**
	 * Gets the slot menu directly involved in this event callback
	 *
	 * @return the involved menu
	 * @since 1.0.0-SNAPSHOT
	 */
	public SlotMenu<?> getMenu() {
		return menu;
	}

	/**
	 * Cancels the provided event if possible
	 *
	 * @since 1.0.0-SNAPSHOT
	 */
	public void cancel() {
		if (this.event instanceof Cancellable cancellable) {
			cancellable.setCancelled(true);
		}
	}
}
