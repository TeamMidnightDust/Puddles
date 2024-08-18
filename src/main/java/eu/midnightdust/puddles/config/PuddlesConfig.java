package eu.midnightdust.puddles.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class PuddlesConfig extends MidnightConfig {
    @Entry(isSlider = true, min = 0, max = 1000)
    public static int puddleSpawnRate = 10;
    @Entry(isSlider = true, min = 0, max = 1000)
    public static int evaporationChance = 100;
}