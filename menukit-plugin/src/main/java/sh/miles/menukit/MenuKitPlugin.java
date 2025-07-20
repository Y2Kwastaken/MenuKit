package sh.miles.menukit;

import org.bukkit.plugin.java.JavaPlugin;

public final class MenuKitPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        MenuKit.INSTANCE.start(this);
        getLogger().info("Successfully Started MenuKit [Plugin]");
    }

    @Override
    public void onDisable() {
        MenuKit.INSTANCE.stop();
        getLogger().info("Successfully shut down MenuKit [Plugin]");
    }
}
