package net.venitstudios.darkcomputers.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FilteredSlot extends SlotItemHandler {
    public final Item allowedItem;
    public final int maxCount;
    public FilteredSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item allowedItem, int maxCount) {
        super(itemHandler, index, xPosition, yPosition);
        this.allowedItem = allowedItem;
        this.maxCount = maxCount;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return (stack.getItem().equals(allowedItem));
    }

    @Override
    public int getMaxStackSize() {
        return maxCount;
    }
}
