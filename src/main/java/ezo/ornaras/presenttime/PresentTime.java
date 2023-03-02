package ezo.ornaras.presenttime;

import ezo.ornaras.presenttime.database.*;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class PresentTime extends JavaPlugin {

    public Map<UUID,BukkitTask> clients = new HashMap<>();
    public IDatabase db;
    public static PresentTime singleton;

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
        singleton = this;
        switch (getConfig().getString("database.type").toLowerCase()){
            case "mysql":
                db = new MySQL();
                break;
            case "mariadb":
                db = new MariaDB();
                break;
            case "postgresql":
                db = new PostgreSQL();
                break;
            default:
                db = new SQLite();
                break;
        }
        if (db.getClass().equals(SQLite.class)) {
            db.Connect(new String[]{
                    getConfig().getString("database.user"),
                    getConfig().getString("database.pass")});
        }else {
            db.Connect(new String[]{
                    getConfig().getString("database.host"),
                    getConfig().getString("database.port"),
                    getConfig().getString("database.name"),
                    getConfig().getString("database.user"),
                    getConfig().getString("database.pass")});
        }
        db.CreateTable();
        for (World w: getServer().getWorlds()) {
            if(!getConfig().getStringList("disabledWorld").contains(w.getName()))
                w.setGameRuleValue("doDaylightCycle", "false");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World w: getServer().getWorlds()) {
                    if(!getConfig().getStringList("disabledWorld").contains(w.getName()))
                        w.setTime(w.getTime()==6000?18000:6000);
                }
            }
        }.runTaskTimer(this,0L, 20L*getConfig().getInt("delayDayCycle"));
        getServer().getPluginManager().registerEvents(new Events(), this);

    }

    @Override
    public void onDisable() {
        for (World w: getServer().getWorlds()) {
            if(!getConfig().getStringList("disabledWorld").contains(w.getName()))
                w.setGameRuleValue("doDaylightCycle", "true");
        }
        db.Disconnect();
        try {
            getConfig().save(getDataFolder() + File.separator + "config.yml");
        } catch (IOException ignored) {}
    }
}
