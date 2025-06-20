package net.venitstudios.darkcomputers.computing.compiler;

import net.venitstudios.darkcomputers.DarkComputers;

import java.io.*;
import java.util.*;

public class CompilerDC16 {
    public class InstructionTemplate {
        public final short opcode;
        public final String[] operands;

        public InstructionTemplate(short opcode, String[] operands) {
            this.opcode = opcode;
            this.operands = operands;
        }
    }

    public static final String srcPath = "assets/darkcomputers/compiler_langsrc/dc16.csv";
    public static final String LABEL_TAG = ".LABEL";
    public static final String CONSTANT_TAG = ".CONST";
    public static final String ORIGINATOR_TAG = ".ORIGIN";
    private Map<String, InstructionTemplate> instructions = new HashMap<>();
    private Map<String, Short> labels = new HashMap<>();
    private Map<String, Short> constants = new HashMap<>();

    public void createAssemblyTarget() {
        instructions.clear();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(srcPath);
        if (inputStream != null) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] lineData = line.split(",");
                        short byteVal = Short.decode(lineData[0]);
                        String tag = lineData[1];
                        String[] operands = Arrays.copyOfRange(lineData, 2, lineData.length);
                        instructions.put(tag, new InstructionTemplate(byteVal, operands));
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }


    private short parseOperand(String operand) {
        operand = operand.replace(",", "").trim();

        if (operand.startsWith("#")) {
            operand = operand.substring(1);
        }

        if (labels.containsKey(operand)) {
            return labels.get(operand);
        }

        if (constants.containsKey(operand)) {
            return constants.get(operand);
        }

        try {
            if (operand.startsWith("0x") || operand.startsWith("0X")) {
                return (short) Integer.parseInt(operand.substring(2), 16);
            }
            return Short.decode(operand);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid operand: " + operand);
        }
    }
    public short[] assembleFile(String filePath) {
        createAssemblyTarget();
        String outputPath = DarkComputers.modDataStoragePath + "/" + new File(filePath).getName() + "_bin";
        collectInformation(filePath);
        System.out.println("Labels: " + labels.toString() + " Variables: " + constants.toString());

        List<Short> shortList = new ArrayList<>(); // To store the shorts

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // Use ByteArrayOutputStream to capture data for the short array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath));
                     DataOutputStream arrayDos = new DataOutputStream(byteArrayOutputStream)) { // Also write to array stream
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.split(";", 2)[0].trim(); // remove comments

                        if (!line.isEmpty()) {
                            String[] lineArgs = line.split("\\s+"); // split on whitespace
                            String tag = lineArgs[0];

                            if (instructions.containsKey(tag)) {
                                short opcode = instructions.get(tag).opcode;
                                String[] operandTypes = instructions.get(tag).operands;

                                byte[] result = new byte[8];
                                result[0] = (byte) ((opcode >> 8) & 0xFF);
                                result[1] = (byte) (opcode & 0xFF);

                                int argIndex = 1;
                                for (int i = 0; i < 3; i++) {
                                    String operandType = operandTypes[i];
                                    short value = 0x0000;

                                    if (!operandType.equals("NOUSE")) {
                                        if (argIndex < lineArgs.length) {
                                            value = parseOperand(lineArgs[argIndex++]);
                                        } else {
                                            argIndex++;
                                        }
                                    }

                                    result[2 + (i * 2)] = (byte) ((value >> 8) & 0xFF);  // high byte
                                    result[3 + (i * 2)] = (byte) (value & 0xFF);         // low byte
                                }
                                dos.write(result); // Write to file
                                arrayDos.write(result); // Write to the array stream
                            }
                            else {
//                                System.err.println("UNKNOWN OPCODE: " + tag);
                            }
                        }
                    }
                }

                byte[] rawBytes = byteArrayOutputStream.toByteArray();
                for (int i = 0; i < rawBytes.length; i += 2) {
                    // Combine two bytes into a short
                    short s = (short) (((rawBytes[i] & 0xFF) << 8) | (rawBytes[i+1] & 0xFF));
                    shortList.add(s);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        short[] resultArray = new short[shortList.size()];
        for (int i = 0; i < shortList.size(); i++) {
            resultArray[i] = shortList.get(i);
        }
        return resultArray;
    }

    public void collectInformation(String filePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;

                labels = new HashMap<>();
                constants = new HashMap<>();

                int currentAddress = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.split(";", 2)[0].trim(); // remove comments

                    if (!line.isEmpty()) {
                        String[] lineArgs = line.split("\\s+"); // split on whitespace

                        String tag = lineArgs[0];



                        switch (tag) {
                            case (ORIGINATOR_TAG): {
                                currentAddress = Integer.decode(lineArgs[1]);
                                break;
                            }
                            case (CONSTANT_TAG): {
                                constants.put(lineArgs[1], Short.decode(lineArgs[2]));
                                break;
                            }
                            case (LABEL_TAG): {
                                labels.put(lineArgs[1], (short) currentAddress);
                                break;
                            }
                        }
                        if (instructions.containsKey(tag)) {
                            currentAddress += 4;
                        }
                    }

                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }


}
