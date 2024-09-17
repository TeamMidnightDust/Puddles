package eu.midnightdust.puddles.block;

import eu.midnightdust.puddles.config.PuddlesConfig;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PuddleBlock extends Block implements PolymerBlock, BlockWithElementHolder {
    public PuddleBlock() {
        super(AbstractBlock.Settings.copy(Blocks.GRASS));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isEmpty()) {
            return ActionResult.PASS;
        } else {
            if (itemStack.getItem() == Items.GLASS_BOTTLE) {
                if (!world.isClient) {
                    if (!player.isCreative()) {
                        ItemStack waterBottleStack = new ItemStack(Items.POTION);
                        PotionUtil.setPotion(waterBottleStack, Potions.WATER);
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            player.setStackInHand(hand, waterBottleStack);
                        } else if (!player.getInventory().insertStack(waterBottleStack)) {
                            player.dropItem(waterBottleStack, false);
                        } else if (player instanceof ServerPlayerEntity) {
                            player.currentScreenHandler.sendContentUpdates();
                        }
                    }

                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
                return ActionResult.success(world.isClient);
            }
            else return ActionResult.FAIL;
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isRaining() && random.nextInt(1000 / PuddlesConfig.evaporationChance) == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        this.scheduledTick(state, world, pos, random);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
            int i;
            // Check if there are puddles on the sides of the block above
            for (i = 2; i < 6; ++i) {
                BlockPos pos1 = pos.up();
                if (world.getBlockState(pos1.offset(Direction.byId(i))).getBlock() instanceof PuddleBlock) {
                    // When sides of the block above have a puddle, don't place the puddle
                    return false;
                }
            }
            // Check if there are puddles on the sides of the block below
            for (i = 2; i < 6; ++i) {
                BlockPos pos1 = pos.down();
                if (world.getBlockState(pos1.offset(Direction.byId(i))).getBlock() instanceof PuddleBlock) {
                    // When sides of the block below have a puddle, don't place the puddle
                    return false;
                }
            }
        }
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.WATER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 7);
    }

    public ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        ElementHolder holder = new ElementHolder();
        var interactionElement = new InteractionElement(new VirtualElement.InteractionHandler() {
            @Override
            public void interact(ServerPlayerEntity player, Hand hand) {
                world.getBlockState(pos).onUse(world, player, hand, new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false));
            }
            @Override
            public void attack(ServerPlayerEntity player) {
                player.playSound(SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.BLOCKS, 0.8f, 1.3f);
                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.SPLASH, false, pos.getX() + 0.5f, pos.getY() + 0.1f, pos.getZ() + 0.5f, 0.5f, 0.1f, 0.5f, 1, 2));
            }
        });
        interactionElement.setSize(1f, 0.06241f);
        interactionElement.setOffset(new Vec3d(0d, -0.5d, 0d));
        holder.addElement(interactionElement);
        return holder;
    }
}

