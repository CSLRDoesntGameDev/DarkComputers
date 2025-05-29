package net.venitstudios.darkcomputers.screen.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.TerminalBlockEntity;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.computing.components.terminal.TextEditor;

import java.io.File;
import java.util.Arrays;

public class TerminalInvScreen extends AbstractContainerScreen<TerminalInvMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/vanilla/terminal_background.png");
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/vanilla/gui_slot.png");
    private static final ResourceLocation TAB_TEXTURE = ResourceLocation.fromNamespaceAndPath(DarkComputers.MOD_ID, "textures/gui/vanilla/tab_left_top.png");
    private TerminalInvMenu terminalMenu;

    // used to offset text when cursor goes off-screen
    public int CurRowOffset = 0;
    public int CurColOffset = 0;
    public boolean editingFile = false;
    public File[] files = new File[0];
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (terminalMenu != null) {
            TerminalBlockEntity entity = terminalMenu.blockEntity;
                entity.editor.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (terminalMenu != null) {
            TerminalBlockEntity entity = terminalMenu.blockEntity;
            boolean sendKey = true;
            switch (keyCode) {
                case 257:
                    if (!entity.editingFile) {
                        File selectedFile = this.files[entity.editor.CurRow];
                        DarkComputers.LOGGER.info(selectedFile.toPath().toString() + " got selected");
                        sendKey = false;
                        entity.editor.loadFile(selectedFile);
                    }
                    break;
            }

            if (sendKey) {
                entity.editor.keyPressed(keyCode, scanCode, modifiers);
            }

            if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public TerminalInvScreen(TerminalInvMenu menu, Inventory playerInventory, Component title) {
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
        this.files = GenericStorageItem.getFilesAt(entity.storageStack);

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
            editor.CurCol = 0;
            // probably not editing file, so show file browser
            if (entity.storageStack == ItemStack.EMPTY || entity.storageStack.getCount() == 0) {
                guiGraphics.drawString(this.font, Component.translatable("gui.darkcomputers.terminal.request_item"),
                        x1, y1 + 4, 0xFFFFFFFF, false);
            }
            else {
                guiGraphics.drawString(this.font, Component.translatable("gui.darkcomputers.terminal.request_selection"),
                        x1, y1 + 4, 0xFFFFFFFF, false);

                CurRowOffset = Math.round((float) (editor.CurRow / (rowCount - 2))) * (rowCount - 2);
                CurColOffset = Math.max(0, editor.CurCol - (colCount - 7));

                editor.CurRow = Math.max(0, Math.min(files.length - 1, editor.CurRow));

                int cursorColor = 0xFFAB33AB;
                guiGraphics.drawString(this.font, ">",  x1 + 6, y1 + 16 + (editor.CurRow * 8) - (CurRowOffset * 8), 0xFF3DFF3D, false);

                for (int r = 2; r < rowCount; r++) {
                    int cr = CurRowOffset + (r - 2); // current column
                    if (cr < files.length) {
                        String fileName = files[cr].getName();
                        guiGraphics.drawString(this.font, fileName, x1 + 16, y1 + (r * 8), 0xFF3DFF3D, false);
                    }
                }

            }
        }

        if (entity.editingFile) {
            // text editor gui

            String rowColInfo = String.format("L %03d C %03d", editor.CurRow, editor.CurCol);
            guiGraphics.fill(x1, y1, x3, y1 + 9, 0xFFA5A5A5);
            guiGraphics.drawString(this.font, rowColInfo, x3 - (rowColInfo.length() * 6), this.topPos + 21, 0xFF404040, false);

            CurRowOffset = Math.round((float) (editor.CurRow / (rowCount - 2))) * (rowCount - 2);
            CurColOffset = Math.max(0, editor.CurCol - (colCount - 7));

            // render text contents
            for (int r = 2; r < rowCount; r++) {
                int cr = CurRowOffset + (r - 2); // current column
                if (cr < editor.FileContents.length) {
                    String line = editor.FileContents[cr];
                    String format = String.format("%03d>", cr);
                    int formatLength = format.length();
                    guiGraphics.drawString(this.font, format, x1, y1 + (r * 8), 0xFF606060);

                    if (line == null) line = "";

                    for (int c = 0; c < colCount - (formatLength + 3); c++) {
                        int cc = CurColOffset + c; // current column
                        if (cc < line.length()) {
                            String chr = line.substring(cc, cc + 1);
                            guiGraphics.drawCenteredString(this.font, chr, x1 + (formatLength * 6) + 6 + (c * 6), y1 + (r * 8), 0xFFFFFFFF);
                        }
                    }
                    // cursor
                    int cx1 = Math.min(x3 - 8, x1 + (formatLength * 6) + 6 + (editor.CurCol * 6));
                    int cy1 = Math.min(y1 + 16 + ((editor.CurRow - CurRowOffset) * 8), y1 + (rowCount - 1) * 8);

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
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 48, this.imageHeight - 94 + 2, 0x404040, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), x, y);
        }
    }


}
