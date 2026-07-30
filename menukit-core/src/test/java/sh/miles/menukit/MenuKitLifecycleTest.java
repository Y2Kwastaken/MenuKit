package sh.miles.menukit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sh.miles.menukit.impl.SlotMenuManager;
import sh.miles.menukit.menu.SlotMenu;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link MenuKit} start and stop lifecycle, and the {@link SlotMenuManager} singleton it owns.
 *
 * <p>Both hold process wide state, so every test scrubs that state on the way in and on the way out rather than
 * trusting the previous test to have left things tidy.
 */
class MenuKitLifecycleTest {

    private MockedStatic<Bukkit> bukkitStatics;
    private PluginManager pluginManager;
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        this.pluginManager = mock(PluginManager.class);
        this.bukkitStatics = mockStatic(Bukkit.class);
        this.bukkitStatics.when(Bukkit::getPluginManager).thenReturn(this.pluginManager);
        this.plugin = mock(Plugin.class);

        teardownMenuKit();
    }

    @AfterEach
    void tearDown() {
        teardownMenuKit();
        this.bukkitStatics.close();
    }

    /**
     * Returns MenuKit to a stopped state whatever state it is currently in.
     */
    private void teardownMenuKit() {
        try {
            MenuKit.INSTANCE.stop();
        } catch (final RuntimeException ignored) {
            // already stopped
        }
        try {
            SlotMenuManager.shutdown();
        } catch (final RuntimeException ignored) {
            // already torn down
        }
    }

    @Test
    void startRegistersAListenerForTheStartingPlugin() {
        MenuKit.INSTANCE.start(this.plugin);

        verify(this.pluginManager).registerEvents(any(Listener.class), eq(this.plugin));
    }

    @Test
    void startExposesTheMenuManager() {
        MenuKit.INSTANCE.start(this.plugin);

        assertNotNull(SlotMenuManager.menuManager());
    }

    @Test
    void startRefusesASecondPluginWhileRunning() {
        MenuKit.INSTANCE.start(this.plugin);

        assertThrows(IllegalArgumentException.class, () -> MenuKit.INSTANCE.start(mock(Plugin.class)));
    }

    @Test
    void stopBeforeStartThrows() {
        assertThrows(IllegalArgumentException.class, () -> MenuKit.INSTANCE.stop());
    }

    @Test
    void stopRetiresTheMenuManager() {
        MenuKit.INSTANCE.start(this.plugin);

        MenuKit.INSTANCE.stop();

        assertThrows(IllegalArgumentException.class, SlotMenuManager::menuManager);
    }

    @Test
    void menuKitCanBeStartedAgainAfterBeingStopped() {
        MenuKit.INSTANCE.start(this.plugin);
        MenuKit.INSTANCE.stop();

        assertDoesNotThrow(() -> MenuKit.INSTANCE.start(this.plugin), "a plugin re-enable must be able to start MenuKit again");

        assertNotNull(SlotMenuManager.menuManager());
        verify(this.pluginManager, times(2)).registerEvents(any(Listener.class), eq(this.plugin));
    }

    @Test
    void stoppingForgetsEveryTrackedMenu() {
        MenuKit.INSTANCE.start(this.plugin);
        final UUID viewerId = UUID.randomUUID();
        final Player viewer = mock(Player.class);
        when(viewer.getUniqueId()).thenReturn(viewerId);
        SlotMenuManager.menuManager().register(viewer, mock(SlotMenu.class));
        assertTrue(SlotMenuManager.menuManager().getMenu(viewerId).isPresent());

        MenuKit.INSTANCE.stop();
        MenuKit.INSTANCE.start(this.plugin);

        assertTrue(SlotMenuManager.menuManager().getMenu(viewerId).isEmpty(), "menus open at shutdown must not survive into the next start");
    }
}
