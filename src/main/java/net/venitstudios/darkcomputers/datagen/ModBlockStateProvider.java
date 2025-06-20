package net.venitstudios.darkcomputers.datagen;

import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, DarkComputers.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {


        // imported from blockbench
//        blockWithMultipleTextures(ModBlocks.TERMINAL_BLOCK,
//                "block/opencomputers/terminal_top", "block/opencomputers/terminal_side",
//                "block/opencomputers/terminal_front", "block/opencomputers/terminal_side",
//                "block/opencomputers/terminal_side", "block/opencomputers/terminal_side");

        blockWithMultipleTextures(ModBlocks.COMPUTER_BLOCK,
                "block/opencomputers/case_top", "block/opencomputers/case_side",
                "block/opencomputers/case_front", "block/opencomputers/case_side",
                "block/opencomputers/case_side", "block/opencomputers/case_side");
    }


    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockWithMultipleTextures(DeferredBlock<?> block, String top, String bottom, String north, String south, String east, String west) {
        simpleBlockWithItem(block.get(),
                models().cube(block.getId().getPath(),
                        modLoc(bottom),
                        modLoc(top),
                        modLoc(north),
                        modLoc(south),
                        modLoc(east),
                        modLoc(west)
                )
        );
    }

}