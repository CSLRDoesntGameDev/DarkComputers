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
    public static final int KEYBOARD_KEY_RET_ADDRESS = 0x3200;
    public static final int START_OF_WRITABLE_SPACE = 0x1FFF;
    public static final int START_OF_BUS_DEVICES = 0x3210;
    public ArrayList<BusDevice> busDevices = new ArrayList<BusDevice>();
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

        if (address >= START_OF_BUS_DEVICES && address < START_OF_BUS_DEVICES + 0xFF) {
            return readBusDevice(address);
        }
        if (address >= 0 && address < memory.length) {
            return memory[address];
        }
//        DarkComputers.LOGGER.info("address out of bounds " + address);
        return 0;
    }

    public byte readBusDevice(int addr) {
        for (int i = 0; i < busDevices.size(); i++) {
            BusDevice device = busDevices.get(i);
            int address = getAddressOfDevice(device);
            if (addr >= address && addr < address + device.laneCount) {
                device.hasUpdated = true;
                return device.lanes[addr - address];
            }
        }
        DarkComputers.LOGGER.error("Out of bounds read address, returning 0");
        return 0;
    }

    public void writeByte(int address, byte data) {
        if (address >= START_OF_WRITABLE_SPACE && address < memory.length) {
            memory[address] = data;
            if (address >= SCREEN_BUF_RANGE[0] && address < SCREEN_BUF_RANGE[1]) {
//                DarkComputers.LOGGER.info("Screen Buffer Written To");
                ppu.charBuf[address - SCREEN_BUF_RANGE[0]] = data;
            }
            if (address >= CHAR_SET_BUF_RANGE[0] && address < CHAR_SET_BUF_RANGE[1]) {
//                DarkComputers.LOGGER.info("Character Buffer Written To");
                ppu.charRom[address - CHAR_SET_BUF_RANGE[0]] = data;
            }
        }
        if (address >= START_OF_BUS_DEVICES && address < START_OF_BUS_DEVICES + 0xFF) {
            writeBusDevice(address, data);
        }
    }

    public void writeBusDevice(int addr, int data) {
        for (int i = 0; i < busDevices.size(); i++) {
            BusDevice device = busDevices.get(i);
            int address = getAddressOfDevice(device);
            if (addr >= address && addr < address + device.laneCount) {
                device.lanes[addr - address] = (byte) data;
                device.hasUpdated = true;
            }
        }
    }



    public void writeShort(int address, short data) {
        if (address >= START_OF_WRITABLE_SPACE && address < memory.length -1) {
            byte high = (byte) ((data >> 8) & 0xff);
            byte low = (byte) (data & 0xff);
            writeByte(address, high);
            writeByte(address+1, low);
        }
    }

    public short readShort(int address) {
        if (address == KEYBOARD_KEY_RET_ADDRESS && !keyBuffer.isEmpty()) {
            int val = keyBuffer.getFirst();
            keyBuffer.removeFirst();
            return (short) val;
        }
        byte byteA = readByte(address);
        byte byteB = readByte(address + 1);
        return (short) (((byteA & 0xFF) << 8) | (byteB & 0xFF));
    }


    public void updateKeyState(int keyCode, int modifiers, boolean pressed) {
        if (pressed) {
            keyBuffer.add(keyCode);
        } else {
            keyBuffer.add(-keyCode);
        }
        DarkComputers.LOGGER.info(keyBuffer.toString());
    }

    public void addDeviceToBus(BusDevice busDevice) {
        busDevices.add(busDevice);
        busDevice.bus = this;
    }

    public void removeDevice(BusDevice busDevice) {
        busDevices.remove(busDevice);
    }

    public void removeAllDevices() {
        for (BusDevice device : busDevices) {
            removeDevice(device);
        }
    }

    public int getAddressOfDevice(BusDevice device) {
        int address = START_OF_BUS_DEVICES;
        for (BusDevice scanDevice : busDevices.stream().toList()) {
            if (scanDevice != device) {
                address += scanDevice.laneCount;
            } else {
                break;
            }
        }
//        DarkComputers.LOGGER.info("Device " + device + " At Memory " + address);
        return address;
    }


}
