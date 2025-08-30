package net.venitstudios.darkcomputers.computing.components.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.DarkComputers;
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
    public boolean renamingFile = false;
    public String currentFileName = "";
    public String newFileName = "";
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
                if (line != null && !line.trim().isEmpty()) {
                    builder.append(line).append("\n");
                }
            }
            byte[] data = builder.toString().getBytes(StandardCharsets.UTF_8);
            GenericStorageItem.writeData(this.currentFile.getName(), disk, 0 , data, true);
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

    public File renameFile(File file, String newName) {
        if ( isServer() && !newName.isEmpty()) {
            File nf = new File(file.getAbsolutePath().replaceAll(file.getName(), newName));
            file.renameTo(nf);
            return nf;
        }
        return null;
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
            if (renamingFile) {
                newFileName = (newFileName + chrstr).substring(0, Math.min(newFileName.length() + 1, 20)).trim();
            } else {
                if (curRow < fileContents.length) {
                    String line = fileContents[curRow];
                    if (line == null) line = "";
                    curCol = Math.min(curCol, line.length());
                    if (curCol <= line.length()) {
                        // Insert character at cursor
                        String modified = line.substring(0, curCol) + chrstr + line.substring(curCol);
                        fileContents[curRow] = modified;
                        curCol += 1;
                    }

                }
            }
        }

    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        editingFile = blockEntity.editingFile;
        boolean server = isServer();
//        DarkComputers.LOGGER.info(keyCode + " " + scanCode + " " + modifiers);
        renamingFile = renamingFile && blockEntity.editingFile;
        if ( isServer() ) {
            int modStep = modifiers == 2 ? 4 : 1;
            String line = "";
            switch (keyCode) {
                case InputConstants.KEY_R:
                    if (modifiers == 2) {
                        renamingFile = true;
                        newFileName = currentFileName;
                    }
                    break;
                case InputConstants.KEY_E:
                    if (modifiers == 2) {
                        resetEditor();
                    }
                    break;
                case InputConstants.KEY_N:
                    if (modifiers == 2 && !editingFile) {
                        newFile();
                    }
                    break;
                case InputConstants.KEY_S:
                    if (modifiers == 2) {
                        saveFile();
                    }
                    break;
                case InputConstants.KEY_DOWN:
                    if (curRow >= fileContents.length) {
                        fileContents = Arrays.copyOf(fileContents, curRow);
                        for (int i = 0; i < curRow; i++) {
                            if (fileContents[i] == null) {
                                fileContents[i] = "";
                            }
                        }
//                        DarkComputers.LOGGER.info("CREATED NEW LINE! " + fileContents.length + " " + curRow);
                    }

                    curRow += 1;
                    if (curRow < fileContents.length) {
                        line = fileContents[curRow];
                        if (line == null) line = "";
                        curCol = Math.min(curCol, line.length());
                    }
                    break;
                case InputConstants.KEY_UP:
                    curRow -= curRow > 0 ? 1 : 0;
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    curCol = Math.min(curCol, line.length());
                    break;
                case InputConstants.KEY_RIGHT: // right arrow
                    line = fileContents[curRow];
                    if (line == null) line = "";
                    curCol += curCol <= line.length() ? modStep : 0;
                    break;
                case InputConstants.KEY_LEFT:
                    curCol -= curCol > 0 ? modStep : 0;
                    break;
                case InputConstants.KEY_RETURN:
                    if (blockEntity.editingFile && !renamingFile) {
                        // stolen from stack overflow :)
                        if (curRow < fileContents.length) {
                            String lineBefore = fileContents[curRow] != null ? fileContents[curRow] : "";

                            String before = lineBefore.substring(0, curCol);
                            String after = lineBefore.substring(curCol);

                            fileContents = Arrays.copyOf(fileContents, fileContents.length + 1);

                            for (int i = fileContents.length - 2; i >= curRow + 1; i--) {
                                fileContents[i + 1] = fileContents[i];
                            }

                            fileContents[curRow] = before;
                            if (curRow + 1 >= fileContents.length) {
                                fileContents = Arrays.copyOf(fileContents, curRow + 1);
                            }
                            fileContents[curRow + 1] = after;

                            curRow += 1;
                            curCol = 0;
                        }
                    } else if (!blockEntity.editingFile && !renamingFile) {
                        File[] files = GenericStorageItem.getFilesAt(blockEntity.storageStack);
                        files = Arrays.stream(files).sorted().toArray(File[]::new);
                        if (this.curRow >= 0 && this.curRow < files.length) {
                            File selectedFile = files[this.curRow];
                            loadFile(selectedFile);
                            curRow = 0;
                        }
                    } else {
                        File file = renameFile(currentFile, newFileName);
                        if (file == null) {
                            loadFile(file);
                            newFileName = currentFileName;
                        }
                        renamingFile = false;
                    }
                    break;
                case InputConstants.KEY_DELETE:
                    if (curRow < fileContents.length) {

                        line = fileContents[curRow];
                        if (line == null) line = "";
                        if (curCol < line.length()) {
                            String modified = line.substring(0, curCol) + line.substring(curCol + 1);
                            fileContents[curRow] = modified;
                        }
                    }
                    break;
                case InputConstants.KEY_BACKSPACE: // backspace
                    // based on the code stolen from stack overflow :)
                    if (blockEntity.editingFile && !renamingFile) {
                        if (curRow < fileContents.length) {
                            line = fileContents[curRow];
                            if (line == null) line = "";

                            if (curCol > 0) {
                                fileContents[curRow] = line.substring(0, curCol - 1) + line.substring(curCol);
                                curCol -= 1;
                            } else if (curRow > 0) {
                                String before = fileContents[curRow - 1];
                                if (before == null) before = "";

                                fileContents[curRow - 1] = before + line;

                                for (int i = curRow; i < fileContents.length - 1; i++) {
                                    fileContents[i] = fileContents[i + 1];
                                }
                                fileContents[fileContents.length - 1] = "";

                                curRow -= 1;
                                curCol = before.length();
                            }
                        }
                    }  else if (renamingFile) {
                        newFileName = newFileName.substring(0, Math.max(newFileName.length() - 1, 0)).trim();
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
