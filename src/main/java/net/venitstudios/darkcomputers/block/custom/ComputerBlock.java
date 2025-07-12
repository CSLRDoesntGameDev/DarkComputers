package net.venitstudios.darkcomputers.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.venitstudios.darkcomputers.block.entity.custom.ComputerBlockEntity;
import net.venitstudios.darkcomputers.block.entity.ModBlockEntities;
import net.venitstudios.darkcomputers.block.entity.custom.TerminalBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ComputerBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);

    public ComputerBlock(Properties properties) {
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
        return TerminalBlock.box(0, 0, 0, 16, 16, 16);
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
        if(level.getBlockEntity(pos) instanceof ComputerBlockEntity computerBlockEntity) {
            if (!level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(computerBlockEntity, Component.literal("Computer")), pos);

                computerBlockEntity.runBus();
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof ComputerBlockEntity computerBlockEntity) {
            computerBlockEntity.dropItems();
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    // https://docs.neoforged.net/docs/1.21.1/blockentities/
    @SuppressWarnings("unchecked") // Due to generics, an unchecked cast is necessary here.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        if (type == ModBlockEntities.COMPUTER_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<ComputerBlockEntity>) ComputerBlockEntity::tick;
        }
        return null;
    }

}
