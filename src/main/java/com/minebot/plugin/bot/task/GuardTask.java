package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Bot guards its home location, attacking hostile mobs that come near.
 */
public class GuardTask extends BotTask {

    private static final double GUARD_RADIUS = 10.0;
    private static final double ATTACK_RANGE = 2.5;
    private static final double ATTACK_DAMAGE = 6.0;

    private int tickCounter = 0;

    public GuardTask(FakePlayer bot) {
        super(bot);
    }

    @Override
    public void start() {
        super.start();
        bot.setHomeLocation(bot.getEntity().getLocation().clone());
    }

    @Override
    public void tick() {
        if (!active) return;

        tickCounter++;

        Location home = bot.getHomeLocation();
        Location botLoc = bot.getEntity().getLocation();

        // Find nearby hostile mobs
        Monster target = findNearestHostile(home);

        if (target != null) {
            double dist = botLoc.distance(target.getLocation());

            if (dist <= ATTACK_RANGE) {
                // Attack the mob every ~1 second
                if (tickCounter % 4 == 0) {
                    target.damage(ATTACK_DAMAGE);

                    // Face the target
                    Location look = target.getLocation();
                    bot.getEntity().teleport(botLoc.setDirection(
                            look.toVector().subtract(botLoc.toVector()).normalize()
                    ));
                }
            } else {
                // Move toward the hostile mob
                bot.moveTo(target.getLocation());
            }
        } else {
            // Return to home if no threats
            if (botLoc.distance(home) > 2.0) {
                bot.moveTo(home);
            }
        }
    }

    private Monster findNearestHostile(Location center) {
        List<Entity> nearby = center.getWorld().getEntities().stream()
                .filter(e -> e instanceof Monster)
                .filter(e -> e.getLocation().distance(center) <= GUARD_RADIUS)
                .toList();

        Monster nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity e : nearby) {
            double dist = e.getLocation().distance(center);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = (Monster) e;
            }
        }

        return nearest;
    }
}
