package eu.midnightdust.puddles.mixin;

import eu.midnightdust.puddles.Puddles;
import eu.midnightdust.puddles.config.PuddlesConfig;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@SuppressWarnings("deprecation")
@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed);
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
            if (bl && random.nextInt(10000 / PuddlesConfig.puddleSpawnRate) == 0) {
                pos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getRandomPosInChunk(x, 0, z, 15));
                if (this.hasRain(pos) && getBlockState(pos.down()).isSideSolidFullSquare(this, pos, Direction.UP) && Puddles.Puddle.canPlaceAt(null,this,pos)) {
                    setBlockState(pos, Puddles.Puddle.getDefaultState());
                }
            }
            profiler.pop();
        }

        if (PuddlesConfig.snowStackChance != 0) {
            profiler.push("extra_snow");
            if (bl && random.nextInt(10000 / PuddlesConfig.snowStackChance) == 0) {
                pos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getRandomPosInChunk(x, 0, z, 15));
                if (this.getBlockState(pos).getBlock() == Blocks.SNOW && getBlockState(pos.down()).isSideSolidFullSquare(this, pos, Direction.UP)) {
                    int layer = getBlockState(pos).get(Properties.LAYERS);
                    if (layer < 5) {
                        setBlockState(pos, Blocks.SNOW.getDefaultState().with(Properties.LAYERS, layer + 1));
                    }
                }
            }
            profiler.pop();
        }
    }
}