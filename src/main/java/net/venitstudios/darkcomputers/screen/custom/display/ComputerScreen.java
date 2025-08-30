package net.venitstudios.darkcomputers.screen.custom.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.computing.S88.PPUS88;
import net.venitstudios.darkcomputers.computing.components.processor.ProcessorDC16;
import net.venitstudios.darkcomputers.network.ModPayloads;

import java.awt.*;
import java.nio.file.Files;
import java.util.Arrays;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/expanded_background.png");
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/slot/rom_slot.png");
    private static final ResourceLocation TAB_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/vanilla/tab_left_top.png");
    private ComputerMenu cdm;
    public ComputerScreen(ComputerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.cdm = menu;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        PacketDistributor.sendToServer(new ModPayloads.ioKeyAction(cdm.blockEntity.getBlockPos(), keyCode, modifiers, true));
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        PacketDistributor.sendToServer(new ModPayloads.ioKeyAction(cdm.blockEntity.getBlockPos(), keyCode, modifiers, false));
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("CPU Reset"), (button) -> {
                            PacketDistributor.sendToServer(new ModPayloads.cpuResetReq(cdm.blockEntity.getBlockPos()));
                            this.minecraft.screen.setFocused(null);
                        })
                        .pos(this.leftPos + this.imageWidth + 64, this.topPos + 65)
                        .size(64, 32)
                        .build()
        );
    }


    private static final byte[] RENDER_BITMASKS = {
            (byte)0x01, (byte)0x02, (byte)0x04, (byte)0x08,
            (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x80
    };
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTooltip(guiGraphics, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int x1 = this.leftPos + 14;
        int y1 = this.topPos + 24;

        PPUS88 ppu = cdm.blockEntity.bus.ppu;

        byte[] charBuf = ppu.charBuf;
        byte[] romBuf = ppu.charRom;

        for (int ly1 = 0; ly1 < ppu.screenHeight; ly1++) {
            for (int lx1 = 0; lx1 < ppu.screenWidth; lx1++) {
                int curChar = (charBuf[(ly1 * ppu.screenWidth) + lx1]) * 8;

                for (int ly2 = 0; ly2 < 8; ly2++) {
                    for (int lx2 = 0; lx2 < 8; lx2++) {

                        byte mask = (byte) (1 << lx2);

                        if ((curChar + ly2 > 0) && (curChar + ly2 < romBuf.length) && (romBuf[curChar + ly2] & mask) > 0) {
                            guiGraphics.fill(
                                    x1 + (lx1 * 8) + lx2,
                                    y1 + (ly1 * 8) + ly2,
                                    x1 + (lx1 * 8) + lx2 + 1,
                                    y1 + (ly1 * 8) + ly2 + 1,
                                    0xFFFFFFFF);
                        }
                    }
                }

            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TAB_TEXTURE, x-28, y + 12, 0, 0, 28, 28, 32, 28);
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.blit(SLOT_TEXTURE, x-19, y + 17, 0, 0, 18, 18, 18, 18);
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
