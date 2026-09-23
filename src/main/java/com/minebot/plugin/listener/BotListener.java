package com.minebot.plugin.listener;

import com.minebot.plugin.MineBot;
import com.minebot.plugin.bot.BotManager;
import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Handles events related to bot entities.
 */
public class BotListener implements Listener {

    private final MineBot plugin;
    private final BotManager botManager;

    public BotListener(MineBot plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    /**
     * Prevent bots from being damaged by players (they're invulnerable, but just in case).
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isBot(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent mobs from targeting bot entities.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && isBot(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    /**
     * Right-click a bot to view its info.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (isBot(clicked)) {
            event.setCancelled(true);
            String botName = clicked.getMetadata("minebot").get(0).asString();
            event.getPlayer().performCommand("bot info " + botName);
        }
    }

    /**
     * When a player leaves, their bots stay but become idle.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (FakePlayer bot : botManager.getPlayerBots(player)) {
            bot.stopTask();
        }
    }

    /**
     * Prevent bot entities from being unloaded with chunks.
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (isBot(entity)) {
                // Bots stay loaded - teleport to spawn if chunk unloads
                entity.teleport(entity.getWorld().getSpawnLocation());
            }
        }
    }

    /**
     * Check if an entity is a MineBot bot.
     */
    private boolean isBot(Entity entity) {
        return entity instanceof Villager && entity.hasMetadata("minebot");
    }
}
