package net.venitstudios.darkcomputers.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FilteredSlot extends Slot {
    public final Item allowedItem;
    public final int maxCount;
    public Container localContainer;
    public FilteredSlot(Container container, int index, int xPosition, int yPosition, Item allowedItem, int maxCount) {
        super(container, index, xPosition, yPosition);
        this.allowedItem = allowedItem;
        this.maxCount = maxCount;
        this.localContainer = container;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return (stack.getItem().equals(allowedItem));
    }

    @Override
    public int getMaxStackSize() {
        return maxCount;
    }

    @Override
    public void set(ItemStack stack) {
        this.localContainer.setItem(getSlotIndex(), stack);
        super.set(stack);
    }


}
