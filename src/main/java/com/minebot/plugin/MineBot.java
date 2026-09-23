package com.minebot.plugin;

import com.minebot.plugin.bot.BotManager;
import com.minebot.plugin.command.BotCommand;
import com.minebot.plugin.listener.BotListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MineBot - A programmable fake-player bot plugin for Paper.
 * Spawns controllable NPC bots that can follow, mine, guard, patrol, and farm.
 */
public final class MineBot extends JavaPlugin {

    private static MineBot instance;
    private BotManager botManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        botManager = new BotManager(this);

        BotCommand botCommand = new BotCommand(this, botManager);
        getCommand("bot").setExecutor(botCommand);
        getCommand("bot").setTabCompleter(botCommand);

        getServer().getPluginManager().registerEvents(new BotListener(this, botManager), this);

        getLogger().info("MineBot enabled! Use /bot to manage your bots.");
    }

    @Override
    public void onDisable() {
        if (botManager != null) {
            botManager.removeAllBots();
        }
        getLogger().info("MineBot disabled. All bots removed.");
    }

    public static MineBot getInstance() {
        return instance;
    }

    public BotManager getBotManager() {
        return botManager;
    }
}
