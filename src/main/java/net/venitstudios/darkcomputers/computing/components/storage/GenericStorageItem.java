package net.venitstudios.darkcomputers.computing.components.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.component.ModDataComponents;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GenericStorageItem extends Item {
    public GenericStorageItem(Properties properties) { super(properties); }

    public static String getStorageUUID(ItemStack stack) { return stack.get(ModDataComponents.ITEM_UUID); }
    public static String getStoragePath(ItemStack stack) { return DarkComputers.modDataStoragePath + "/" + getStorageUUID(stack) + "/"; }
    public static String newUUID() { return UUID.randomUUID().toString(); }
    public static boolean checkUUID(ItemStack stack) { return stack.has(ModDataComponents.ITEM_UUID); }
    public static boolean checkDirectory(ItemStack stack) {
        if (stack.has(ModDataComponents.ITEM_UUID))
            return new File(getStoragePath(stack)).isDirectory();
        return false;
    }
    public static void ensurePath(ItemStack stack) {
        if (!checkDirectory(stack)) {
            boolean created = new File(getStoragePath(stack)).mkdirs();
//            DarkComputers.LOGGER.info("made file? " + created);
        }
    }
    public static File[] getFilesAt(ItemStack stack) {
        File[] fs = new File(getStoragePath(stack)).listFiles();
        if (fs != null) return fs;
        return new File[0];
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!checkUUID(stack)) stack.set(ModDataComponents.ITEM_UUID, newUUID());

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (checkUUID(stack)) tooltipComponents.add(Component.literal(getStorageUUID(stack)).withColor(0xFFababab));
        ensurePath(stack);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static void writeData(String fileName, ItemStack stack, int startAddress, byte[] data) {
        Path filePath = Path.of(getStoragePath(stack) + fileName);
        File fi = new File(filePath.toUri());

        ensurePath(stack);

        if (!fi.exists()) {
                try {
                    boolean fileCreated = fi.createNewFile();
                    if (!fileCreated) DarkComputers.LOGGER.info("Failed to create file: " + filePath.toString());

                } catch (IOException e) {
                    DarkComputers.LOGGER.info("IOException Thrown during DarkComputers.GenericStorageItem.writeData (B): {}", e.toString());
                }
        }

        if (fi.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
                raf.seek(startAddress);
                raf.write(data);
                raf.close();
            } catch (IOException e) {
                DarkComputers.LOGGER.info("IOException Thrown during DarkComputers.GenericStorageItem.writeData (A): {}", e.toString());
            }
        }
    }
    public static byte[] readData(String fileName, ItemStack stack, int startAddress, int byteCount) {
        Path filePath = Path.of(getStoragePath(stack) + fileName);
        if (new File(filePath.toUri()).exists()) {

            try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
                try {
                    raf.seek(startAddress);
                } catch (IOException ex) {
                    DarkComputers.LOGGER.info("IOException Thrown during DarkComputers.GenericStorageItem.readData (A): " + ex.toString());
                }
                byte[] buffer = new byte[byteCount];
                int bytesRead = raf.read(buffer);
                if (bytesRead < byteCount) {
                    return Arrays.copyOf(buffer, Math.max(bytesRead, 0));
                }
                return buffer;
            } catch (IOException e) {
                DarkComputers.LOGGER.info("IOException Thrown during DarkComputers.GenericStorageItem.readData (B): " + e.toString());
            }
            return new byte[0];
        }
        return new byte[0];
    }


}

