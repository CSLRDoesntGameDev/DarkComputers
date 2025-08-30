package net.venitstudios.darkcomputers.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.DarkComputers;

import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DarkComputers.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ITEM_UUID = register("storage_item_uuid",
            builder -> builder.persistent(Codec.STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EEPROM_FILE_NAME = register("eeprom_file_name",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> GENERIC_STORAGE_FILES = register("generic_storage_files",
            builder -> builder.persistent(Codec.STRING));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }
    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }

}
