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
                .pattern("   ")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COMPUTER_BLOCK.get())
                .pattern("IGI")
                .pattern("BCB")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('B', Items.IRON_BARS)
                .define('C', Items.CLOCK)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.INTERFACE_BLOCK.get())
                .pattern("IEI")
                .pattern("IRI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE_BLOCK)
                .define('E', ModItems.EEPROM)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EEPROM.get())
                .pattern(" G ")
                .pattern(" R ")
                .pattern(" P ")
                .define('G', Items.GOLD_NUGGET)
                .define('R', Items.REDSTONE)
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EEPROM_PROGRAMMER.get())
                .pattern(" E ")
                .pattern(" R ")
                .pattern(" L ")
                .define('L', Items.IRON_INGOT)
                .define('E', ModItems.EEPROM)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLOPPY_DISK.get())
                .pattern("PI ")
                .pattern("R  ")
                .pattern("   ")
                .define('I', Items.IRON_NUGGET)
                .define('R', Items.REDSTONE)
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);
    }
}