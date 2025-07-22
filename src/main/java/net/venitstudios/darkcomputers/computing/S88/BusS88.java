package net.venitstudios.darkcomputers.computing.S88;

import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.custom.ComputerBlockEntity;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BusS88 {

    public static List<Integer> keyBuffer = new ArrayList<Integer>();
    public static final int[] SCREEN_BUF_RANGE = new int[] {0x2000, 0x21BF};
    public static final int[] CHAR_SET_BUF_RANGE = new int[] {0x2200, 0x2FFF};
    public static final int KEYBOARD_KEY_RET_ADDRESS = 0x3201;
    public static final int KEYBOARD_KEY_MOD_ADDRESS = 0x3202;
    public static final int KEYBOARD_KEY_PRB_ADDRESS = 0x3203;
    public static final int KEYBOARD_KEY_LEN_ADDRESS = 0x3204;
    public static final int START_OF_WRITABLE_SPACE = 0x1FFF;

    public ComputerBlockEntity computerBlockEntity;
    public int cyclesPerTick = 100;
    public byte[] memory = new byte[64 * 1024];
    public ProcessorS88 processor;
    public PPUS88 ppu;

    public BusS88(ProcessorS88 processor) {

        this.processor = processor;
        this.ppu = new PPUS88();
    }
    public BusS88(ComputerBlockEntity computerBlockEntity) {

        this.computerBlockEntity = computerBlockEntity;
        processor = new ProcessorS88(this);
        this.ppu = new PPUS88();

        resetBus();
    }

    public void eepromToMemory(ItemStack itemStack) {
        GenericStorageItem.ensurePath(itemStack);
        byte[] data = new byte[0];
        try {
            if (!Objects.equals(GenericStorageItem.getStorageUUID(itemStack), null)) {
                data = Files.readAllBytes(Path.of(GenericStorageItem.getStoragePath(itemStack) + "/eeprom.bin"));
            }
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.arraycopy(data, 0, this.memory, 0, Math.min(data.length, 8096));
    }

    public void resetBus() {
        memory = new byte[1024 * 1024];
        ppu.resetPPU();
        eepromToMemory(computerBlockEntity.storageStack);
    }

    public byte readByte(int address) {
        if (address >= 0 && address < memory.length) {
            return memory[address];
        }
//        DarkComputers.LOGGER.info("address out of bounds " + address);
        return 0;
    }

    public void writeByte(int address, byte data) {
        if (address >= START_OF_WRITABLE_SPACE && address < memory.length) {
            memory[address] = data;
            if (address >= SCREEN_BUF_RANGE[0] && address < SCREEN_BUF_RANGE[1]) {
//                DarkComputers.LOGGER.info("Screen Buffer Written To");
                ppu.charBuf[address - SCREEN_BUF_RANGE[0] ] = data;
            }
            if (address >= CHAR_SET_BUF_RANGE[0] && address < CHAR_SET_BUF_RANGE[1]) {
//                DarkComputers.LOGGER.info("Character Buffer Written To");
                ppu.charRom[address - CHAR_SET_BUF_RANGE[0]] = data;
            }
        }
        updateElements(address, data);
    }

    public void writeShort(int address, short data) {
        if (address >= START_OF_WRITABLE_SPACE && address < memory.length -1) {
            byte high = (byte) ((data >> 8) & 0xff);
            byte low = (byte) (data & 0xff);
            writeByte(address, high);
            writeByte(address+1, low);
        }
        updateElements(address, data);
    }

    public short readShort(int address) {
        byte byteA = readByte(address);
        byte byteB = readByte(address + 1);

        return (short) (((byteA & 0xFF) << 8) | (byteB & 0xFF));
    }


    public void updateKeyState(int keyCode, int modifiers, boolean pressed) {
        if (pressed) {
            keyBuffer.add(keyCode);
            keyBuffer.add(modifiers);
        } else {
            keyBuffer.add(keyCode | 0x8000);
            keyBuffer.add(modifiers);
        }
    }

    public void updateElements(int addr, short data) {
        this.memory[KEYBOARD_KEY_LEN_ADDRESS] = (byte) keyBuffer.size();
        switch (addr) {
            case (KEYBOARD_KEY_PRB_ADDRESS): {
                this.memory[KEYBOARD_KEY_RET_ADDRESS] = (byte) keyBuffer.getFirst().intValue();
                keyBuffer.removeFirst();
                this.memory[KEYBOARD_KEY_MOD_ADDRESS] = (byte) keyBuffer.getFirst().intValue();
                keyBuffer.removeFirst();
                this.memory[KEYBOARD_KEY_PRB_ADDRESS] = 0;
                break;
            }
        }
    }

}
