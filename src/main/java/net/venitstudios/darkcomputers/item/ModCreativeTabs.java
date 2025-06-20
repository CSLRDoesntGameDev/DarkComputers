package net.venitstudios.darkcomputers.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DarkComputers.MOD_ID);

    public static final Supplier<CreativeModeTab> DARKCOMPUTERS_TAB = CREATIVE_MODE_TAB.register("darkcomputers_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CPU_BASE.get()))
                    .title(Component.translatable("creative.darkcomputers.main_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        // items
                        output.accept(ModItems.CPU_BASE);
                        output.accept(ModItems.FLOPPY_DISK);
                        output.accept(ModItems.EEPROM);

                        // blocks

                        output.accept(ModBlocks.TERMINAL_BLOCK);
                        output.accept(ModBlocks.COMPUTER_BLOCK);
                        output.accept(ModBlocks.PROGRAMMER_BLOCK);
                    })

                    .build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
