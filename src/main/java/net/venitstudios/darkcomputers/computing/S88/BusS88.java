package net.venitstudios.darkcomputers.computing.S88;

public class BusS88 {

    public byte[] memory = new byte[0];
    public ProcessorS88 processor;

    public BusS88(ProcessorS88 processor) {
        this.processor = processor;
    }

    public void resetBus() {
        memory = new byte[1024 * 1024];
    }

    public byte readByte(int address) {
        if (address >= 0 && address < memory.length) {
            return memory[address];
        }
        return 0;
    }

    public void writeByte(int address, byte data) {
        if (address >= 0 && address < memory.length) {
            memory[address] = data;
        }
    }

    public void writeShort(int address, short data) {
        if (address >= 0 && address < memory.length -1) {
            byte high = (byte) ((data >> 8) & 0xff);
            byte low = (byte) (data & 0xff);
            writeByte(address, high);
            writeByte(address+1, low);
        }
    }

    public short readShort(int address) {
        byte byteA = readByte(address);
        byte byteB = readByte(address + 1);
        return (short) (((byteA & 0xFF) << 8) | (byteB & 0xFF));
    }



}
