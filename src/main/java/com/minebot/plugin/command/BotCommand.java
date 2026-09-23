package com.minebot.plugin.command;

import com.minebot.plugin.MineBot;
import com.minebot.plugin.bot.BotManager;
import com.minebot.plugin.bot.BotTaskType;
import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all /bot commands.
 */
public class BotCommand implements CommandExecutor, TabCompleter {

    private final MineBot plugin;
    private final BotManager botManager;

    private static final List<String> SUB_COMMANDS = Arrays.asList(
            "spawn", "remove", "follow", "stay", "mine", "guard", "patrol", "farm",
            "list", "tp", "info"
    );

    public BotCommand(MineBot plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "spawn" -> handleSpawn(player, args);
            case "remove" -> handleRemove(player, args);
            case "follow" -> handleSetTask(player, args, BotTaskType.FOLLOW);
            case "stay" -> handleSetTask(player, args, BotTaskType.IDLE);
            case "mine" -> handleSetTask(player, args, BotTaskType.MINE);
            case "guard" -> handleSetTask(player, args, BotTaskType.GUARD);
            case "patrol" -> handleSetTask(player, args, BotTaskType.PATROL);
            case "farm" -> handleSetTask(player, args, BotTaskType.FARM);
            case "list" -> handleList(player);
            case "tp" -> handleTeleport(player, args);
            case "info" -> handleInfo(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bot spawn <name>");
            return;
        }

        String name = args[1];

        if (name.length() > 16) {
            player.sendMessage(ChatColor.RED + "Bot name must be 16 characters or less.");
            return;
        }

        if (botManager.getBot(name) != null) {
            player.sendMessage(ChatColor.RED + "A bot named '" + name + "' already exists!");
            return;
        }

        FakePlayer bot = botManager.spawnBot(name, player);
        if (bot != null) {
            player.sendMessage(ChatColor.GREEN + "Bot '" + name + "' spawned! Use /bot follow " + name + " to make it follow you.");
        } else {
            player.sendMessage(ChatColor.RED + "Could not spawn bot. You may have reached the limit (5 per player).");
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bot remove <name>");
            return;
        }

        String name = args[1];

        if (!botManager.isOwner(name, player) && !player.hasPermission("minebot.admin")) {
            player.sendMessage(ChatColor.RED + "You don't own a bot named '" + name + "'.");
            return;
        }

        if (botManager.removeBot(name)) {
            player.sendMessage(ChatColor.GREEN + "Bot '" + name + "' removed.");
        } else {
            player.sendMessage(ChatColor.RED + "Bot '" + name + "' not found.");
        }
    }

    private void handleSetTask(Player player, String[] args, BotTaskType taskType) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bot " + args[0] + " <name>");
            return;
        }

        String name = args[1];
        FakePlayer bot = botManager.getBot(name);

        if (bot == null) {
            player.sendMessage(ChatColor.RED + "Bot '" + name + "' not found.");
            return;
        }

        if (!bot.getOwner().getUniqueId().equals(player.getUniqueId()) && !player.hasPermission("minebot.admin")) {
            player.sendMessage(ChatColor.RED + "You don't own this bot.");
            return;
        }

        bot.setTask(taskType);
        if (taskType == BotTaskType.IDLE) {
            player.sendMessage(ChatColor.YELLOW + "Bot '" + name + "' is now staying in place.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Bot '" + name + "' is now " + taskType.getDescription().toLowerCase() + ".");
        }
    }

    private void handleList(Player player) {
        List<FakePlayer> playerBots = botManager.getPlayerBots(player);

        if (playerBots.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no bots. Use /bot spawn <name> to create one!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Your Bots ===");
        for (FakePlayer bot : playerBots) {
            String status = bot.getCurrentTaskType().getDisplayName();
            String alive = bot.isAlive() ? ChatColor.GREEN + "✔" : ChatColor.RED + "✘";
            player.sendMessage(ChatColor.AQUA + " " + bot.getName() +
                    ChatColor.GRAY + " - " + status +
                    " " + alive);
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bot tp <name>");
            return;
        }

        String name = args[1];
        FakePlayer bot = botManager.getBot(name);

        if (bot == null) {
            player.sendMessage(ChatColor.RED + "Bot '" + name + "' not found.");
            return;
        }

        if (!bot.getOwner().getUniqueId().equals(player.getUniqueId()) && !player.hasPermission("minebot.admin")) {
            player.sendMessage(ChatColor.RED + "You don't own this bot.");
            return;
        }

        if (bot.isAlive()) {
            bot.getEntity().teleport(player.getLocation().add(player.getLocation().getDirection().multiply(2)));
            player.sendMessage(ChatColor.GREEN + "Bot '" + name + "' teleported to you.");
        } else {
            player.sendMessage(ChatColor.RED + "Bot entity is not alive.");
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bot info <name>");
            return;
        }

        String name = args[1];
        FakePlayer bot = botManager.getBot(name);

        if (bot == null) {
            player.sendMessage(ChatColor.RED + "Bot '" + name + "' not found.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Bot Info: " + bot.getName() + " ===");
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + bot.getOwner().getName());
        player.sendMessage(ChatColor.GRAY + "Task: " + ChatColor.WHITE + bot.getCurrentTaskType().getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Status: " + ChatColor.WHITE + bot.getCurrentTaskType().getDescription());
        player.sendMessage(ChatColor.GRAY + "Alive: " + (bot.isAlive() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        if (bot.isAlive()) {
            player.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE +
                    String.format("%.1f, %.1f, %.1f",
                            bot.getEntity().getLocation().getX(),
                            bot.getEntity().getLocation().getY(),
                            bot.getEntity().getLocation().getZ()));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== MineBot Commands ===");
        player.sendMessage(ChatColor.AQUA + "/bot spawn <name>" + ChatColor.GRAY + " - Spawn a new bot");
        player.sendMessage(ChatColor.AQUA + "/bot remove <name>" + ChatColor.GRAY + " - Remove a bot");
        player.sendMessage(ChatColor.AQUA + "/bot follow <name>" + ChatColor.GRAY + " - Bot follows you");
        player.sendMessage(ChatColor.AQUA + "/bot stay <name>" + ChatColor.GRAY + " - Bot stays in place");
        player.sendMessage(ChatColor.AQUA + "/bot mine <name>" + ChatColor.GRAY + " - Bot mines nearby blocks");
        player.sendMessage(ChatColor.AQUA + "/bot guard <name>" + ChatColor.GRAY + " - Bot guards the area");
        player.sendMessage(ChatColor.AQUA + "/bot patrol <name>" + ChatColor.GRAY + " - Bot patrols in a circle");
        player.sendMessage(ChatColor.AQUA + "/bot farm <name>" + ChatColor.GRAY + " - Bot farms nearby crops");
        player.sendMessage(ChatColor.AQUA + "/bot list" + ChatColor.GRAY + " - List your bots");
        player.sendMessage(ChatColor.AQUA + "/bot tp <name>" + ChatColor.GRAY + " - Teleport bot to you");
        player.sendMessage(ChatColor.AQUA + "/bot info <name>" + ChatColor.GRAY + " - Show bot info");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (!sub.equals("spawn") && !sub.equals("list")) {
                // Suggest bot names owned by the player
                return botManager.getPlayerBots(player).stream()
                        .map(FakePlayer::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
