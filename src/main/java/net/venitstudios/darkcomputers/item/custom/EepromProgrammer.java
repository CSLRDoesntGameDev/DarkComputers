package net.venitstudios.darkcomputers.item.custom;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.component.ModDataComponents;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.container.custom.ProgrammerContainer;
import net.venitstudios.darkcomputers.item.ModItems;
import net.venitstudios.darkcomputers.screen.custom.programmer.ProgrammerMenu;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class EepromProgrammer extends Item implements MenuProvider {

    public EepromProgrammer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(this, Component.literal("Programmer")));
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.getGameTime() % 4 == 0) {

            if (!level.isClientSide) {
                String finalErrorString = "";
                if (entity instanceof ServerPlayer player && !stack.isEmpty()) {
                    ItemStack inventoryStack = ItemStack.EMPTY;

                    if (player.getMainHandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
                        inventoryStack = player.getMainHandItem();
                    } else if (player.getOffhandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
                        inventoryStack = player.getOffhandItem();
                    }

                    if (inventoryStack.getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {

                        ProgrammerContainer container = new ProgrammerContainer(inventoryStack);

                        ItemStack storageItemA = container.getItem(0);
                        ItemStack storageItemB = container.getItem(1);

                        if (storageItemA.getItem().equals(ModItems.FLOPPY_DISK.get())) {
                            File[] files = GenericStorageItem.getFilesAt(storageItemA);

                            StringBuilder fileList = new StringBuilder();

                            for (File file : files) {

                                fileList.append(file.getName());

                                if (!file.getName().equals(Arrays.stream(files).toList().getLast().getName())) {
                                    fileList.append(",");
                                }
                            }
                            if (!Objects.equals(stack.get(ModDataComponents.GENERIC_STORAGE_FILES), fileList.toString())) {
                                stack.set(ModDataComponents.GENERIC_STORAGE_FILES, fileList.toString());
                            }

                        } else
                        {
                            finalErrorString += "No Storage";
                        }
                        if (!storageItemB.getItem().equals(ModItems.EEPROM.get())) {
                            finalErrorString += ",No ROM";
                        }

                        if (!finalErrorString.isBlank() && !Objects.equals(stack.get(ModDataComponents.GENERIC_STORAGE_FILES), finalErrorString)) {
                            stack.set(ModDataComponents.GENERIC_STORAGE_FILES, finalErrorString);
                        }

                    }
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Programmer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (player.getMainHandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
            return new ProgrammerMenu(containerId, playerInventory, player.getMainHandItem());
        } else if (player.getOffhandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
            return new ProgrammerMenu(containerId, playerInventory, player.getOffhandItem());
        }
        return null;
    }
}
