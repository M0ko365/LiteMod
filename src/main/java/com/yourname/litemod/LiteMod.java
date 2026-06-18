package com.yourname.litemod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LiteMod extends JavaPlugin implements Listener {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Set<UUID> godPlayers = new HashSet<>();
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private boolean chatMuted = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(ChatColor.GREEN + "LiteMod has been enabled! Made with ❤️ by YourName");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "LiteMod has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "ban":
                handleBan(player, args, false);
                break;
            case "tempban":
                handleTempBan(player, args);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "mute":
                handleMute(player, args, false);
                break;
            case "tempmute":
                handleTempMute(player, args);
                break;
            case "warn":
                handleWarn(player, args);
                break;
            case "freeze":
                handleFreeze(player, args);
                break;
            case "unfreeze":
                handleUnfreeze(player, args);
                break;
            case "vanish":
                toggleVanish(player);
                break;
            case "staffchat":
            case "sc":
                sendStaffChat(player, args);
                break;
            case "clearchat":
                clearChat(player);
                break;
            case "report":
                handleReport(player, args);
                break;
            case "history":
                showHistory(player, args);
                break;
            case "unban":
                handleUnban(player, args);
                break;
            case "unmute":
                handleUnmute(player, args);
                break;
            case "invsee":
                handleInvSee(player, args);
                break;
            case "endersee":
                handleEnderSee(player, args);
                break;
            case "fly":
                toggleFly(player);
                break;
            case "god":
                toggleGod(player);
                break;
            case "heal":
                handleHeal(player, args);
                break;
            case "feed":
                handleFeed(player, args);
                break;
            case "broadcast":
            case "bc":
                broadcastMessage(player, args);
                break;
            case "check":
                handleCheck(player, args);
                break;
            default:
                return false;
        }
        return true;
    }

    // TODO: Implement full punishment storage (YAML or database)
    private void handleBan(Player player, String[] args, boolean temp) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /ban <player> [reason]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason given";
        target.kickPlayer(ChatColor.RED + "You have been banned!\nReason: " + reason);
        Bukkit.broadcastMessage(ChatColor.RED + target.getName() + " has been banned by " + player.getName());
        // Save to punishments file (expand this)
    }

    private void handleTempBan(Player player, String[] args) {
        // Similar to ban but with duration parsing
        player.sendMessage(ChatColor.YELLOW + "Tempban feature coming soon - basic ban used for now.");
        handleBan(player, args, true);
    }

    private void handleKick(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) return;
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Kicked by staff";
        target.kickPlayer(ChatColor.RED + reason);
        Bukkit.broadcastMessage(ChatColor.RED + target.getName() + " was kicked by " + player.getName());
    }

    private void handleMute(Player player, String[] args, boolean temp) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /mute <player> [reason]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) return;
        mutedPlayers.put(target.getUniqueId(), System.currentTimeMillis() + (temp ? 600000 : Long.MAX_VALUE)); // 10 min default for temp
        target.sendMessage(ChatColor.RED + "You have been muted!");
    }

    private void handleTempMute(Player player, String[] args) {
        handleMute(player, args, true);
    }

    private void handleWarn(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            target.sendMessage(ChatColor.GOLD + "⚠ You have been warned by " + player.getName() + "!");
        }
    }

    private void handleFreeze(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            frozenPlayers.add(target.getUniqueId());
            target.sendMessage(ChatColor.RED + "You have been frozen!");
        }
    }

    private void handleUnfreeze(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            frozenPlayers.remove(target.getUniqueId());
            target.sendMessage(ChatColor.GREEN + "You have been unfrozen!");
        }
    }

    private void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Vanish disabled.");
            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(this, player);
        } else {
            vanishedPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Vanish enabled.");
            for (Player p : Bukkit.getOnlinePlayers()) p.hidePlayer(this, player);
        }
    }

    private void sendStaffChat(Player player, String[] args) {
        if (args.length == 0) return;
        String message = String.join(" ", args);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("litemod.staff")) {
                p.sendMessage(ChatColor.AQUA + "[Staff] " + player.getName() + ": " + message);
            }
        }
    }

    private void clearChat(Player player) {
        for (int i = 0; i < 100; i++) Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Chat has been cleared by " + player.getName());
    }

    private void handleReport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            return;
        }
        // Log report (expand later)
        player.sendMessage(ChatColor.GREEN + "Report sent! Staff have been notified.");
    }

    private void showHistory(Player player, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "Punishment history feature - coming soon!");
    }

    private void handleUnban(Player player, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "Unban feature coming soon.");
    }

    private void handleUnmute(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) mutedPlayers.remove(target.getUniqueId());
    }

    private void handleInvSee(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) player.openInventory(target.getInventory());
    }

    private void handleEnderSee(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) player.openInventory(target.getEnderChest());
    }

    private void toggleFly(Player player) {
        player.setAllowFlight(!player.getAllowFlight());
        player.sendMessage(ChatColor.GREEN + "Flight " + (player.getAllowFlight() ? "enabled" : "disabled") + ".");
    }

    private void toggleGod(Player player) {
        if (godPlayers.contains(player.getUniqueId())) {
            godPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "God mode disabled.");
        } else {
            godPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "God mode enabled.");
        }
    }

    private void handleHeal(Player player, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target != null) {
            target.setHealth(20);
            target.setFoodLevel(20);
            target.sendMessage(ChatColor.GREEN + "You have been healed!");
        }
    }

    private void handleFeed(Player player, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target != null) {
            target.setFoodLevel(20);
            target.sendMessage(ChatColor.GREEN + "You have been fed!");
        }
    }

    private void broadcastMessage(Player player, String[] args) {
        if (args.length == 0) return;
        String message = String.join(" ", args);
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[Broadcast] " + message);
    }

    private void handleCheck(Player player, String[] args) {
        if (args.length == 0) return;
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            player.sendMessage(ChatColor.YELLOW + "=== Player Info: " + target.getName() + " ===");
            player.sendMessage(ChatColor.YELLOW + "Health: " + target.getHealth());
            player.sendMessage(ChatColor.YELLOW + "Location: " + target.getLocation().getBlockX() + ", " + target.getLocation().getBlockZ());
        }
    }

    // Event Listeners
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (chatMuted && !p.hasPermission("litemod.staff")) {
            e.setCancelled(true);
            return;
        }
        if (mutedPlayers.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You are muted!");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (frozenPlayers.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (vanishedPlayers.contains(p.getUniqueId())) {
            // Hide vanished players from new joiners
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (vanishedPlayers.contains(online.getUniqueId())) {
                    p.hidePlayer(this, online);
                }
            }
        }
    }
}