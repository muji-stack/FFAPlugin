package com.yourname.ffa;

import com.yourname.ffa.commands.FFACommand;
import com.yourname.ffa.commands.FFAAdminCommand;
import com.yourname.ffa.data.DataManager;
import com.yourname.ffa.data.PlayerStats;
import com.yourname.ffa.listeners.PlayerListener;
import com.yourname.ffa.managers.ArenaManager;
import com.yourname.ffa.managers.KillstreakManager;
import com.yourname.ffa.managers.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FFAPlugin extends JavaPlugin {
    
    private static FFAPlugin instance;
    private ArenaManager arenaManager;
    private DataManager dataManager;
    private KillstreakManager killstreakManager;
    private ScoreboardManager scoreboardManager;
    
    // プレイヤーがアリーナにいるかどうか
    private Map<UUID, Boolean> playersInArena;
    // プレイヤーの統計情報（キャッシュ）
    private Map<UUID, PlayerStats> playerStats;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // コンフィグを保存
        saveDefaultConfig();
        
        // マップの初期化
        playersInArena = new HashMap<>();
        playerStats = new HashMap<>();
        
        // マネージャーの初期化
        dataManager = new DataManager(this);
        arenaManager = new ArenaManager(this);
        killstreakManager = new KillstreakManager(this);
        scoreboardManager = new ScoreboardManager(this);
        
        // データベース接続
        dataManager.connect();
        dataManager.createTables();
        
        // コマンド登録
        getCommand("ffa").setExecutor(new FFACommand(this));
        getCommand("ffaadmin").setExecutor(new FFAAdminCommand(this));
        
        // リスナー登録
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // スコアボード更新タスク
        if (getConfig().getBoolean("scoreboard.enabled")) {
            scoreboardManager.startUpdateTask();
        }
        
        getLogger().info("FFAPlugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // アリーナにいる全プレイヤーを退出させる
        for (UUID uuid : new HashMap<>(playersInArena).keySet()) {
            arenaManager.removePlayer(getServer().getPlayer(uuid));
        }
        
        // データベース切断
        if (dataManager != null) {
            dataManager.disconnect();
        }
        
        // スコアボードタスクを停止
        if (scoreboardManager != null) {
            scoreboardManager.stopUpdateTask();
        }
        
        getLogger().info("FFAPlugin has been disabled!");
    }
    
    public static FFAPlugin getInstance() {
        return instance;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public KillstreakManager getKillstreakManager() {
        return killstreakManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public Map<UUID, Boolean> getPlayersInArena() {
        return playersInArena;
    }
    
    public Map<UUID, PlayerStats> getPlayerStats() {
        return playerStats;
    }
    
    public String colorize(String message) {
        return message.replace("&", "§");
    }
}
