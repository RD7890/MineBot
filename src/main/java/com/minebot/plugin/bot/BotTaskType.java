package com.minebot.plugin.bot;

/**
 * Enum of all task types a bot can perform.
 */
public enum BotTaskType {
    IDLE("Idle", "Standing still"),
    FOLLOW("Follow", "Following owner"),
    MINE("Mine", "Mining nearby blocks"),
    GUARD("Guard", "Guarding home location"),
    PATROL("Patrol", "Patrolling around home"),
    FARM("Farm", "Farming nearby crops");

    private final String displayName;
    private final String description;

    BotTaskType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
