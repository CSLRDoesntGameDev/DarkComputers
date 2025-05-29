package net.venitstudios.darkcomputers.computing.components.terminal;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.TerminalBlockEntity;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import org.checkerframework.checker.units.qual.C;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class TextEditor {
    public TerminalBlockEntity blockEntity;
    public String[] FileContents = new String[8];
    // current column cursor is on
    public int CurCol = 0;

    // current row cursor is on
    public int CurRow = 0;

    public boolean EditingFile = false;

    public TextEditor(TerminalBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void resetEditor() {
        this.CurRow = 0;
        this.CurCol = 0;
        this.FileContents = new String[] {"","","","","","","",""};
        this.blockEntity.editingFile = false;
    }

    public void loadFile(File file) {
        resetEditor();

        try {
            int lncnt = 0;
            byte[] bytes = Files.readAllBytes(file.toPath());

            for (int i = 0; i < bytes.length; i++) {
                char charVal = (char)bytes[i];

                if (charVal == '\n') {
                    lncnt += 1;
                    this.FileContents = Arrays.copyOf(FileContents, FileContents.length + 1);
                    FileContents[FileContents.length - 1] = "";

                } else {
                    if (charVal == '\t') {
                        this.FileContents[lncnt] += " ";
                        continue;
                    }
                    this.FileContents[lncnt] += charVal;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.blockEntity.editingFile = true;
    }

    public void charTyped(char chr, int modifiers) {

        String chrstr = String.valueOf(chr);

        if (CurRow < FileContents.length) {
            String line = FileContents[CurRow];
            if (line == null) line = "";
            CurCol = Math.min(CurCol, line.length());
            if (line == null) line = "";
            DarkComputers.LOGGER.info(line.length() + " " + line + " " + CurRow + " " + CurCol);
            if (CurCol <= line.length()) {
                // Insert character at cursor
                String modified = line.substring(0, CurCol) + chrstr + line.substring(CurCol);
                FileContents[CurRow] = modified;
                DarkComputers.LOGGER.info(modified);
                CurCol += 1;
            }

        }

    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        int modStep = modifiers == 2 ? 4 : 1;
        String line = "";
        switch (keyCode) {
            case 264: // down arrow
                if (CurRow + 2 > FileContents.length) {
                    FileContents = Arrays.copyOf(FileContents, FileContents.length + 1);
                    FileContents[FileContents.length - 1] = "";
                }
                CurRow += 1;
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 265: // up arrow
                CurRow -= CurRow > 0 ? 1 : 0;
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 262: // right arrow
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol += CurCol <= line.length() ? modStep : 0;
                break;
            case 263: // left arrow
                CurCol -= CurCol > 0 ? modStep : 0;
                break;
            case 257: // enter
                if (CurRow + 2 > FileContents.length) {
                    FileContents = Arrays.copyOf(FileContents, FileContents.length + 1);
                    FileContents[FileContents.length - 1] = "";
                }
                CurRow += 1;
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 261: // delete
                line = FileContents[CurRow];
                if (line == null) line = "";
                if (CurCol < line.length()) {
                    String modified = line.substring(0, CurCol) + line.substring(CurCol + 1);
                    FileContents[CurRow] = modified;
                }
                break;
            case 259: // backspace
                line = FileContents[CurRow];
                if (line == null) line = "";
                if (CurCol > 0 && CurCol <= line.length()) {
                    String modified = line.substring(0, CurCol - 1) + line.substring(CurCol);
                    FileContents[CurRow] = modified;
                    CurCol -= 1;
                }
                break;
        }
    }
}
