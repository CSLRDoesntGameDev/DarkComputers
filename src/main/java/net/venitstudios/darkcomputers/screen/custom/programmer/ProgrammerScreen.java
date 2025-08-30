package net.venitstudios.darkcomputers.screen.custom.programmer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.component.ModDataComponents;
import net.venitstudios.darkcomputers.item.ModItems;
import net.venitstudios.darkcomputers.network.ModPayloads;

import java.util.Arrays;

public class ProgrammerScreen extends AbstractContainerScreen<ProgrammerMenu> {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/programmer/programmer_background.png");
    private static final ResourceLocation UP_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/vanilla/up_arrow.png");
    private static final ResourceLocation DOWN_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/vanilla/down_arrow.png");

    private ProgrammerMenu programmerMenu;
    private Inventory playerInventory;

    int fileIndex = 0;

    public ProgrammerScreen(ProgrammerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.programmerMenu = menu;
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 264: {
                fileIndex += 1;
                break;
            }
            case 265: {
                fileIndex -= 1;
                break;
            }
        }

        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {

        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Player player = playerInventory.player;
        ItemStack inventoryStack = ItemStack.EMPTY;

        if (player.getMainHandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
            inventoryStack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem().equals(ModItems.EEPROM_PROGRAMMER.get())) {
            inventoryStack = player.getOffhandItem();
        }

        String[] files = inventoryStack.getOrDefault(ModDataComponents.GENERIC_STORAGE_FILES, "").split(",");

        files = Arrays.stream(files).sorted().toArray(String[]::new);

        guiGraphics.drawString(this.font, ">", this.leftPos + 55, this.topPos + 18, 0xFF3DFF3D, false);

        for (int i = 0; i < files.length; i++) {
            fileIndex = Math.clamp(fileIndex, 0, files.length);
            if (i + fileIndex < files.length) {
                String text = files[i + fileIndex];
                int color = i == 0 ? 0xFF3DFF3D : 0xFF3D3D3D;
                guiGraphics.drawString(this.font, text.substring(0, Math.min(text.length(), 10)), this.leftPos + 55 + 8, this.topPos + 18 + (i * 8), color, false);
            }
        }
        guiGraphics.drawString(this.font, "IDX:" + fileIndex, this.leftPos + 55 + 64, this.topPos + 6, 0xFF3D3D3D, false);



        String[] finalFiles = files.clone(); // IDEA wouldn't stop giving me an error unless i had this here.
        this.addRenderableWidget(Button.builder(Component.translatable("gui.eeprom_program"), (button) -> {

                ItemStack floppyStorage = programmerMenu.getSlot(programmerMenu.getItems().size() - 2).getItem();
                ItemStack eepromStorage = programmerMenu.getSlot(programmerMenu.getItems().size() - 1).getItem();

                if (fileIndex < finalFiles.length) {
                    String fileName = finalFiles[fileIndex];
                    ModPayloads.programmerFlashEeprom eepromPacket = new ModPayloads.programmerFlashEeprom(
                            fileName
                    );

                    PacketDistributor.sendToServer(eepromPacket);
                }

            })
            .pos(this.leftPos + 68, this.topPos + 53)
            .size(43, 16)
            .build()
        );

        this.addRenderableWidget(Button.builder(Component.literal(""), (button) -> {

            fileIndex += 1;
            this.minecraft.screen.setFocused(null);

            })
            .pos(this.leftPos + 111, this.topPos + 53)
            .size(16, 16)
            .build()
        );

        this.addRenderableWidget(Button.builder(Component.literal(""), (button) -> {

            fileIndex -= 1;
            this.minecraft.screen.setFocused(null);

            })
            .pos(this.leftPos + 52, this.topPos + 53)
            .size(16, 16)
            .build()
        );

        guiGraphics.blit(DOWN_ARROW_TEXTURE, this.leftPos + 111, this.topPos + 53, 0, 0,16, 16, 16, 16);
        guiGraphics.blit(UP_ARROW_TEXTURE, this.leftPos + 52, this.topPos + 53, 0, 0, 16, 16, 16, 16);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }



    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, Component.translatable("item.darkcomputers.eeprom_programmer"), 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 92, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), x, y);
        }
    }
}
