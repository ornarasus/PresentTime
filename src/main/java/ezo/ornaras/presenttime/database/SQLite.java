package ezo.ornaras.presenttime.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import ezo.ornaras.presenttime.PresentTime;

public class SQLite extends MySQL{

    public void Connect(String[] args) {
        try {
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s/db.sqlite",
                    PresentTime.singleton.getDataFolder().getAbsolutePath()), args[0], args[1]);
        } catch (SQLException ignored) {}
    }
}
