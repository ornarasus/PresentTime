package ezo.ornaras.presenttime.database;

import ezo.ornaras.presenttime.Events;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL implements IDatabase{
    Connection connection;
    @Override
    public void Connect(String[] args) {
        try {
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", args[0], args[1], args[2]),
                    args[3], args[4]);
        } catch (SQLException ignored) {}
    }

    @Override
    public void Disconnect() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException ignored) {}
    }

    @Override
    public void AddPlayer(UUID uuid, String utc) {
        try {
            String sql = "INSERT INTO " +
                    "PresentTime(Player, UTC) VALUES (?, ?)";
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, uuid.toString());
            st.setString(2, utc);
            st.executeUpdate();
        } catch (SQLException ignored) {}
    }

    @Override
    public String GetUTC(Player player) {
        String result;
        try {
            String sql = "SELECT UTC FROM PresentTime WHERE Player=?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, player.getUniqueId().toString());
            result = stmt.executeQuery().getString("UTC");
            if(result == null)
                result = Events.singleton.newUTC(player);
        } catch (SQLException e) {
            result = Events.singleton.newUTC(player);
        }
        return result;
    }

    @Override
    public void CreateTable() {
        try {
            String sql = "create table if not exists PresentTime(Player CHAR(36), UTC CHAR(9))";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
