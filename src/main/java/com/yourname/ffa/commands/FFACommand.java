package com.yourname.ffa.commands;

import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.data.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FFACommand implements CommandExecutor {
    
    private FFAPlugin plugin;
    
    public FFACommand(FFAPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cこのコマンドはプレイヤーのみ実行できます！"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                plugin.getArenaManager().addPlayer(player);
                break;
                
            case "leave":
                plugin.getArenaManager().removePlayer(player);
                break;
                
            case "stats":
                if (args.length == 1) {
                    showStats(player, player);
                } else if (args.length == 2) {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(plugin.colorize("&cプレイヤーが見つかりません！"));
                        return true;
                    }
                    showStats(player, target);
                }
                break;
                
            case "top":
            case "leaderboard":
                showLeaderboard(player);
                break;
                
            case "setspawn":
                if (!player.hasPermission("ffa.admin")) {
                    player.sendMessage(plugin.colorize("&cこのコマンドを実行する権限がありません！"));
                    return true;
                }
                plugin.getArenaManager().addSpawn(player.getLocation());
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.spawn-set")));
                break;
                
            case "setlobby":
                if (!player.hasPermission("ffa.admin")) {
                    player.sendMessage(plugin.colorize("&cこのコマンドを実行する権限がありません！"));
                    return true;
                }
                plugin.getArenaManager().setLobby(player.getLocation());
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.lobby-set")));
                break;
                
            case "enable":
                if (!player.hasPermission("ffa.admin")) {
                    player.sendMessage(plugin.colorize("&cこのコマンドを実行する権限がありません！"));
                    return true;
                }
                plugin.getArenaManager().setEnabled(true);
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.arena-enabled")));
                break;
                
            case "disable":
                if (!player.hasPermission("ffa.admin")) {
                    player.sendMessage(plugin.colorize("&cこのコマンドを実行する権限がありません！"));
                    return true;
                }
                plugin.getArenaManager().setEnabled(false);
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.arena-disabled-admin")));
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(plugin.colorize("&6===== &eFFA コマンド &6====="));
        player.sendMessage(plugin.colorize("&e/ffa join &7- アリーナに参加"));
        player.sendMessage(plugin.colorize("&e/ffa leave &7- アリーナから退出"));
        player.sendMessage(plugin.colorize("&e/ffa stats [プレイヤー] &7- 統計を表示"));
        player.sendMessage(plugin.colorize("&e/ffa top &7- ランキングを表示"));
        
        if (player.hasPermission("ffa.admin")) {
            player.sendMessage(plugin.colorize("&c===== &4管理者コマンド &c====="));
            player.sendMessage(plugin.colorize("&c/ffa setspawn &7- スポーン地点を設定"));
            player.sendMessage(plugin.colorize("&c/ffa setlobby &7- ロビー地点を設定"));
            player.sendMessage(plugin.colorize("&c/ffa enable &7- アリーナを有効化"));
            player.sendMessage(plugin.colorize("&c/ffa disable &7- アリーナを無効化"));
        }
    }
    
    private void showStats(Player sender, Player target) {
        PlayerStats stats = plugin.getPlayerStats().get(target.getUniqueId());
        if (stats == null) {
            stats = plugin.getDataManager().loadPlayerStats(target.getUniqueId(), target.getName());
        }
        
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-header")));
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-kills")
            .replace("{kills}", String.valueOf(stats.getKills()))));
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-deaths")
            .replace("{deaths}", String.valueOf(stats.getDeaths()))));
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-kd")
            .replace("{kd}", String.valueOf(stats.getKDRatio()))));
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-killstreak")
            .replace("{streak}", String.valueOf(stats.getCurrentStreak()))));
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.stats-best-streak")
            .replace("{best}", String.valueOf(stats.getBestStreak()))));
    }
    
    private void showLeaderboard(Player player) {
        List<PlayerStats> topPlayers = plugin.getDataManager().getTopPlayers(10);
        
        player.sendMessage(plugin.colorize("&6===== &eトップ10ランキング &6====="));
        
        if (topPlayers.isEmpty()) {
            player.sendMessage(plugin.colorize("&cまだデータがありません！"));
            return;
        }
        
        int rank = 1;
        for (PlayerStats stats : topPlayers) {
            String rankColor = getRankColor(rank);
            player.sendMessage(plugin.colorize(
                rankColor + "#" + rank + " &f" + stats.getPlayerName() + 
                " &7- &eキル: &a" + stats.getKills() + 
                " &7| &eK/D: &b" + stats.getKDRatio() +
                " &7| &e最高連続: &6" + stats.getBestStreak()
            ));
            rank++;
        }
    }
    
    private String getRankColor(int rank) {
        switch (rank) {
            case 1:
                return "&6"; // ゴールド
            case 2:
                return "&7"; // シルバー
            case 3:
                return "&c"; // ブロンズ
            default:
                return "&e"; // 黄色
        }
    }
}
