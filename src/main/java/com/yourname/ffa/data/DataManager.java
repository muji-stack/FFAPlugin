package com.yourname.ffa.data;

import com.yourname.ffa.FFAPlugin;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DataManager {
    
    private FFAPlugin plugin;
    private Connection connection;
    private String dbType;
    
    public DataManager(FFAPlugin plugin) {
        this.plugin = plugin;
        this.dbType = plugin.getConfig().getString("database.type", "sqlite");
    }
    
    public void connect() {
        try {
            if (dbType.equalsIgnoreCase("mysql")) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String database = plugin.getConfig().getString("database.mysql.database");
                String username = plugin.getConfig().getString("database.mysql.username");
                String password = plugin.getConfig().getString("database.mysql.password");
                
                connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username,
                    password
                );
            } else {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                
                File databaseFile = new File(dataFolder, "ffa_data.db");
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            }
            
            plugin.getLogger().info("Database connected successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database disconnected successfully!");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to disconnect from database: " + e.getMessage());
            }
        }
    }
    
    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16), " +
                    "kills INT DEFAULT 0, " +
                    "deaths INT DEFAULT 0, " +
                    "current_streak INT DEFAULT 0, " +
                    "best_streak INT DEFAULT 0" +
                    ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public PlayerStats loadPlayerStats(UUID uuid, String playerName) {
        String sql = "SELECT * FROM player_stats WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new PlayerStats(
                    uuid,
                    rs.getString("player_name"),
                    rs.getInt("kills"),
                    rs.getInt("deaths"),
                    rs.getInt("current_streak"),
                    rs.getInt("best_streak")
                );
            } else {
                // プレイヤーが存在しない場合は新規作成
                PlayerStats stats = new PlayerStats(uuid, playerName);
                savePlayerStats(stats);
                return stats;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player stats: " + e.getMessage());
            return new PlayerStats(uuid, playerName);
        }
    }
    
    public void savePlayerStats(PlayerStats stats) {
        String sql = "INSERT OR REPLACE INTO player_stats (uuid, player_name, kills, deaths, current_streak, best_streak) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        // MySQLの場合は異なる構文を使用
        if (dbType.equalsIgnoreCase("mysql")) {
            sql = "INSERT INTO player_stats (uuid, player_name, kills, deaths, current_streak, best_streak) " +
                 "VALUES (?, ?, ?, ?, ?, ?) " +
                 "ON DUPLICATE KEY UPDATE player_name=?, kills=?, deaths=?, current_streak=?, best_streak=?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, stats.getUuid().toString());
            stmt.setString(2, stats.getPlayerName());
            stmt.setInt(3, stats.getKills());
            stmt.setInt(4, stats.getDeaths());
            stmt.setInt(5, stats.getCurrentStreak());
            stmt.setInt(6, stats.getBestStreak());
            
            if (dbType.equalsIgnoreCase("mysql")) {
                stmt.setString(7, stats.getPlayerName());
                stmt.setInt(8, stats.getKills());
                stmt.setInt(9, stats.getDeaths());
                stmt.setInt(10, stats.getCurrentStreak());
                stmt.setInt(11, stats.getBestStreak());
            }
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<PlayerStats> getTopPlayers(int limit) {
        List<PlayerStats> topPlayers = new ArrayList<>();
        String sql = "SELECT * FROM player_stats ORDER BY kills DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                topPlayers.add(new PlayerStats(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("player_name"),
                    rs.getInt("kills"),
                    rs.getInt("deaths"),
                    rs.getInt("current_streak"),
                    rs.getInt("best_streak")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get top players: " + e.getMessage());
        }
        
        return topPlayers;
    }
}
