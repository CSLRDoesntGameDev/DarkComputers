package net.venitstudios.darkcomputers.computing.components.terminal;

import net.minecraft.client.Minecraft;
import net.venitstudios.darkcomputers.DarkComputers;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TextEditor {
    public String[] FileContents = new String[8];
    // current column cursor is on
    public int CurCol = 0;

    // current row cursor is on
    public int CurRow = 0;

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
                    FileContents[FileContents.length-1] = "";
                }
                CurRow += 1;
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 265: // up arrow
                CurRow -= CurRow > 0 ? 1 : 0;
                line = FileContents[CurRow]; if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 262: // right arrow
                line = FileContents[CurRow]; if (line == null) line = "";
                CurCol += CurCol <= line.length() ? modStep : 0;
                break;
            case 263: // left arrow
                CurCol -= CurCol > 0 ? modStep : 0;
                break;
            case 257: // enter
                if (CurRow + 2 > FileContents.length) {
                    FileContents = Arrays.copyOf(FileContents, FileContents.length + 1);
                    FileContents[FileContents.length-1] = "";
                }
                CurRow += 1;
                line = FileContents[CurRow];
                if (line == null) line = "";
                CurCol = Math.min(CurCol, line.length());
                break;
            case 261: // delete
                line = FileContents[CurRow]; if (line == null) line = "";
                if (CurCol < line.length()) {
                    String modified = line.substring(0, CurCol) + line.substring(CurCol + 1);
                    FileContents[CurRow] = modified;
                }
                break;
            case 259: // backspace
                line = FileContents[CurRow]; if (line == null) line = "";
                if (CurCol > 0 && CurCol <= line.length()) {
                        String modified = line.substring(0, CurCol - 1) + line.substring(CurCol);
                        FileContents[CurRow] = modified;
                        CurCol -= 1;
                }
                break;

        }
    }
}
