package net.venitstudios.darkcomputers.computing.components.computer;

import net.minecraft.world.item.ItemStack;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.custom.ComputerBlockEntity;
import net.venitstudios.darkcomputers.computing.components.processor.ProcessorDC16;
import net.venitstudios.darkcomputers.computing.components.storage.GenericStorageItem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusDC16 {
    public ProcessorDC16 processor;
    public DD_DC16 displayDriver;
    public ComputerBlockEntity computerBlockEntity;
    public short[] memory;
    public int cyclesPerTick = 64;
    public int cyclesRan = 0;
    public static List<Integer> keyBuffer = new ArrayList<Integer>();
    public static final int[] DISPLAY_RANGE = new int[] {0x7000, 0x7200};
    public static final int KEYBOARD_KEY_RET_ADDRESS = 0x7201;
    public static final int KEYBOARD_KEY_MOD_ADDRESS = 0x7202;
    public static final int KEYBOARD_KEY_PRB_ADDRESS = 0x7203;
    public static final int KEYBOARD_KEY_LEN_ADDRESS = 0x7204;
    public BusDC16(ComputerBlockEntity computerBlockEntity) {
        this.processor = new ProcessorDC16();
        this.processor.bus = this;
        this.displayDriver = new DD_DC16();
        this.displayDriver.bus = this;
        this.memory = new short[1024 * 64];
        this.computerBlockEntity = computerBlockEntity;
    }


    public void eepromToMemory(ItemStack itemStack) {
        GenericStorageItem.ensurePath(itemStack);
        byte[] data = null;
        try {
            data = Files.readAllBytes(Path.of(GenericStorageItem.getStoragePath(itemStack) + "/eeprom.bin"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        short[] shorts = new short[data.length/2];

        for (int i = 0; i < data.length; i += 2) {
            short resultShort = (short) (((data[i] & 0xFF) << 8) | (data[i+1] & 0xFF));
            shorts[i/2] = resultShort;
        }

        System.arraycopy(shorts, 0, this.memory, 0, shorts.length);
    }

    public void resetBus() {
        this.memory = new short[1024 * 64];
        this.cyclesRan = 0;
        this.displayDriver.resetBuffer();
        eepromToMemory(computerBlockEntity.storageStack);
    }


    public void write(int addr, short data) {
        this.memory[addr] = data;
        this.updateElements(addr, data);
    }
    public short read(int addr) {
        this.memory[KEYBOARD_KEY_LEN_ADDRESS] = (short) keyBuffer.size();
        if (addr >= 0 && addr <= memory.length) {
            if (addr >= DISPLAY_RANGE[0] && addr <= DISPLAY_RANGE[1]) {
                return (short) displayDriver.readBuffer(addr);
            }
            return this.memory[addr];
        }
        return 0;
    }
    public void updateElements(int addr, short data) {
        this.memory[KEYBOARD_KEY_LEN_ADDRESS] = (short) keyBuffer.size();
        if (addr >= DISPLAY_RANGE[0] && addr <= DISPLAY_RANGE[1]) {
            if (data >= 0x00FF) {
                displayDriver.resetBuffer();
            }
            if (data != 0xFF) {
                displayDriver.writeBuffer(addr - DISPLAY_RANGE[0], data);
            }
        }
        switch (addr) {
            case (KEYBOARD_KEY_PRB_ADDRESS): {
                this.memory[KEYBOARD_KEY_RET_ADDRESS] = (short)keyBuffer.getFirst().intValue();
                keyBuffer.removeFirst();
                this.memory[KEYBOARD_KEY_MOD_ADDRESS] = (short)keyBuffer.getFirst().intValue();
                keyBuffer.removeFirst();
                this.memory[KEYBOARD_KEY_PRB_ADDRESS] = 0;
                break;
            }
        }

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

}
