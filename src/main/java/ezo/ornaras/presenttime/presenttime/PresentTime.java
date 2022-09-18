package ezo.ornaras.presenttime.presenttime;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class PresentTime extends JavaPlugin {

    public Map<UUID,BukkitTask> clients = new HashMap<>();

    public void createConfig(){
        File f = new File(getDataFolder() + File.separator + "config.yml");
        if (!f.exists()) {
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
    }

    @Override
    public void onEnable() {
        createConfig();
        Metrics metrics = new Metrics(this, 16453);
        getServer().getPluginManager().registerEvents(new Events(this), this);
    }

    @Override
    public void onDisable() {
        try {
            getConfig().save(getDataFolder() + File.separator + "config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
