package sh.miles.menukit.impl;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import sh.miles.menukit.menu.SlotMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages open and closed menus
 * <p>
 * Note this is an internal class managed by {@link SlotMenu}
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class SlotMenuManager {

    private static SlotMenuManager instance = null;

    private final Map<UUID, SlotMenu<?>> menus = new HashMap<>();
    private final Listener listener;

    /**
     * Creates a new slot menu manager
     * <p>
     * Only one should exist per plugin
     *
     * @param plugin the plugin to register with
     * @since 1.0.0-SNAPSHOT
     */
    public SlotMenuManager(final Plugin plugin) {
        this.listener = new SlotMenuListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, plugin);
    }

    /**
     * Marks the given menu as the one open for the given player, replacing any menu already tracked for them.
     *
     * @param player the player the menu belongs to
     * @param menu   the menu to track
     * @since 1.0.0-SNAPSHOT
     */
    public void register(Player player, SlotMenu<?> menu) {
        this.menus.put(player.getUniqueId(), menu);
    }

    /**
     * Stops tracking whatever menu is open for the given player. Doing so is what keeps closed menus from leaking.
     *
     * @param playerUUID the uuid of the player to drop
     * @since 1.0.0-SNAPSHOT
     */
    public void unregister(UUID playerUUID) {
        this.menus.remove(playerUUID);
    }

    /**
     * Gets the menu currently open for the given player.
     *
     * @param playerUUID the uuid of the player to look up
     * @return the open menu, or empty if that player has no menu open
     * @since 1.0.0-SNAPSHOT
     */
    public Optional<SlotMenu<?>> getMenu(UUID playerUUID) {
        return Optional.ofNullable(menus.get(playerUUID));
    }

    /**
     * Gets the active menu manager.
     *
     * @return the manager
     * @throws IllegalArgumentException thrown if MenuKit has not been started
     * @since 1.0.0-SNAPSHOT
     */
    public static SlotMenuManager menuManager() {
        Preconditions.checkArgument(instance != null, "Can not query MenuManager without initializing MenuKit");
        return instance;
    }

    /**
     * Creates the singleton menu manager for the given plugin and registers its listener.
     *
     * @param plugin the plugin to register with
     * @return the listener backing the new manager, so that the caller can unregister it on teardown
     * @throws IllegalArgumentException thrown if a manager is already initialized
     * @since 1.0.0-SNAPSHOT
     */
    public static Listener initialize(Plugin plugin) {
        Preconditions.checkArgument(instance == null, "Can not initialize MenuManager when MenuKit is already initialized");
        instance = new SlotMenuManager(plugin);
        return instance.listener;
    }

    /**
     * Tears down the active menu manager, dropping every tracked menu and clearing the singleton so that MenuKit can be
     * started again.
     *
     * @throws IllegalArgumentException thrown if no MenuManager is currently initialized
     * @since 2.1.1-SNAPSHOT
     */
    public static void shutdown() throws IllegalArgumentException {
        Preconditions.checkArgument(instance != null, "Can not shutdown MenuManager without initializing MenuKit");
        instance.menus.clear();
        instance = null;
    }
}
