package net.venitstudios.darkcomputers.screen.custom.programmer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.network.ModPayloads;

public class ProgrammerScreen extends AbstractContainerScreen<ProgrammerMenu> {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/programmer/programmer_background.png");
    private static final ResourceLocation FLOPPY_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/programmer/disk_slot.png");
    private static final ResourceLocation EEPROM_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/programmer/rom_slot.png");
    private ProgrammerMenu programmerMenu;
    public ProgrammerScreen(ProgrammerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.programmerMenu = menu;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        PacketDistributor.sendToServer(new ModPayloads.ioKeyAction(programmerMenu.blockEntity.getBlockPos(), keyCode, modifiers, true));
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        PacketDistributor.sendToServer(new ModPayloads.ioKeyAction(programmerMenu.blockEntity.getBlockPos(), keyCode, modifiers, false));
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 48, this.imageHeight - 92, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), x, y);
        }
    }
}
