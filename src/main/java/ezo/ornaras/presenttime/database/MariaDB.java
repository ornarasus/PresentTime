package ezo.ornaras.presenttime.database;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDB extends MySQL{
    public void Connect(String[] args) {
        try {
            connection = DriverManager.getConnection(
                    String.format("jdbc:mariadb://%s:%s/%s", args[0], args[1], args[2]),
                    args[3], args[4]);
        } catch (SQLException ignored) {}
    }
}