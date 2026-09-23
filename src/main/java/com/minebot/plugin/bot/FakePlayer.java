package com.minebot.plugin.bot;

import com.minebot.plugin.MineBot;
import com.minebot.plugin.bot.task.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Represents a single fake-player bot in the world.
 * The bot is backed by a Villager entity with custom AI behaviour.
 */
public class FakePlayer {

    private final String name;
    private final UUID id;
    private final MineBot plugin;
    private Villager entity;
    private Player owner;
    private BotTaskType currentTaskType;
    private BotTask currentTask;
    private BukkitTask tickTask;
    private Location homeLocation;

    public FakePlayer(String name, Player owner, MineBot plugin) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.plugin = plugin;
        this.currentTaskType = BotTaskType.IDLE;
    }

    /**
     * Spawns the bot entity at the given location.
     */
    public void spawn(Location location) {
        this.homeLocation = location.clone();
        this.entity = location.getWorld().spawn(location, Villager.class, villager -> {
            villager.setCustomName("§b[Bot] §f" + name);
            villager.setCustomNameVisible(true);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setCollidable(false);
            villager.setProfession(Villager.Profession.TOOLSMITH);
            villager.setVillagerType(Villager.Type.PLAINS);
            villager.setMetadata("minebot", new FixedMetadataValue(plugin, name));
            villager.setMetadata("minebot_owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        });

        // Start the tick loop for this bot
        startTickLoop();
    }

    /**
     * Removes the bot from the world.
     */
    public void remove() {
        stopTask();
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    /**
     * Assigns a new task to this bot.
     */
    public void setTask(BotTaskType taskType) {
        stopTask();
        this.currentTaskType = taskType;

        switch (taskType) {
            case FOLLOW -> currentTask = new FollowTask(this);
            case MINE -> currentTask = new MineTask(this);
            case GUARD -> currentTask = new GuardTask(this);
            case PATROL -> currentTask = new PatrolTask(this);
            case FARM -> currentTask = new FarmTask(this);
            case IDLE -> currentTask = null;
        }

        if (currentTask != null) {
            currentTask.start();
        }
    }

    /**
     * Stops the current task.
     */
    public void stopTask() {
        if (currentTask != null) {
            currentTask.stop();
            currentTask = null;
        }
        currentTaskType = BotTaskType.IDLE;
    }

    private void startTickLoop() {
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (entity == null || entity.isDead()) {
                return;
            }
            if (currentTask != null) {
                currentTask.tick();
            }
        }, 0L, 5L); // Tick every 5 server ticks (0.25s)
    }

    /**
     * Smoothly moves the bot toward a target location.
     */
    public void moveTo(Location target) {
        if (entity == null || entity.isDead()) return;

        Location current = entity.getLocation();
        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        double dz = target.getZ() - current.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.5) return;

        double speed = Math.min(0.4, dist);
        double mx = (dx / dist) * speed;
        double mz = (dz / dist) * speed;

        // Calculate yaw to face movement direction
        float yaw = (float) Math.toDegrees(Math.atan2(-mx, mz));

        Location newLoc = current.clone().add(mx, 0, mz);
        newLoc.setYaw(yaw);
        newLoc.setPitch(current.getPitch());

        // Handle height differences
        if (dy > 0.5) {
            newLoc.add(0, 1, 0);
        } else if (dy < -0.5) {
            newLoc.add(0, -1, 0);
        }

        entity.teleport(newLoc);
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public Villager getEntity() {
        return entity;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public BotTaskType getCurrentTaskType() {
        return currentTaskType;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public MineBot getPlugin() {
        return plugin;
    }

    public boolean isAlive() {
        return entity != null && !entity.isDead();
    }
}
