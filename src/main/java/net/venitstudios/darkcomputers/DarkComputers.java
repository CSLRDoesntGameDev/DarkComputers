package net.venitstudios.darkcomputers;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.venitstudios.darkcomputers.block.ModBlocks;
import net.venitstudios.darkcomputers.block.entity.ModBlockEntities;
import net.venitstudios.darkcomputers.component.ModDataComponents;
import net.venitstudios.darkcomputers.datagen.ModRecipeProvider;
import net.venitstudios.darkcomputers.item.ModCreativeTabs;
import net.venitstudios.darkcomputers.item.ModItems;
import net.venitstudios.darkcomputers.network.ModPayloads;
import net.venitstudios.darkcomputers.screen.ModMenuTypes;
import net.venitstudios.darkcomputers.screen.custom.programmer.ProgrammerScreen;
import net.venitstudios.darkcomputers.screen.custom.terminal.TerminalScreen;
import net.venitstudios.darkcomputers.screen.custom.display.ComputerScreen;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DarkComputers.MOD_ID)
public class DarkComputers {
    public static final String MOD_ID = "darkcomputers";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Path levelPath;
    public static Path modDataPath;
    public static Path modDataStoragePath;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DarkComputers(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        ModCreativeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        ModDataComponents.register(modEventBus);

        modEventBus.register(ModPayloads.class);

        NeoForge.EVENT_BUS.addListener(this::onWorldLoad);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void onWorldLoad(LevelEvent.Load event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Path worldPath = server.getWorldPath(LevelResource.ROOT).toAbsolutePath();
            DarkComputers.LOGGER.info("Game Loaded Level " + worldPath);
            levelPath = worldPath;

            modDataPath = Path.of(worldPath.toString() + "/data/" + DarkComputers.MOD_ID);
            modDataStoragePath = Path.of(worldPath.toString() + "/data/" + DarkComputers.MOD_ID + "/storage/");

            if (!Files.isDirectory(modDataStoragePath)) {
                boolean created = new File(modDataPath.toString()).mkdirs();
                if (created) {
                    DarkComputers.LOGGER.info("Sucesfully Made Folder for " + DarkComputers.MOD_ID + " at " + modDataPath.toString());
                }
            }

            if (!Files.isDirectory(modDataStoragePath)) {
                boolean created = new File(modDataStoragePath.toString()).mkdirs();
                if (created) {
                    DarkComputers.LOGGER.info("Sucesfully Made Folder for " + DarkComputers.MOD_ID + " Storage items at " + modDataStoragePath.toString());
                }
            }

        }
    }

    private void commonSetup(final FMLCommonSetupEvent event)  {
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        DarkComputers.LOGGER.info("SERVER STARTING");
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartingEvent event) {
        DarkComputers.LOGGER.info("SERVER STARTED");
        MinecraftServer server = event.getServer();
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.TERMINAL_MENU.get(), TerminalScreen::new);
            event.register(ModMenuTypes.COMPUTER_DISPLAY_MENU.get(), ComputerScreen::new);
            event.register(ModMenuTypes.PROGRAMMER_MENU.get(), ProgrammerScreen::new);
        }

    }
}
