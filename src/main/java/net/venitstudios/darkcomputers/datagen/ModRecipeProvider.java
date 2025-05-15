package net.venitstudios.darkcomputers.datagen;

import net.minecraft.world.item.Items;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.ModBlocks;
import net.venitstudios.darkcomputers.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TERMINAL_BLOCK.get())
                .pattern("IGI")
                .pattern("IRI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE);
    }
}