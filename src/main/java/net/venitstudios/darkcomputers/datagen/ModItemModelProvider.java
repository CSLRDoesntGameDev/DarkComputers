package net.venitstudios.darkcomputers.datagen;

import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DarkComputers.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.CPU_BASE.get());
        basicItem(ModItems.FLOPPY_DISK.get());
    }
}