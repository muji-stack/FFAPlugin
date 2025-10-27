package com.yourname.ffa.listeners;

import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.data.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    
    private FFAPlugin plugin;
    
    public PlayerListener(FFAPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // プレイヤーの統計をロード
        PlayerStats stats = plugin.getDataManager().loadPlayerStats(player.getUniqueId(), player.getName());
        plugin.getPlayerStats().put(player.getUniqueId(), stats);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // アリーナにいる場合は退出させる
        if (plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
            plugin.getArenaManager().removePlayer(player);
        }
        
        // 統計を保存
        PlayerStats stats = plugin.getPlayerStats().get(player.getUniqueId());
        if (stats != null) {
            plugin.getDataManager().savePlayerStats(stats);
            plugin.getPlayerStats().remove(player.getUniqueId());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // アリーナにいない場合は処理しない
        if (!plugin.getPlayersInArena().containsKey(victim.getUniqueId())) {
            return;
        }
        
        // デスメッセージをカスタマイズ
        event.setDeathMessage(null);
        
        // ドロップアイテムをクリア
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // 被害者の統計を更新
        PlayerStats victimStats = plugin.getPlayerStats().get(victim.getUniqueId());
        if (victimStats != null) {
            victimStats.addDeath();
            plugin.getDataManager().savePlayerStats(victimStats);
        }
        
        // キラーがいる場合
        if (killer != null && plugin.getPlayersInArena().containsKey(killer.getUniqueId())) {
            // キラーの統計を更新
            PlayerStats killerStats = plugin.getPlayerStats().get(killer.getUniqueId());
            if (killerStats != null) {
                killerStats.addKill();
                plugin.getDataManager().savePlayerStats(killerStats);
                
                // キルストリークをチェック
                plugin.getKillstreakManager().checkKillstreak(killer);
                plugin.getKillstreakManager().announceKillstreak(killer, killerStats.getCurrentStreak());
                
                // スコアボードを更新
                plugin.getScoreboardManager().updateScoreboard(killer);
            }
            
            // キルメッセージ
            String killMsg = plugin.getConfig().getString("messages.kill")
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName());
            Bukkit.broadcastMessage(plugin.colorize(killMsg));
        }
        
        // スコアボードを更新
        plugin.getScoreboardManager().updateScoreboard(victim);
        
        // デスメッセージを表示
        int delay = plugin.getConfig().getInt("respawn.delay", 3);
        String deathMsg = plugin.getConfig().getString("messages.death")
            .replace("{delay}", String.valueOf(delay));
        victim.sendMessage(plugin.colorize(deathMsg));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // アリーナにいない場合は処理しない
        if (!plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
            return;
        }
        
        // リスポーン遅延
        int delay = plugin.getConfig().getInt("respawn.delay", 3);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
                    return;
                }
                
                // ランダムスポーンにテレポート
                Location spawn = plugin.getArenaManager().getRandomSpawn();
                if (spawn != null) {
                    player.teleport(spawn);
                }
                
                // キットを付与
                plugin.getArenaManager().giveKit(player);
                
                // 体力と空腹度を回復
                double health = plugin.getConfig().getDouble("respawn.health", 20.0);
                int food = plugin.getConfig().getInt("respawn.food", 20);
                
                player.setHealth(health);
                player.setFoodLevel(food);
                player.setSaturation(20.0f);
            }
        }.runTaskLater(plugin, delay * 20L);
        
        // リスポーン地点を一時的に設定（即座にテレポートさせないため）
        Location spawn = plugin.getArenaManager().getRandomSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 被害者がプレイヤーでない場合は処理しない
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        
        // アリーナにいない場合は処理しない
        if (!plugin.getPlayersInArena().containsKey(victim.getUniqueId())) {
            return;
        }
        
        // 攻撃者がプレイヤーでない場合は処理しない
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        
        // 攻撃者がアリーナにいない場合はキャンセル
        if (!plugin.getPlayersInArena().containsKey(attacker.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
