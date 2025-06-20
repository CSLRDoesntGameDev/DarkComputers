package net.venitstudios.darkcomputers.computing.components.terminal;

import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.block.entity.custom.TerminalBlockEntity;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;
import net.venitstudios.darkcomputers.screen.custom.terminal.TerminalScreen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class TextEditor {
    public TerminalBlockEntity blockEntity;
    public String[] fileContents = new String[8];
    // current column cursor is on
    public int curCol = 0;

    // current row cursor is on
    public int curRow = 0;
    public int curRowOffset = 0;
    public int curColOffset = 0;
    public boolean editingFile = false;
    public String currentFileName = "";
    public TerminalScreen currentScreen;
    public File currentFile;
    public TextEditor(TerminalBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void resetEditor() {
        this.curRow = 0;
        this.curCol = 0;
        this.fileContents = new String[] {"","","","","","","",""};
        this.blockEntity.editingFile = false;
        this.currentFile = null;
    }

    public void saveFile() {
        if ( isServer() ) {
            ItemStack disk = this.blockEntity.storageStack;
            StringBuilder builder = new StringBuilder();
            for (String line : this.fileContents) {
                builder.append(line);
                builder.append("\n");
            }
            byte[] data = builder.toString().getBytes(StandardCharsets.UTF_8);
            GenericStorageItem.writeData(this.currentFile.getName(), disk, 0, data);
        }
    }
    public void newFile() {
        if ( isServer() ) {
            resetEditor();
            ItemStack disk = this.blockEntity.storageStack;
            Path path = Path.of(GenericStorageItem.getStoragePath(disk) + "New File " + GenericStorageItem.getFilesAt(disk).length);
            if (!Files.exists(path)) {
                File newFile = new File(path.toString());
                try {
                    GenericStorageItem.ensurePath(disk);
                    boolean created = newFile.createNewFile();
                    if (created) {
                        this.currentFile = newFile;
                        loadFile(newFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadFile(File file) {
        if ( isServer() ) {
            resetEditor();
            currentFile = file;
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());

                loadBytesToEdit(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.blockEntity.editingFile = true;
        }
    }

    public void loadBytesToEdit(byte[] bytes) {
        int lncnt = 0;
        this.fileContents = new String[] {"","","","","","","",""};
        for (byte aByte : bytes) {
            char charVal = (char) aByte;
            if (charVal == '\n') {
                lncnt += 1;
                this.fileContents = Arrays.copyOf(fileContents, fileContents.length + 2);
                for (int i = 0; i < this.fileContents.length; i++) {
                    if (this.fileContents[i] == null || Objects.equals(this.fileContents[i], "null")) {
                        this.fileContents[i] = "";
                    }
                }

            } else {
                if (charVal == '\t') {
                    this.fileContents[lncnt] += " ";
                    continue;
                }
                this.fileContents[lncnt] += charVal;
            }

        }
    }

    public void charTyped(char chr, int modifiers) {
        if ( isServer() ) {
            String chrstr = String.valueOf(chr);

            if (curRow < fileContents.length) {
                String line = fileContents[curRow];
                if (line == null) line = "";
                curCol = Math.min(curCol, line.length());
                if (line == null) line = "";
                if (curCol <= line.length()) {
                    // Insert character at cursor
                    String modified = line.substring(0, curCol) + chrstr + line.substring(curCol);
                    fileContents[curRow] = modified;
                    curCol += 1;
                }

            }
        }

    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean server = isServer();
        if ( isServer() ) {
            int modStep = modifiers == 2 ? 4 : 1;
            String line = "";
            switch (keyCode) {
                case 69: // e
                    if (modifiers == 2) {
                        resetEditor();
                    }
                    break;
                case 78:
                    if (modifiers == 2) {
                        newFile();
                    }
                    break;
                case 83: // s
                    if (modifiers == 2) {
                        saveFile();
                    }
                    break;
                case 264: // down arrow
                    if (curRow + 2 > fileContents.length) {
                        fileContents = Arrays.copyOf(fileContents, fileContents.length + 1);
                        fileContents[fileContents.length - 1] = "";
                    }
                    curRow += 1;
                    if (curRow < fileContents.length) {
                        line = fileContents[curRow];
                        if (line == null) line = "";
                        curCol = Math.min(curCol, line.length());
                    }
                    break;
                case 265: // up arrow
                    curRow -= curRow > 0 ? 1 : 0;
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    curCol = Math.min(curCol, line.length());
                    break;
                case 262: // right arrow
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    curCol += curCol <= line.length() ? modStep : 0;
                    break;
                case 263: // left arrow
                    curCol -= curCol > 0 ? modStep : 0;
                    break;
                case 257: // enter
                    if (blockEntity.editingFile) {
                        if (curRow + 2 > fileContents.length) {
                            fileContents = Arrays.copyOf(fileContents, fileContents.length + 1);
                            for (int i = 0; i < fileContents.length; i++) {
                                if (fileContents[i] == null) {
                                    fileContents[i] = "";
                                }
                            }
                        }
                        if (curRow < fileContents.length) {
                            curRow += 1;
                            line = fileContents[curRow];
                            if (line == null) line = "";
                            curCol = Math.min(curCol, line.length());
                        }
                    } else {
                        File[] files = GenericStorageItem.getFilesAt(blockEntity.storageStack);
                        if (this.curRow >= 0 && this.curRow < files.length) {
                            File selectedFile = files[this.curRow];
                            loadFile(selectedFile);
                            curRow = 0;
                        }
                    }

                    break;
                case 261: // delete
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    if (curCol < line.length()) {
                        String modified = line.substring(0, curCol) + line.substring(curCol + 1);
                        fileContents[curRow] = modified;
                    }
                    break;
                case 259: // backspace
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    if (curCol > 0 && curCol <= line.length()) {
                        String modified = line.substring(0, curCol - 1) + line.substring(curCol);
                        fileContents[curRow] = modified;
                        curCol -= 1;
                    }
                    if (curCol == 0 && curRow > 0) {
                        curRow -= 1;
                        line = fileContents[curRow];
                        curCol = line.length();
                    }
                    break;
            }
        }
    }



    public boolean isServer() {
        if (blockEntity.getLevel() != null) {
            return !blockEntity.getLevel().isClientSide();
        }
        return false;
    }
}
