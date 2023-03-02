package ezo.ornaras.presenttime.database;

import org.bukkit.entity.Player;
import java.util.UUID;

public interface IDatabase {
    void Connect(String[] args);
    void Disconnect();
    void AddPlayer(UUID uuid, String utc);
    String GetUTC(Player player);
    void CreateTable();
}
