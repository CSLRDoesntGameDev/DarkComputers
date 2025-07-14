package net.venitstudios.darkcomputers.computing.S88;

import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.computing.components.processor.ProcessorDC16;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProcessorS88 {

    public static class InstructionS88 {
        public final short opcode;
        public final String tag;
        public final String[] operands;

        public InstructionS88(short opcode, String tag, String[] operands) {
            this.opcode = opcode;
            this.operands = operands;
            this.tag = tag;
        }
    }
    public BusS88 bus;
    public boolean halted = true;
    private Map<Short, InstructionS88> instructions = new HashMap<>();
    public int[] REG = new int[16];
    public static final String srcPath = "assets/darkcomputers/compiler_langsrc/DC-S88_ISA.csv";
    public void writeRegister(int register, int data) {
        if (register >= 0 && register < REG.length) {
            REG[register] = data;
        }
    }

    public int readRegister(int register) {
        if (register >= 0 && register < REG.length) {
            return REG[register];
        }
        return 0;
    }



    public ProcessorS88() {
        dumpFromCSV();
        bus = new BusS88(this);
    }

    public void resetCPU() {
        REG = new int[16];
        halted = false;
    }

    public byte readByte(int address) {
        return bus.readByte(address);
    }

    public void writeShort(int address, short data) {
        bus.writeShort(address, data);
    }
    public short readShort(int address) {
        return bus.readShort(address);
    }

    public short[] readInstructionData() {
        short[] data = new short[3];

        for (int i = 0; i < 3; i++) {
            readShort(getPC());
            incPC();
        }

        return data;
    }

    public void writeByte(int address, byte data) {
        bus.writeByte(address, data);
    }

    public int getPC() { return REG[14]; }
    public void setPC(int value) { REG[14] = value; }
    public void incPC() { setPC(getPC()+1); }
    public void decPC() { setPC(getPC()-1); }

    public int getSP() { return REG[15]; }
    public void setSP(int value) { REG[15] = value; }
    public void incSP() { setPC(getSP()+1); }
    public void decSP() { setPC(getSP()-1); }

        public void dumpFromCSV() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(srcPath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] lineData = line.split(",");
                        short byteVal = Short.decode(lineData[0]);
                        String tag = lineData[1];
                        String[] operands = lineData[3].split(", ");
                        instructions.put(byteVal, new InstructionS88(byteVal, tag, operands));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void step() {
        if (halted) { return; }

        short opcode = readShort(getPC());

        if (instructions.containsKey(opcode)) {
            InstructionS88 instruction = instructions.get(opcode);
            Method opcodeMethod = null;
            try {
                opcodeMethod = getClass().getMethod(instruction.tag);
                opcodeMethod.invoke(this);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.out.println("Hit exception with opcode " + instruction.tag + ", might not exist?" );
            }
        }
    }

    public void MOV() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[2];
        writeRegister(opB, readRegister(opA));
    }

    public void LDI() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[2];
        writeRegister(opA, (int) opB);
    }

    public void LDR() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[2];
        writeRegister(opA, (int) readByte(readRegister(opB) + (int) opC) & 0xFF);
    }

    public void STR() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[2];
        writeByte(readRegister(opB) + (int)opC, (byte) (readRegister(opA) & 0xFF));
    }

    public void ADD() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) + readRegister(opA));
    }

    public void SUB() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) - readRegister(opA));
    }

    public void MUL() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) * readRegister(opA));
    }

    public void DIV() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) / readRegister(opA));
    }

    public void MOD() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) % readRegister(opA));
    }

    public void NEG() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, -readRegister(opA));
    }

    public void AND() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) & readRegister(opA));
    }
    public void OR() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) | readRegister(opA));
    }
    public void XOR() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) ^ readRegister(opA));
    }

    public void SHL() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) << readRegister(opA));
    }

    public void SHR() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        writeRegister(opB, readRegister(opB) >> readRegister(opA));
    }

    public void JMP() {
        short[] operands = readInstructionData();
        short opA = operands[0];
        setPC(readRegister(opA));
    }

    public void JSR() {
        short[] operands = readInstructionData();
        short opA = operands[0];
        PSH((short)getPC());
        setPC(readRegister(opA));
    }

    public void JIP() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1];
        if (readRegister(opB) > 0) {
            PSH((short) getPC());
            setPC(readRegister(opA));
        }
    }

    public void RET() {
        setPC(POP());
    }

    public void HLT() {
        halted = true;
    }

    public void EQ() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) == readRegister(opC)) ? 1 : 0);
    }

    public void NEQ() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) != readRegister(opC)) ? 1 : 0);
    }

    public void LT() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) < readRegister(opC)) ? 1 : 0);
    }

    public void GT() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) > readRegister(opC)) ? 1 : 0);
    }

    public void LE() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) <= readRegister(opC)) ? 1 : 0);
    }

    public void GE() {
        short[] operands = readInstructionData();
        short opA = operands[0]; short opB = operands[1]; short opC = operands[1];
        writeRegister(opA, (readRegister(opB) >= readRegister(opC)) ? 1 : 0);
    }

    public void PSH(short data) {
        if (data == 0) {
            short[] operands = readInstructionData();
            short opA = operands[0];
            incSP();
            writeShort(getSP(), (byte) (readRegister(opA) & 0xff));
        } else {
            writeShort(getSP(), data);
        }
    }

    public int POP() {
        decSP();
        return readShort(getSP());
    }

}
