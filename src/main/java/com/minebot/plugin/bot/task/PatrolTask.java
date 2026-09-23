package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.Location;

/**
 * Bot patrols in a circle around its home location.
 */
public class PatrolTask extends BotTask {

    private static final double PATROL_RADIUS = 8.0;
    private static final double PATROL_SPEED = 0.15;

    private double angle = 0;

    public PatrolTask(FakePlayer bot) {
        super(bot);
    }

    @Override
    public void start() {
        super.start();
        bot.setHomeLocation(bot.getEntity().getLocation().clone());
        angle = 0;
    }

    @Override
    public void tick() {
        if (!active) return;

        Location home = bot.getHomeLocation();

        // Calculate patrol point along circle
        double targetX = home.getX() + Math.cos(angle) * PATROL_RADIUS;
        double targetZ = home.getZ() + Math.sin(angle) * PATROL_RADIUS;

        Location patrolPoint = new Location(
                home.getWorld(),
                targetX,
                home.getY(),
                targetZ
        );

        // Move toward the patrol point
        bot.moveTo(patrolPoint);

        // Advance angle
        double botDist = bot.getEntity().getLocation().distance(patrolPoint);
        if (botDist < 1.5) {
            angle += PATROL_SPEED;
            if (angle >= Math.PI * 2) {
                angle -= Math.PI * 2;
            }
        }
    }
}
