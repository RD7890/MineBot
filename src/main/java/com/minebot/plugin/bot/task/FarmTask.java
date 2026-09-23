package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bot farms nearby crops - harvests mature crops and replants them.
 */
public class FarmTask extends BotTask {

    private static final double FARM_RADIUS = 6.0;
    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART
    );
    private static final java.util.Map<Material, Material> SEED_MAP = java.util.Map.of(
            Material.WHEAT, Material.WHEAT_SEEDS,
            Material.CARROTS, Material.CARROT,
            Material.POTATOES, Material.POTATO,
            Material.BEETROOTS, Material.BEETROOT_SEEDS,
            Material.NETHER_WART, Material.NETHER_WART
    );

    private int tickCounter = 0;

    public FarmTask(FakePlayer bot) {
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
        // Farm every ~1.5 seconds
        if (tickCounter % 6 != 0) return;

        Location center = bot.getEntity().getLocation();
        Block target = findMatureCrop(center);

        if (target != null) {
            // Move toward the crop
            bot.moveTo(target.getLocation().add(0.5, 0, 0.5));

            Material cropType = target.getType();

            // Harvest - drop items
            for (ItemStack drop : target.getDrops()) {
                center.getWorld().dropItemNaturally(target.getLocation().add(0.5, 0.5, 0.5), drop);
            }

            // Replant
            target.setType(cropType);
            BlockData data = target.getBlockData();
            if (data instanceof Ageable ageable) {
                ageable.setAge(0);
                target.setBlockData(ageable);
            }

            // Sound feedback
            center.getWorld().playSound(center, org.bukkit.Sound.BLOCK_CROP_BREAK, 0.5f, 1.0f);
        }
    }

    private Block findMatureCrop(Location center) {
        List<Block> mature = new ArrayList<>();
        int radius = (int) FARM_RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);
                    if (CROPS.contains(block.getType())) {
                        BlockData data = block.getBlockData();
                        if (data instanceof Ageable ageable) {
                            if (ageable.getAge() >= ageable.getMaximumAge()) {
                                mature.add(block);
                            }
                        }
                    }
                }
            }
        }

        if (mature.isEmpty()) return null;

        // Return closest mature crop
        mature.sort((a, b) -> {
            double distA = a.getLocation().distanceSquared(center);
            double distB = b.getLocation().distanceSquared(center);
            return Double.compare(distA, distB);
        });

        return mature.get(0);
    }
}
