package net.venitstudios.darkcomputers.item.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.component.ModDataComponents;

import java.io.File;
import java.util.UUID;

public class EproomItem extends Item {
    public EproomItem(Properties properties) { super(properties); }

    public static String getStorageUUID(ItemStack stack) { return stack.get(ModDataComponents.ITEM_UUID); }
    public static String getStoragePath(ItemStack stack) { return DarkComputers.modDataStoragePath + "/eeprom/" + getStorageUUID(stack) + "/"; }
    public static String newUUID() { return UUID.randomUUID().toString(); }
    public static boolean checkUUID(ItemStack stack) { return stack.has(ModDataComponents.ITEM_UUID); }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!checkUUID(stack)) stack.set(ModDataComponents.ITEM_UUID, newUUID());

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
