package com.minebot.plugin.bot.task;

import com.minebot.plugin.bot.FakePlayer;

/**
 * Base class for all bot tasks.
 */
public abstract class BotTask {

    protected final FakePlayer bot;
    protected boolean active;

    public BotTask(FakePlayer bot) {
        this.bot = bot;
        this.active = false;
    }

    /**
     * Called when the task starts.
     */
    public void start() {
        this.active = true;
    }

    /**
     * Called every bot tick while the task is active.
     */
    public abstract void tick();

    /**
     * Called when the task stops.
     */
    public void stop() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }
}
