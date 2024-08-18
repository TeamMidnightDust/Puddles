package eu.midnightdust.puddles.mixin;

import eu.midnightdust.puddles.Puddles;
import eu.midnightdust.puddles.config.PuddlesConfig;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(at = @At("TAIL"),method = "tickChunk")
    public void puddles$tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ChunkPos chunkPos = chunk.getPos();
        boolean bl = this.isRaining();
        int x = chunkPos.getStartX();
        int z = chunkPos.getStartZ();
        Profiler profiler = this.getProfiler();
        BlockPos pos;

        if (PuddlesConfig.puddleSpawnRate != 0) {
            profiler.push("puddles");
            if (bl && random.nextInt(100000 / PuddlesConfig.puddleSpawnRate) == 0) {
                pos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getRandomPosInChunk(x, 0, z, 15));
                if (this.hasRain(pos) && getBlockState(pos.down()).isSideSolidFullSquare(this, pos, Direction.UP) &&
                        Puddles.Puddle.canPlaceAt(null,this,pos)) {
                    setBlockState(pos, Puddles.Puddle.getDefaultState());
                }
            }
            profiler.pop();
        }
    }
}