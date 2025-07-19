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

    public void register(Player player, SlotMenu<?> menu) {
        this.menus.put(player.getUniqueId(), menu);
    }

    public void unregister(UUID playerUUID) {
        this.menus.remove(playerUUID);
    }

    public Optional<SlotMenu<?>> getMenu(UUID playerUUID) {
        return Optional.ofNullable(menus.get(playerUUID));
    }

    public static SlotMenuManager menuManager() {
        Preconditions.checkArgument(instance != null, "Can not query MenuManager without initializing MenuKit");
        return instance;
    }

    public static Listener initialize(Plugin plugin) {
        Preconditions.checkArgument(instance == null, "Can not initialize MenuManager when MenuKit is already initialized");
        instance = new SlotMenuManager(plugin);
        return instance.listener;
    }
}
