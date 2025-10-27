package com.yourname.ffa.commands;

import com.yourname.ffa.FFAPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FFAAdminCommand implements CommandExecutor {
    
    private FFAPlugin plugin;
    
    public FFAAdminCommand(FFAPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ffa.admin")) {
            sender.sendMessage(plugin.colorize("&cこのコマンドを実行する権限がありません！"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(plugin.colorize("&aコンフィグをリロードしました！"));
                break;
                
            case "setkillstreak":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.colorize("&cこのコマンドはプレイヤーのみ実行できます！"));
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage(plugin.colorize("&c使用方法: /ffaadmin setkillstreak <プレイヤー> <キル数>"));
                    return true;
                }
                
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.colorize("&cプレイヤーが見つかりません！"));
                    return true;
                }
                
                try {
                    int kills = Integer.parseInt(args[2]);
                    if (plugin.getPlayerStats().containsKey(target.getUniqueId())) {
                        plugin.getPlayerStats().get(target.getUniqueId()).setCurrentStreak(kills);
                        sender.sendMessage(plugin.colorize("&a" + target.getName() + " のキルストリークを " + kills + " に設定しました！"));
                    } else {
                        sender.sendMessage(plugin.colorize("&c" + target.getName() + " はアリーナに参加していません！"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.colorize("&c無効な数値です！"));
                }
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&c===== &4FFA 管理者コマンド &c====="));
        sender.sendMessage(plugin.colorize("&c/ffaadmin reload &7- コンフィグをリロード"));
        sender.sendMessage(plugin.colorize("&c/ffaadmin setkillstreak <プレイヤー> <キル数> &7- キルストリークを設定"));
    }
}
