package net.venitstudios.darkcomputers.screen.custom.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.custom.TerminalBlockEntity;
import net.venitstudios.darkcomputers.computing.components.terminal.TextEditor;
import net.venitstudios.darkcomputers.network.ModPayloads;

import java.awt.im.InputContext;
import java.util.Arrays;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/expanded_background.png");
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/slot/disk_slot.png");
    private static final ResourceLocation TAB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DarkComputers.MOD_ID, "textures/gui/vanilla/tab_left_top.png");
    private final TerminalMenu terminalMenu;
    public String[] files = new String[0];
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (terminalMenu != null) {
            PacketDistributor.sendToServer(new ModPayloads.ioCharType(terminalMenu.blockEntity.getBlockPos(), codePoint, modifiers));
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        PacketDistributor.sendToServer(
                new ModPayloads.ioKeyAction(
                        terminalMenu.blockEntity.getBlockPos(),
                        keyCode,
                        modifiers,
                        true)
        );

        if (keyCode == InputConstants.KEY_V && modifiers == InputConstants.MOD_CONTROL) {
            String clipboardContents = Minecraft.getInstance().keyboardHandler.getClipboard();
            for (char ch : clipboardContents.toCharArray()) {
                if (ch == '\n') {

                    PacketDistributor.sendToServer(
                            new ModPayloads.ioKeyAction(
                                    terminalMenu.blockEntity.getBlockPos(),
                                    InputConstants.KEY_RETURN,
                                    modifiers,
                                    true)
                    );

                } else {
                    PacketDistributor.sendToServer(
                            new ModPayloads.ioCharType(
                                    terminalMenu.blockEntity.getBlockPos(),
                                    ch,
                                    modifiers)
                    );
                }
            }

            PacketDistributor.sendToServer(
                    new ModPayloads.ioKeyAction(
                    terminalMenu.blockEntity.getBlockPos(),
                    InputConstants.KEY_RETURN,
                    modifiers,
                    true)
            );
        }

        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.terminalMenu = menu;
        this.imageWidth = 256;
        this.imageHeight = 256;
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
    protected void containerTick() {
        super.containerTick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        TerminalBlockEntity entity = terminalMenu.blockEntity;
        TextEditor editor = entity.editor;

        editor.currentScreen = this;

        int x1 = this.leftPos + 10;
        int y1 = this.topPos + 20;

        // yay hard coded values!
        int x2 = 236;
        int y2 = 144;

        int x3 = x1 + x2;
        int y3 = y1 + y2;

        int rowCount = ((y3 - y1) / 8) - 2;
        int colCount = ((x3 - x1) / 6);

        if (!entity.editingFile) {
            editor.curCol = 0;
            // probably not editing file, so show file browser
            if (files.length == 0) {
                guiGraphics.drawString(this.font, Component.translatable("gui.darkcomputers.terminal.request_item"),
                        x1, y1 + 4, 0xFFFFFFFF, false);
            }
            else {
                guiGraphics.drawString(this.font, Component.translatable("gui.darkcomputers.terminal.request_selection"),
                        x1, y1 + 4, 0xFFFFFFFF, false);

                editor.curRowOffset = Math.round((float) (editor.curRow / (rowCount - 2))) * (rowCount - 2);
                editor.curColOffset = Math.max(0, editor.curCol - (colCount - 7));
                editor.curRow = Math.max(0, Math.min(files.length - 1, editor.curRow));

                int cursorColor = 0xFFAB33AB;

                guiGraphics.drawString(this.font, ">",
                        x1 + 6, y1 + 16 + (editor.curRow * 8) - (editor.curRowOffset * 8), 0xFF3DFF3D, false);

                for (int r = 2; r < rowCount; r++) {
                    int cr = editor.curRowOffset + (r - 2);
                    if (cr < files.length) {

                        String fileName = files[cr];
                        guiGraphics.drawString(this.font, fileName, x1 + 16, y1 + (r * 8), 0xFF3DFF3D, false);

                    }

                }

            }

        }

        if (entity.editingFile) {
            // text editor gui

            String rowColInfo = String.format("L %03d C %03d", editor.curRow, editor.curCol);
            guiGraphics.fill(x1, y1, x3, y1 + 9, 0xFFA5A5A5);
            guiGraphics.drawString(this.font, rowColInfo, x3 - (rowColInfo.length() * 6), this.topPos + 21, 0xFF404040, false);

            if (editor.currentFile != null) {
                guiGraphics.drawString(this.font, editor.currentFile.getName(), x1 + 8, this.topPos + 21, 0xFF404040, false);
            }

            editor.curRowOffset = Math.round((float) (editor.curRow / (rowCount - 2))) * (rowCount - 2);
            editor.curColOffset = Math.max(0, editor.curCol - (colCount - 7));
            // render text contents
            for (int r = 2; r < rowCount; r++) {
                int cr = editor.curRowOffset + (r - 2); // current row
                if (cr < editor.fileContents.length) {
                    String line = editor.fileContents[cr];
                    String format = String.format("%03d>", cr);
                    int formatLength = format.length();
                    guiGraphics.drawString(this.font, format, x1, y1 + (r * 8), 0xFF606060);

                    if (line == null) line = "";

                    for (int c = 0; c < colCount - (formatLength); c++) {
                        int cc = editor.curColOffset + c; // current column
                        if (cc < line.length()) {
                            String chr = line.substring(cc, cc + 1);
                            guiGraphics.drawCenteredString(this.font, chr, x1 + (formatLength * 6) + 6 + (c * 6), y1 + (r * 8), 0xFFFFFFFF);
                        }
                    }
                    // cursor
                    int cx1 = Math.min(x3 - 8, x1 + (formatLength * 6) + 6 + (editor.curCol * 6));
                    int cy1 = Math.min(y1 + 16 + ((editor.curRow - editor.curRowOffset) * 8), y1 + (rowCount - 1) * 8);

                    int cursorColor = 0x55AB33AB;
                    guiGraphics.fill(cx1 - 3, cy1, cx1 + 3, cy1 + 8, cursorColor);

                }
            }
        }

        renderTooltip(guiGraphics, mouseX, mouseY);
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
