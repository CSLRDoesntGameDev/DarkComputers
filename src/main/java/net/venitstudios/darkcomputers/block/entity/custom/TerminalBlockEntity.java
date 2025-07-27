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
import net.venitstudios.darkcomputers.component.ModDataComponents;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.computing.components.terminal.TextEditor;
import net.venitstudios.darkcomputers.item.ModItems;
import net.venitstudios.darkcomputers.network.ModPayloads;
import net.venitstudios.darkcomputers.screen.custom.terminal.TerminalMenu;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TerminalBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    public ItemStack storageStack = ItemStack.EMPTY;
    public TextEditor editor;
    public boolean editingFile = false;

    public static void tick(Level level, BlockPos pos, BlockState state, TerminalBlockEntity blockEntity) {

        if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminalBlockEntity && !level.isClientSide()) {

            terminalBlockEntity.storageStack = terminalBlockEntity.inventory.getStackInSlot(0);

            if (terminalBlockEntity.storageStack.equals(ItemStack.EMPTY) || terminalBlockEntity.storageStack.getCount() == 0) {
                terminalBlockEntity.editor.resetEditor();
            }

            float detectionRange = 7f;
            File[] files = GenericStorageItem.getFilesAt(terminalBlockEntity.storageStack);
            String[] strings = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                strings[i] = files[i].getName();
            }
            Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);

            String fileName = terminalBlockEntity.editor.currentFile != null ? terminalBlockEntity.editor.currentFile.getName() : "";


            if (terminalBlockEntity.storageStack.getCount() > 0) {
                ModPayloads.fileStatusUpdate fileStatusUpdate = new ModPayloads.fileStatusUpdate(pos,
                        Arrays.toString(strings).replaceAll("[\\[\\]]", " "),
                        fileName,
                        terminalBlockEntity.editingFile,
                        terminalBlockEntity.editor.renamingFile,
                        terminalBlockEntity.storageStack
                );
                PacketDistributor.sendToPlayersNear((ServerLevel) level, null, pos.getX(), pos.getY(), pos.getZ(), detectionRange, fileStatusUpdate);
            } else {
                ModPayloads.fileStatusUpdate fileStatusUpdate = new ModPayloads.fileStatusUpdate(pos,
                        "",
                        "",
                        false,
                        false,
                        ModItems.FLOPPY_DISK.toStack(1)
                );

                PacketDistributor.sendToPlayersNear((ServerLevel) level, null, pos.getX(), pos.getY(), pos.getZ(), detectionRange, fileStatusUpdate);

            }


        TextEditor editor = terminalBlockEntity.editor;

          if (editor != null) {
              StringBuilder builder = new StringBuilder();

              for (String line : terminalBlockEntity.editor.fileContents) {
                  builder.append(line);
                  builder.append("\n");
              }

              byte[] data = builder.toString().getBytes(StandardCharsets.UTF_8);
              ModPayloads.terminalFileContentUpdate terminalFileContentUpdate = null;

              if (data.length < 10_000_000) {
                  terminalFileContentUpdate = new ModPayloads.terminalFileContentUpdate(pos,
                          data,
                          editor.newFileName
                          );
              } else {
                  terminalFileContentUpdate = new ModPayloads.terminalFileContentUpdate(pos,
                          ("File Too Large!\nyou will need to manually\nmake the file smaller\nif you're on a server contact the host\nStorage Disk UUID:\n" +
                                  terminalBlockEntity.storageStack.get(ModDataComponents.ITEM_UUID)).getBytes(StandardCharsets.UTF_8),
                          editor.newFileName);
              }

              PacketDistributor.sendToPlayersNear((ServerLevel) level, null, pos.getX(), pos.getY(), pos.getZ(), detectionRange, terminalFileContentUpdate);
          }



            ModPayloads.terminalCursorUpdate terminalCursorUpdate = new ModPayloads.terminalCursorUpdate(pos,
                    editor.curCol, editor.curRow, editor.curRowOffset, editor.curColOffset);
            PacketDistributor.sendToPlayersNear((ServerLevel) level, null, pos.getX(), pos.getY(), pos.getZ(), detectionRange, terminalCursorUpdate);

        }
    }

    public TerminalBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TERMINAL_BE.get(), pos, blockState);
        this.editor = new TextEditor(this);
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
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
        return Component.literal("Terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        TerminalMenu menu = new TerminalMenu(i, inventory, this);
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
