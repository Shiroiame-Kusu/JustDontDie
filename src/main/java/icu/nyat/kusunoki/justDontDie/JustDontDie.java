package icu.nyat.kusunoki.justDontDie;

import icu.nyat.kusunoki.justDontDie.Events.PlayerDeathListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustDontDie extends JavaPlugin {
    private boolean disableVanillaTotem;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPluginConfig();

        new PlayerDeathListener(this);

    }
    public void reloadPluginConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        this.disableVanillaTotem = config.getBoolean("disable-vanilla-totem", true);
    }

    public boolean isVanillaTotemDisabled() {
        return disableVanillaTotem;
    }
    @Override
    public void onDisable() {

    }
}
