package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bot mines nearby ore and stone blocks, dropping items at its feet.
 */
public class MineTask extends BotTask {

    private static final double MINE_RADIUS = 5.0;
    private static final Set<Material> MINEABLE = Set.of(
            Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE,
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GRAVEL, Material.DIRT, Material.SAND
    );

    private int tickCounter = 0;

    public MineTask(FakePlayer bot) {
        super(bot);
    }

    @Override
    public void tick() {
        if (!active) return;

        tickCounter++;
        // Mine one block every ~1 second (4 ticks of 5-tick intervals)
        if (tickCounter % 4 != 0) return;

        Location center = bot.getEntity().getLocation();
        Block target = findMineableBlock(center);

        if (target != null) {
            // Move toward the block first
            bot.moveTo(target.getLocation().add(0.5, 0, 0.5));

            // Break the block and drop items
            for (ItemStack drop : target.getDrops()) {
                center.getWorld().dropItemNaturally(center, drop);
            }
            target.setType(Material.AIR);

            // Visual/sound feedback
            center.getWorld().playSound(center, org.bukkit.Sound.BLOCK_STONE_BREAK, 0.5f, 1.0f);
        }
    }

    private Block findMineableBlock(Location center) {
        List<Block> candidates = new ArrayList<>();
        int radius = (int) MINE_RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);
                    if (MINEABLE.contains(block.getType())) {
                        candidates.add(block);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick the closest block
        candidates.sort((a, b) -> {
            double distA = a.getLocation().distanceSquared(center);
            double distB = b.getLocation().distanceSquared(center);
            return Double.compare(distA, distB);
        });

        return candidates.get(0);
    }
}
