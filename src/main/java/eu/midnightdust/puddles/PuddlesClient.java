package eu.midnightdust.puddles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.impl.client.rendering.ColorProviderRegistryImpl;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;

import java.util.Objects;

import static eu.midnightdust.puddles.Puddles.*;

public class PuddlesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Colored Puddle Items & Blocks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            int waterColor;
            if (client.world != null && client.player != null) {
                Biome biome = client.world.getBiome(client.player.getBlockPos());
                waterColor = biome.getWaterColor();
            } else waterColor = BuiltinBiomes.PLAINS.getWaterColor();

            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> waterColor, Puddles.Puddle);
        });

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> Objects.requireNonNull(ColorProviderRegistryImpl.BLOCK.get(Blocks.WATER)).getColor(state, view, pos, tintIndex), Puddle);
    }
}
