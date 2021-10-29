package eu.midnightdust.puddles;

import eu.midnightdust.puddles.block.PuddleBlock;
import eu.midnightdust.puddles.config.PuddlesConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;

public class Puddles implements ModInitializer {
    public static final String MOD_ID = "puddles";
    public static final Block Puddle = new PuddleBlock(Fluids.WATER, FabricBlockSettings.of(Material.WATER));

    public void onInitialize() {
        PuddlesConfig.init(MOD_ID, PuddlesConfig.class);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID,"puddle"), Puddle);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID,"puddle"), new BlockItem(Puddle, new Item.Settings()));
    }
}
