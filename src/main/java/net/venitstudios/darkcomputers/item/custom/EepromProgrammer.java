package net.venitstudios.darkcomputers.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.venitstudios.darkcomputers.screen.custom.programmer.ProgrammerMenu;
import org.jetbrains.annotations.Nullable;

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
    public Component getDisplayName() {
        return Component.literal("Programmer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProgrammerMenu(containerId, playerInventory, player.getUseItem().copy());
    }
}
