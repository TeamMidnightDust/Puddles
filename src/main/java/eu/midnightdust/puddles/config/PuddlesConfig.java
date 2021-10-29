package eu.midnightdust.puddles.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class PuddlesConfig extends MidnightConfig {
    @Entry // Enable or disable hats for contributors, friends and donors.
    public static int puddleSpawnRate = 1;
    @Entry
    public static int snowStackChance = 1;
    @Entry
    public static int evaporationChance = 5;
}
