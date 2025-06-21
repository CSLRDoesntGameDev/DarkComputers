package net.venitstudios.darkcomputers.block.entity;

import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.venitstudios.darkcomputers.block.entity.custom.ComputerBlockEntity;
import net.venitstudios.darkcomputers.block.entity.custom.TerminalBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, DarkComputers.MOD_ID);

    public static final Supplier<BlockEntityType<TerminalBlockEntity>> TERMINAL_BE =
            BLOCK_ENTITIES.register("terminal_be", () -> BlockEntityType.Builder.of(
                    TerminalBlockEntity::new, ModBlocks.TERMINAL_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<ComputerBlockEntity>> COMPUTER_BE =
            BLOCK_ENTITIES.register("computer_be", () -> BlockEntityType.Builder.of(
                    ComputerBlockEntity::new, ModBlocks.COMPUTER_BLOCK.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}