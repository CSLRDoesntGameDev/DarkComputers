package net.venitstudios.darkcomputers.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.item.custom.EproomItem;
import net.venitstudios.darkcomputers.item.custom.ProcessorItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DarkComputers.MOD_ID);

    public static final DeferredItem<Item> CPU_BASE = ITEMS.register("cpu_a",
            () -> new ProcessorItem(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<Item> FLOPPY_DISK = ITEMS.register("floppy_a",
            () -> new GenericStorageItem(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<Item> EEPROM = ITEMS.register("eeprom_a",
            () -> new EproomItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
