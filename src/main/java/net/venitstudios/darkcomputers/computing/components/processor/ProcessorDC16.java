package net.venitstudios.darkcomputers.computing.components.processor;

import net.minecraft.client.Minecraft;
import net.venitstudios.darkcomputers.computing.components.computer.BusDC16;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ProcessorDC16 {
    public static class Instruction {
        String tag = "";
        int opcode = 0x00;
        int totalBytes = 0;

        public Instruction(String tag, int opcode, int totalBytes) {
            this.opcode = opcode;
            this.tag = tag;
            this.totalBytes = totalBytes;
        }
    }
    private static final Instruction[] INSTRUCTION_OPCODES = new Instruction[] {
            new Instruction("HLT", 0x0000, 4),
            new Instruction("LDR", 0x0081, 4),
            new Instruction("STR", 0x0082, 4),
            new Instruction("CPR", 0x0083, 4),
            new Instruction("CRR", 0x0084, 4),
            new Instruction("LDV", 0x0085, 4),
            new Instruction("STV", 0x0086, 4),
            new Instruction("NOP", 0x0087, 4),
            new Instruction("ADR", 0x0088, 4),
            new Instruction("SBR", 0x0089, 4),
            new Instruction("AND", 0x008A, 4),
            new Instruction("ORR", 0x008B, 4),
            new Instruction("NOT", 0x008C, 4),
            new Instruction("XOR", 0x008D, 4),
            new Instruction("SRR", 0x008E, 4),
            new Instruction("SRL", 0x008F, 4),
            new Instruction("ADV", 0x0090, 4),
            new Instruction("SBV", 0x0091, 4),
            new Instruction("NOP", 0x0092, 4),
            new Instruction("CMP", 0x0093, 4),
            new Instruction("JMR", 0x0094, 4),
            new Instruction("JMP", 0x0095, 4),
            new Instruction("HOP", 0x0096, 4),
            new Instruction("JEQ", 0x0097, 4),
            new Instruction("JNE", 0x0098, 4),
            new Instruction("JGT", 0x0099, 4),
            new Instruction("JLT", 0x009A, 4),
            new Instruction("JGE", 0x009B, 4),
            new Instruction("JLE", 0x009C, 4),
            new Instruction("CAL", 0x009D, 4),
            new Instruction("RET", 0x009E, 4),
            new Instruction("WCS", 0x009F, 4),
            new Instruction("HLT", 0x00A0, 4)
    };

    public BusDC16 bus;
    public boolean halted = true;
    public int PC = 0;
    public int SP = 0; // Stack Pointer
    public int SV = 0x00008000; // Stack Vector (origin of stack)
    public int IC = 0; // Ignored Cycles
    public short[] REG = new short[512];
    public short readRegister(int idx) {
        if (idx >= 0 && idx < REG.length) {
            return REG[idx];
        }
        return 0;
    }
    public void writeRegister(int idx, int data) {
        REG[idx] = (short)(data & 0xFFFF);
    }

    public short readFromBus(int idx) {
        if (bus != null) {
            return bus.read(idx);
        }
        return 0;
    }
    public void writeToBus(int idx, int data) {
        bus.write(idx, (short)(data & 0xFFFF));
    }
    public boolean Z, N, C = false;
    public void resetProcessor() {
        this.PC = 0;
        this.SP = 0;
        this.REG = new short[512];
        this.Z = false;
        this.N = false;
        this.C = false;
        this.halted = false;
        System.out.println("Finished Resetting. " + this.PC + " " + this.SP + " ");
    }

    public void clockCycle() {
        if (halted ) { return; }
        bus.cyclesRan += 1;
        int initialPC = PC;
        int opcode = readFromBus(PC);
        int opcodeAddress = ((int)opcode) & 0b01111111;

        if (IC > 0) {
            IC -= 1;
        }

        if (opcodeAddress >= 0 && opcodeAddress < INSTRUCTION_OPCODES.length) {
            Instruction instruction = INSTRUCTION_OPCODES[opcodeAddress];
            if ((instruction.opcode & 0b01111111) == (opcode & 0b01111111)) {
                try {
//                    System.out.println("Trying " + instruction.tag + " " + opcode + " " + opcodeAddress);
                    Method opcodeMethod = getClass().getMethod(instruction.tag, Instruction.class);
                    opcodeMethod.invoke(this, instruction);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (PC == initialPC) {
                    if (IC == 0) {
                        PC += instruction.totalBytes;
                    }
                }
            }
        } else {
            System.out.println("OPCODE OUT OF BOUNDS: " + opcode + " " + opcodeAddress + " " + Integer.toHexString(bus.memory[PC]));
            halted = true;
        }
    }

    private short[] fetchInstructionData(Instruction instruction) {
        short[] args = new short[instruction.totalBytes];
//        Arrays.copyOfRange(bus.memory, PC + 1, PC + instruction.totalBytes);
        for (int i = 0; i < instruction.totalBytes; i++) {
            args[i] = readFromBus(PC + 1 + i);
        }
        return args;
    }
    public static int byteArrayToInt(byte[] input) {
        if (input == null || input.length == 0) {
            return 0;
        }
        int result = 0;
        int maxBytes = Integer.BYTES;
        int len = Math.min(input.length, maxBytes);
        for (int i = 0; i < len; i++) {
            result <<= 8;
            result |= (input[i] & 0xFF);
        }
        return result;
    }

    public void LDR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) {
            writeRegister(readRegister(args[1]), readFromBus(args[0]));
            return;
        }
        writeRegister(args[2], readFromBus(args[0]));

    }
    public void STR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) {
            writeToBus(readRegister(args[1]), readRegister(args[0]));
            return;
        }
        writeToBus(args[2], readRegister(args[0]));

    }
    public void CPR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) { writeRegister(readRegister(args[1]), readRegister(args[0]));
        return;
        }
        writeRegister(args[2], readRegister(args[0]));

    }
    public void CRR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) {
            writeToBus(readRegister(args[1]) + readRegister(args[2]), readRegister(args[0]));
        return;
        }
        writeToBus(args[2], readRegister(args[0]));

    }
    public void LDV(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) { writeRegister(readRegister(args[1]), args[0]);
        return;
        }
        writeRegister(args[2], args[0]);
    }
    public void STV(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (args[1] > 0) {
            writeToBus(readRegister(args[1]), args[0]);
        return;
        }
        writeToBus(args[2], args[0]);
    }
    public void NOP(Instruction instruction) {}
    public void ADR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) + readRegister(args[1]));
    }
    public void SBR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) - readRegister(args[1]));
    }
    public void AND(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) & readRegister(args[1]));
    }
    public void ORR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) | readRegister(args[1]));
    }
    public void NOT(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], ~readRegister(args[0]));
    }
    public void XOR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) ^ readRegister(args[1]));
    }
    public void SRR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) >> readRegister(args[1]));
    }
    public void SRL(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2], readRegister(args[0]) << readRegister(args[1]));
    }
    public void ADV(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2],args[0] + readRegister(args[1]));
    }
    public void SBV(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeRegister(args[2],args[0] - readRegister(args[1]));
    }
    public void CMP(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        int result = readRegister(args[0]) - readRegister(args[1]);
        writeRegister(args[2], result);
        Z = (result == 0); N = (result < 0);
//        System.out.println("Result of CMP " + readRegister(args[0]) + " " + readRegister(args[1]) + " " + result + " " + Z + " " + N);
    }
    public void JMR(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        PC = readRegister(args[2]);
    }
    public void JMP(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        PC = args[0];
    }
    public void HOP(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        PC = PC + args[0];
    }
    public void JEQ(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (Z) PC = args[0];
    }
    public void JNE(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (!Z) PC = args[0];
    }
    public void JGT(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if ((!Z) && (!N)) PC = args[0];
    }
    public void JLT(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (N) PC = args[0];
    }
    public void JGE(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (!N) PC = args[0];
    }
    public void JLE(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        if (Z || N) PC = args[0];
    }
    public void CAL(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        writeToBus(SV + SP, PC);
        SP += 1;
        PC = args[0];
    }
    public void RET(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        SP -= 1;
        PC = readFromBus(SV + SP);
    }

    public void WCS(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        IC = readFromBus(args[0]);
    }

    public void HLT(Instruction instruction) {
        short[] args = fetchInstructionData(instruction);
        halted = true;
    }

}

