package sh.miles.menukit;

import com.google.common.base.Preconditions;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import sh.miles.menukit.impl.SlotMenuManager;

/**
 * Main Access Point for MenuKit
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class MenuKit {

    public static final MenuKit INSTANCE = new MenuKit();

    private Plugin plugin;
    private Listener listener;

    /**
     * Starts MenuKit exclusively for a single plugin
     *
     * @param plugin the plugin to start MenuKit for
     * @throws IllegalArgumentException if a plugin has already started this instance of MenuKit
     * @since 1.0.0-SNAPSHOT
     */
    public void start(Plugin plugin) throws IllegalArgumentException {
        Preconditions.checkArgument(this.plugin == null, "A plugin has already initialized " + getClass());
        this.plugin = plugin;
        this.listener = SlotMenuManager.initialize(plugin);
    }

    /**
     * Stops MenuKit for the starting plugin
     *
     * @throws IllegalArgumentException thrown if MenuKit was never started
     * @since 1.0.0-SNAPSHOT
     */
    public void stop() throws IllegalArgumentException {
        Preconditions.checkArgument(this.plugin != null, "Can not teardown MenuKit before MenuKit#setup is called");
        HandlerList.unregisterAll(listener);
        plugin = null;
    }
}
