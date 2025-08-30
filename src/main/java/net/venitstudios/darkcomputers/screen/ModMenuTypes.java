package net.venitstudios.darkcomputers.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.screen.custom.programmer.ProgrammerMenu;
import net.venitstudios.darkcomputers.screen.custom.terminal.TerminalMenu;
import net.venitstudios.darkcomputers.screen.custom.display.ComputerMenu;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, DarkComputers.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TerminalMenu>> TERMINAL_MENU =
            registerMenuType("terminal_menu", TerminalMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<ProgrammerMenu>> PROGRAMMER_MENU =
            registerMenuType("programmer_menu", ProgrammerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ComputerMenu>> COMPUTER_DISPLAY_MENU =
            registerMenuType("computer_display_menu", ComputerMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
