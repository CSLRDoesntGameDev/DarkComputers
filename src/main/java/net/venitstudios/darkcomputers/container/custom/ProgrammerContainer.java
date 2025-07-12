package net.venitstudios.darkcomputers.container.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.venitstudios.darkcomputers.DarkComputers;

import java.util.Arrays;


// based on example code from https://docs.neoforged.net/docs/1.21.1/blockentities/container/#containers-on-itemstacks
public class ProgrammerContainer extends SimpleContainer {
    private final ItemStack stack;
    public ProgrammerContainer(ItemStack stack) {
        super(2);
        this.stack = stack;
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.copyInto(this.getItems());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

}
