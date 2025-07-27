package net.venitstudios.darkcomputers.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.item.custom.EepromProgrammer;
import net.venitstudios.darkcomputers.item.custom.EproomItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DarkComputers.MOD_ID);

    public static final DeferredItem<Item> FLOPPY_DISK = ITEMS.register("floppy_a",
            () -> new GenericStorageItem(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<Item> EEPROM = ITEMS.register("eeprom_a",
            () -> new EproomItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<Item> EEPROM_PROGRAMMER = ITEMS.register("eeprom_programmer",
            () -> new EepromProgrammer(new Item.Properties().stacksTo(64))
    );



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
