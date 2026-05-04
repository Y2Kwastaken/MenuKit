package sh.miles.menukit.menu;

import com.google.common.base.Preconditions;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;

import java.util.function.Consumer;

/**
 * A class that represents a "event" callback. Event Callbacks are wrapped around Bukkit events to provide ease of use
 * utility methods.
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class MenuEventCallback<T extends InventoryEvent> {

    /**
     * A Built in menu click callback that does nothing.
     */
    public static final Consumer<MenuEventCallback<InventoryClickEvent>> CLICK_NOTHING = (e) -> {
    };
    /**
     * A Built in menu drag callback that does nothing.
     */
    public static final Consumer<MenuEventCallback<InventoryDragEvent>> DRAG_NOTHING = (e) -> {
    };
    /**
     * A Built in menu click callback that cancels the click event.
     */
    public static final Consumer<MenuEventCallback<InventoryClickEvent>> CLICK_CANCEL = MenuEventCallback::cancel;
    /**
     * A Built in menu drag callback that cancels the event.
     */
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
     * Gets the player involved for this menu callback.
     *
     * @return the player
     * @throws IllegalStateException thrown if the menu event callback is somehow triggered by a non player
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
     * Gets the event for this callback.
     *
     * @return the event
     * @since 1.0.0-SNAPSHOT
     */
    public T getEvent() {
        return event;
    }

    /**
     * Gets the slot menu directly involved in this event callback.
     *
     * @return the involved menu
     * @since 1.0.0-SNAPSHOT
     */
    public SlotMenu<?> getMenu() {
        return menu;
    }

    /**
     * Cancels the provided event if possible.
     *
     * @since 1.0.0-SNAPSHOT
     */
    public void cancel() {
        if (this.event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }
}
