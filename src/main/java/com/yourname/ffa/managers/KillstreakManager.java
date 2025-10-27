package com.yourname.ffa.managers;

import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.data.PlayerStats;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class KillstreakManager {
    
    private FFAPlugin plugin;
    
    public KillstreakManager(FFAPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void checkKillstreak(Player player) {
        if (!plugin.getConfig().getBoolean("killstreaks.enabled", true)) {
            return;
        }
        
        PlayerStats stats = plugin.getPlayerStats().get(player.getUniqueId());
        if (stats == null) {
            return;
        }
        
        int streak = stats.getCurrentStreak();
        
        // 設定からキルストリークの報酬を取得
        ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("killstreaks.rewards");
        if (rewardsSection == null) {
            return;
        }
        
        // 現在のストリークに報酬があるかチェック
        if (rewardsSection.contains(String.valueOf(streak))) {
            ConfigurationSection streakSection = rewardsSection.getConfigurationSection(String.valueOf(streak));
            
            // メッセージを送信
            String message = streakSection.getString("message");
            if (message != null) {
                player.sendMessage(plugin.colorize(message));
            }
            
            // アイテムを付与
            if (streakSection.contains("items")) {
                List<?> items = streakSection.getList("items");
                if (items != null) {
                    for (Object itemObj : items) {
                        if (itemObj instanceof ConfigurationSection) {
                            ConfigurationSection itemSection = (ConfigurationSection) itemObj;
                            giveRewardItem(player, itemSection);
                        }
                    }
                }
            }
            
            // サウンドを再生（1.8.9互換）
            player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
        }
    }
    
    private void giveRewardItem(Player player, ConfigurationSection itemSection) {
        String typeName = itemSection.getString("type");
        Material type = Material.getMaterial(typeName);
        
        if (type == null) {
            plugin.getLogger().warning("Invalid material type: " + typeName);
            return;
        }
        
        int amount = itemSection.getInt("amount", 1);
        int data = itemSection.getInt("data", 0);
        
        ItemStack item = new ItemStack(type, amount, (short) data);
        
        // インベントリに追加
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            // インベントリが満杯の場合はドロップ
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage(plugin.colorize("&eインベントリが満杯だったため、アイテムをドロップしました！"));
        }
    }
    
    public void announceKillstreak(Player player, int streak) {
        // 特定のキルストリークで全体アナウンス
        String message = null;
        
        switch (streak) {
            case 3:
                message = "&e" + player.getName() + " &7が &e3連続キル &7を達成！";
                break;
            case 5:
                message = "&6" + player.getName() + " &7が &6" + "5連続キル &7を達成！";
                break;
            case 10:
                message = "&c" + player.getName() + " &7が &c10連続キル &7を達成！";
                break;
            case 15:
                message = "&4" + player.getName() + " &7が &4&l15連続キル &7を達成！無敵状態！";
                break;
            case 20:
                message = "&5&l" + player.getName() + " &7が &5&l20連続キル &7を達成！伝説的！";
                break;
        }
        
        if (message != null) {
            plugin.getServer().broadcastMessage(plugin.colorize(message));
        }
    }
}
