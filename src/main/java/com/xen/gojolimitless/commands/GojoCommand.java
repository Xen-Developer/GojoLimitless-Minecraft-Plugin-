package com.xen.gojolimitless.commands;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GojoCommand implements CommandExecutor, TabCompleter {

    private final GojoLimitlessPlugin plugin;

    public GojoCommand(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gojo give <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return true;
                }
                plugin.getKitManager().giveBaseKit(target);
                plugin.getPlayerDataManager().get(target); 
                sender.sendMessage(ChatColor.AQUA + "Gave Gojo's base kit to " + target.getName() + ".");
            }
            case "awaken" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gojo awaken <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return true;
                }
                var data = plugin.getPlayerDataManager().get(target);
                data.setAwakenBar(plugin.getPlayerDataManager().maxBar(), plugin.getPlayerDataManager().maxBar());
                data.setReadyToAwaken(true);
                boolean ok = plugin.getAwakenManager().tryAwaken(target);
                sender.sendMessage(ok
                        ? ChatColor.LIGHT_PURPLE + "Force-awakened " + target.getName() + "."
                        : ChatColor.RED + "Could not awaken " + target.getName() + " (already awakened?).");
            }
            case "bar" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gojo bar <player> <0-100>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return true;
                }
                double value;
                try {
                    value = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Bar value must be a number between 0 and 100.");
                    return true;
                }
                var data = plugin.getPlayerDataManager().get(target);
                var pdm = plugin.getPlayerDataManager();
                data.setAwakenBar(value, pdm.maxBar());
                data.setReadyToAwaken(value >= pdm.maxBar());
                pdm.refreshBossBar(target, data);
                sender.sendMessage(ChatColor.AQUA + "Set " + target.getName() + "'s awaken bar to " + value + ".");
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gojo reset <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return true;
                }
                plugin.getPlayerDataManager().resetPlayer(target);
                sender.sendMessage(ChatColor.AQUA + "Reset cooldowns and awaken bar for " + target.getName() + ".");
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "GojoLimitless config reloaded.");
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- GojoLimitless ---");
        sender.sendMessage(ChatColor.YELLOW + "/gojo give <player>" + ChatColor.GRAY + " - give the base skill kit");
        sender.sendMessage(ChatColor.YELLOW + "/gojo awaken <player>" + ChatColor.GRAY + " - force Awaken Mode");
        sender.sendMessage(ChatColor.YELLOW + "/gojo bar <player> <0-100>" + ChatColor.GRAY + " - set awaken bar");
        sender.sendMessage(ChatColor.YELLOW + "/gojo reset <player>" + ChatColor.GRAY + " - clear cooldowns/bar");
        sender.sendMessage(ChatColor.YELLOW + "/gojo reload" + ChatColor.GRAY + " - reload config.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "awaken", "bar", "reset", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && List.of("give", "awaken", "bar", "reset").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
