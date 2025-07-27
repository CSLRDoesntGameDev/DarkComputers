package net.venitstudios.darkcomputers.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.venitstudios.darkcomputers.block.entity.ModBlockEntities;
import net.venitstudios.darkcomputers.computing.S88.BusS88;
import net.venitstudios.darkcomputers.network.ModPayloads;
import net.venitstudios.darkcomputers.screen.custom.display.ComputerMenu;
import org.jetbrains.annotations.Nullable;

public class ComputerBlockEntity extends BlockEntity implements MenuProvider {
    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BE.get(), pos, blockState);
        this.bus = new BusS88(this);
    }
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            storageStack = inventory.getStackInSlot(slot);
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    public ItemStack storageStack = ItemStack.EMPTY;
    public BusS88 bus;

    private static final BlockPos[] POS_OFFSETS = new BlockPos[] {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    };

    // The signature of this method matches the signature of the BlockEntityTicker functional interface.
    public static void tick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (level.getGameTime() % 2 == 0) {
                if (level.getBlockEntity(pos) instanceof ComputerBlockEntity computerBlockEntity) {
                    float detectionRange = 7f;
                    byte[] charBuf = computerBlockEntity.bus.ppu.charBuf;

                    byte[] romBuf = computerBlockEntity.bus.ppu.charRom;
                        PacketDistributor.sendToPlayersNear(
                                (ServerLevel) level, null,
                                pos.getX(), pos.getY(), pos.getZ(), detectionRange,
                                new ModPayloads.cmpUpdate(pos, charBuf, romBuf)
                        );

                }
            }

            if (level.getGameTime() % 10 == 0) {
                scanNeighborsForDevices(level, pos, state, blockEntity);
            }


            if (!blockEntity.bus.processor.halted) {
                for (int i = 0; i < blockEntity.bus.cyclesPerTick; i++) {
                    blockEntity.bus.processor.step();
                }
            }
        }

    }

    public static void scanNeighborsForDevices(Level level, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ComputerBlockEntity computerBlockEntity) {

                for (int i = 0; i < 6; i++) {
                    BlockPos checkPos = pos.offset(POS_OFFSETS[i]);
                    BlockEntity entity = level.getBlockEntity(checkPos);

                    if (entity instanceof InterfaceBlockEntity interfaceBlockEntity) {
                        interfaceBlockEntity.initializeWithComputer(computerBlockEntity);
                    }

                }
            }
        }
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void runBus() {

    }

    public void dropItems() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Computer");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        ComputerMenu menu = new ComputerMenu(i, inventory, this);
        return menu;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }
}
