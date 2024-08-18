package eu.midnightdust.puddles;

import eu.midnightdust.puddles.block.PuddleBlock;
import eu.midnightdust.puddles.config.PuddlesConfig;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class Puddles implements ModInitializer {
    public static final String MOD_ID = "puddles";
    public static final PuddleBlock Puddle = new PuddleBlock();
    private final static EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(id("puddle_speed"), 100, EntityAttributeModifier.Operation.ADD_VALUE);

    @Override
    public void onInitialize() {
        PuddlesConfig.init(MOD_ID, PuddlesConfig.class);
        Registry.register(Registries.BLOCK, id("puddle"), Puddle);
        Registry.register(Registries.ITEM, id("puddle"), new PolymerBlockItem(Puddle, new Item.Settings(), Items.POTION));
        ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(player -> {
            EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY);
            if (player.getBlockStateAtPos().getBlock() instanceof PuddleBlock) {
                if (entityAttributeInstance != null) {
                    entityAttributeInstance.removeModifier(entityAttributeModifier);
                    entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
                }
                if (world.random.nextInt(30) == 0) player.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_SWIM, SoundCategory.BLOCKS, 0.5f, 1.2f);
                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.SPLASH, false, player.getBlockX() + 0.5f, player.getBlockY() + 0.1f, player.getBlockZ() + 0.5f, 0.5f, 0.1f, 0.5f, 1, 2));
            } else if (player.isInFluid()) {
                if (entityAttributeInstance != null) entityAttributeInstance.removeModifier(entityAttributeModifier);
            }
        }));
    }
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
