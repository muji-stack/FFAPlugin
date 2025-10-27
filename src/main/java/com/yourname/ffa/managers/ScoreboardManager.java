package com.yourname.ffa.managers;

import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.data.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
    
    private FFAPlugin plugin;
    private int taskId = -1;
    
    public ScoreboardManager(FFAPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void setScoreboard(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ffa", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(plugin.colorize(plugin.getConfig().getString("scoreboard.title")));
        
        player.setScoreboard(scoreboard);
        updateScoreboard(player);
    }
    
    public void updateScoreboard(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }
        
        if (!plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
            return;
        }
        
        PlayerStats stats = plugin.getPlayerStats().get(player.getUniqueId());
        if (stats == null) {
            return;
        }
        
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            setScoreboard(player);
            return;
        }
        
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            setScoreboard(player);
            return;
        }
        
        // スコアをクリア
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        // 新しいスコアを設定
        int score = 10;
        
        setScore(objective, plugin.colorize("&7&m-----------------"), score--);
        setScore(objective, plugin.colorize("&fプレイヤー: &a" + player.getName()), score--);
        setScore(objective, plugin.colorize(""), score--);
        setScore(objective, plugin.colorize("&eキル: &a" + stats.getKills()), score--);
        setScore(objective, plugin.colorize("&eデス: &c" + stats.getDeaths()), score--);
        setScore(objective, plugin.colorize("&eK/D: &b" + stats.getKDRatio()), score--);
        setScore(objective, plugin.colorize(" "), score--);
        setScore(objective, plugin.colorize("&6連続キル: &e" + stats.getCurrentStreak()), score--);
        setScore(objective, plugin.colorize("&6最高記録: &e" + stats.getBestStreak()), score--);
        setScore(objective, plugin.colorize("&7&m-----------------"), score--);
    }
    
    private void setScore(Objective objective, String text, int score) {
        Score s = objective.getScore(text);
        s.setScore(score);
    }
    
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    public void startUpdateTask() {
        int interval = plugin.getConfig().getInt("scoreboard.update-interval", 20);
        
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
                        updateScoreboard(player);
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval).getTaskId();
    }
    
    public void stopUpdateTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
