package com.yourname.ffa.managers;

import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.data.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaManager {
    
    private FFAPlugin plugin;
    private Random random;
    
    public ArenaManager(FFAPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }
    
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("arena.enabled", false);
    }
    
    public void setEnabled(boolean enabled) {
        plugin.getConfig().set("arena.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void addPlayer(Player player) {
        if (!isEnabled()) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.arena-disabled")));
            return;
        }
        
        if (plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.colorize("&c既にアリーナに参加しています！"));
            return;
        }
        
        // プレイヤーの統計をロード
        PlayerStats stats = plugin.getDataManager().loadPlayerStats(player.getUniqueId(), player.getName());
        plugin.getPlayerStats().put(player.getUniqueId(), stats);
        
        // アリーナに追加
        plugin.getPlayersInArena().put(player.getUniqueId(), true);
        
        // スポーン地点にテレポート
        Location spawn = getRandomSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }
        
        // キットを付与
        giveKit(player);
        
        // 体力と空腹度を回復
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // ポーション効果をクリア
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // スコアボードを設定
        plugin.getScoreboardManager().setScoreboard(player);
        
        // 参加メッセージ
        String joinMsg = plugin.getConfig().getString("messages.join")
            .replace("{player}", player.getName());
        Bukkit.broadcastMessage(plugin.colorize(joinMsg));
    }
    
    public void removePlayer(Player player) {
        if (!plugin.getPlayersInArena().containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.not-in-arena")));
            return;
        }
        
        // 統計を保存
        PlayerStats stats = plugin.getPlayerStats().get(player.getUniqueId());
        if (stats != null) {
            plugin.getDataManager().savePlayerStats(stats);
        }
        
        // アリーナから削除
        plugin.getPlayersInArena().remove(player.getUniqueId());
        plugin.getPlayerStats().remove(player.getUniqueId());
        
        // ロビーにテレポート
        Location lobby = getLobby();
        if (lobby != null) {
            player.teleport(lobby);
        }
        
        // インベントリをクリア
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        // 体力と空腹度を回復
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // ポーション効果をクリア
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // スコアボードをリセット
        plugin.getScoreboardManager().removeScoreboard(player);
        
        // 退出メッセージ
        String leaveMsg = plugin.getConfig().getString("messages.leave")
            .replace("{player}", player.getName());
        Bukkit.broadcastMessage(plugin.colorize(leaveMsg));
    }
    
    public void giveKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        
        // 防具を装備
        inv.setHelmet(getArmorPiece(plugin.getConfig().getString("kit.helmet")));
        inv.setChestplate(getArmorPiece(plugin.getConfig().getString("kit.chestplate")));
        inv.setLeggings(getArmorPiece(plugin.getConfig().getString("kit.leggings")));
        inv.setBoots(getArmorPiece(plugin.getConfig().getString("kit.boots")));
        
        // アイテムを付与
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("kit.items");
        if (itemsSection != null) {
            List<String> itemKeys = new ArrayList<>(itemsSection.getKeys(false));
            
            for (String key : itemKeys) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;
                
                String typeName = itemSection.getString("type");
                Material type = Material.getMaterial(typeName);
                if (type == null) continue;
                
                int slot = itemSection.getInt("slot", 0);
                int amount = itemSection.getInt("amount", 1);
                int data = itemSection.getInt("data", 0);
                
                ItemStack item = new ItemStack(type, amount, (short) data);
                
                // エンチャントを追加
                if (itemSection.contains("enchantments")) {
                    List<String> enchants = itemSection.getStringList("enchantments");
                    for (String enchantStr : enchants) {
                        String[] parts = enchantStr.split(":");
                        if (parts.length == 2) {
                            Enchantment enchant = Enchantment.getByName(parts[0]);
                            int level = Integer.parseInt(parts[1]);
                            if (enchant != null) {
                                item.addUnsafeEnchantment(enchant, level);
                            }
                        }
                    }
                }
                
                inv.setItem(slot, item);
            }
        }
    }
    
    private ItemStack getArmorPiece(String materialName) {
        Material material = Material.getMaterial(materialName);
        if (material != null) {
            return new ItemStack(material);
        }
        return null;
    }
    
    public Location getRandomSpawn() {
        List<?> spawns = plugin.getConfig().getList("arena.spawns");
        if (spawns == null || spawns.isEmpty()) {
            return null;
        }
        
        int index = random.nextInt(spawns.size());
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("arena.spawns." + index);
        
        if (spawnSection != null) {
            String worldName = spawnSection.getString("world");
            double x = spawnSection.getDouble("x");
            double y = spawnSection.getDouble("y");
            double z = spawnSection.getDouble("z");
            float yaw = (float) spawnSection.getDouble("yaw");
            float pitch = (float) spawnSection.getDouble("pitch");
            
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        }
        
        return null;
    }
    
    public void addSpawn(Location location) {
        List<Object> spawns = (List<Object>) plugin.getConfig().getList("arena.spawns", new ArrayList<>());
        
        int index = spawns.size();
        plugin.getConfig().set("arena.spawns." + index + ".world", location.getWorld().getName());
        plugin.getConfig().set("arena.spawns." + index + ".x", location.getX());
        plugin.getConfig().set("arena.spawns." + index + ".y", location.getY());
        plugin.getConfig().set("arena.spawns." + index + ".z", location.getZ());
        plugin.getConfig().set("arena.spawns." + index + ".yaw", location.getYaw());
        plugin.getConfig().set("arena.spawns." + index + ".pitch", location.getPitch());
        
        plugin.saveConfig();
    }
    
    public Location getLobby() {
        ConfigurationSection lobbySection = plugin.getConfig().getConfigurationSection("arena.lobby");
        if (lobbySection != null) {
            String worldName = lobbySection.getString("world");
            double x = lobbySection.getDouble("x");
            double y = lobbySection.getDouble("y");
            double z = lobbySection.getDouble("z");
            float yaw = (float) lobbySection.getDouble("yaw");
            float pitch = (float) lobbySection.getDouble("pitch");
            
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        }
        return null;
    }
    
    public void setLobby(Location location) {
        plugin.getConfig().set("arena.lobby.world", location.getWorld().getName());
        plugin.getConfig().set("arena.lobby.x", location.getX());
        plugin.getConfig().set("arena.lobby.y", location.getY());
        plugin.getConfig().set("arena.lobby.z", location.getZ());
        plugin.getConfig().set("arena.lobby.yaw", location.getYaw());
        plugin.getConfig().set("arena.lobby.pitch", location.getPitch());
        
        plugin.saveConfig();
    }
}
