package net.venitstudios.darkcomputers.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.venitstudios.darkcomputers.block.entity.ModBlockEntities;
import net.venitstudios.darkcomputers.block.entity.custom.InterfaceBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class InterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<InterfaceBlock> CODEC = simpleCodec(InterfaceBlock::new);

    public InterfaceBlock(Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 16, 16);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InterfaceBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        use(state, level, pos, player);
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        use(state, level, pos, player);
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }


    private void use(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity interfaceBlock) {
            if (interfaceBlock.busDevice != null) {
                player.sendSystemMessage(Component.literal(Arrays.toString(interfaceBlock.busDevice.lanes)));
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {

        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity interfaceBlock) {
            if (interfaceBlock.busDevice != null) {
                interfaceBlock.busDevice.bus.removeDevice(interfaceBlock.busDevice);
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {

        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity interfaceBlock && !level.isClientSide) {

            BlockState neighborState = level.getBlockState(neighborPos);
            neighborBlock = neighborState.getBlock();
            if (neighborBlock.equals(Blocks.REDSTONE_WIRE)) {

                int dx = neighborPos.getX() - pos.getX();
                int dy = neighborPos.getY() - pos.getY();
                int dz = neighborPos.getZ() - pos.getZ();

                Direction direction = Direction.fromDelta(dx, dy, dz);

                int power = neighborState.getValue(BlockStateProperties.POWER);

                interfaceBlock.setBusStrength(direction, power);

            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        direction = direction.getOpposite();
        if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity interfaceBlock) {

            if (!Objects.requireNonNull(interfaceBlock.getLevel()).isClientSide) {

                return interfaceBlock.getSignalFromBus(direction);
            }
        }
        return 0;
    }


    // https://docs.neoforged.net/docs/1.21.1/blockentities/
    @SuppressWarnings("unchecked") // Due to generics, an unchecked cast is necessary here.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        if (type == ModBlockEntities.INTERFACE_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<InterfaceBlockEntity>) InterfaceBlockEntity::tick;
        }
        return null;
    }


}