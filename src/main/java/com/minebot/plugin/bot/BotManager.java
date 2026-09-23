package com.minebot.plugin.bot;

import com.minebot.plugin.MineBot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active bots across the server.
 */
public class BotManager {

    private final MineBot plugin;
    private final Map<String, FakePlayer> bots;
    private static final int MAX_BOTS_PER_PLAYER = 5;
    private static final int MAX_TOTAL_BOTS = 50;

    public BotManager(MineBot plugin) {
        this.plugin = plugin;
        this.bots = new ConcurrentHashMap<>();
    }

    /**
     * Spawns a new bot for the given player.
     */
    public FakePlayer spawnBot(String name, Player owner) {
        if (bots.containsKey(name.toLowerCase())) {
            return null; // Bot with this name already exists
        }

        long playerBotCount = bots.values().stream()
                .filter(b -> b.getOwner().getUniqueId().equals(owner.getUniqueId()))
                .count();

        if (playerBotCount >= MAX_BOTS_PER_PLAYER) {
            return null;
        }

        if (bots.size() >= MAX_TOTAL_BOTS) {
            return null;
        }

        FakePlayer bot = new FakePlayer(name, owner, plugin);
        Location spawnLoc = owner.getLocation().add(
                owner.getLocation().getDirection().multiply(2)
        );
        bot.spawn(spawnLoc);
        bots.put(name.toLowerCase(), bot);
        return bot;
    }

    /**
     * Removes a bot by name.
     */
    public boolean removeBot(String name) {
        FakePlayer bot = bots.remove(name.toLowerCase());
        if (bot != null) {
            bot.remove();
            return true;
        }
        return false;
    }

    /**
     * Gets a bot by name.
     */
    public FakePlayer getBot(String name) {
        return bots.get(name.toLowerCase());
    }

    /**
     * Gets all bots owned by a player.
     */
    public List<FakePlayer> getPlayerBots(Player player) {
        List<FakePlayer> playerBots = new ArrayList<>();
        for (FakePlayer bot : bots.values()) {
            if (bot.getOwner().getUniqueId().equals(player.getUniqueId())) {
                playerBots.add(bot);
            }
        }
        return playerBots;
    }

    /**
     * Gets all bots.
     */
    public Collection<FakePlayer> getAllBots() {
        return Collections.unmodifiableCollection(bots.values());
    }

    /**
     * Gets all bot names.
     */
    public Set<String> getBotNames() {
        return Collections.unmodifiableSet(bots.keySet());
    }

    /**
     * Removes all bots from the server (used on plugin disable).
     */
    public void removeAllBots() {
        for (FakePlayer bot : bots.values()) {
            bot.remove();
        }
        bots.clear();
    }

    /**
     * Checks if a player owns a bot.
     */
    public boolean isOwner(String botName, Player player) {
        FakePlayer bot = bots.get(botName.toLowerCase());
        return bot != null && bot.getOwner().getUniqueId().equals(player.getUniqueId());
    }
}
