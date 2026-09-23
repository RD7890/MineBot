package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Bot follows its owner at a configurable distance.
 */
public class FollowTask extends BotTask {

    private static final double FOLLOW_DISTANCE = 3.0;
    private static final double TELEPORT_DISTANCE = 30.0;

    public FollowTask(FakePlayer bot) {
        super(bot);
    }

    @Override
    public void tick() {
        if (!active) return;

        Player owner = bot.getOwner();
        if (owner == null || !owner.isOnline()) {
            return;
        }

        Location botLoc = bot.getEntity().getLocation();
        Location ownerLoc = owner.getLocation();

        // Must be in the same world
        if (!botLoc.getWorld().equals(ownerLoc.getWorld())) {
            bot.getEntity().teleport(ownerLoc);
            return;
        }

        double distance = botLoc.distance(ownerLoc);

        // Teleport if too far away
        if (distance > TELEPORT_DISTANCE) {
            Location tpLoc = ownerLoc.clone().add(
                    ownerLoc.getDirection().multiply(-2)
            );
            bot.getEntity().teleport(tpLoc);
            return;
        }

        // Move toward owner if outside follow distance
        if (distance > FOLLOW_DISTANCE) {
            bot.moveTo(ownerLoc);
        }
    }
}
