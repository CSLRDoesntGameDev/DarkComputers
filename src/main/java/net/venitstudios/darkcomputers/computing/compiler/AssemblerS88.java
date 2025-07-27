package net.venitstudios.darkcomputers.computing.compiler;

import net.venitstudios.darkcomputers.DarkComputers;

import java.io.*;
import java.util.*;

public class AssemblerS88 {
    public class InstructionTemplate {
        public final short opcode;
        public final String[] operands;

        public InstructionTemplate(short opcode, String[] operands) {
            this.opcode = opcode;
            this.operands = operands;
        }
    }

    public static final String srcPath = "assets/darkcomputers/compiler_langsrc/DC-S88_ISA.csv"; // Change this line
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
                        String[] lineData = line.replaceAll("\" ", "").split(",");
                        short byteVal = Short.decode(lineData[0]);
                        String tag = lineData[2];

                        String[] operands = Arrays.copyOfRange(lineData, 3, 6);

                        instructions.put(tag, new InstructionTemplate(byteVal, operands));
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        } else {
            DarkComputers.LOGGER.info("Input Stream is Null - createAssemblyTarget");
        }
//        DarkComputers.LOGGER.info("Assembly Target Succesfully Created");
    }


    private short parseOperand(String operand) {
        operand = operand.replace(",", "").trim();
        DarkComputers.LOGGER.info("Parsing " + operand);
        if (operand.startsWith("#")) {
            operand = operand.substring(1);
            return (short) Integer.parseInt(operand);
        }

        if (labels.containsKey(operand)) {
            return labels.get(operand);
        }

        if (constants.containsKey(operand)) {
            return constants.get(operand);
        }

        try {
            if (operand.startsWith("0x") || operand.startsWith("0X")) {
                DarkComputers.LOGGER.info(String.valueOf((short) Integer.parseInt(operand.substring(2), 16)));
                return (short) Integer.parseInt(operand.substring(2), 16);
            }

            if (operand.startsWith("0b") || operand.startsWith("0B")) {
                DarkComputers.LOGGER.info(String.valueOf((short) Integer.parseInt(operand.substring(2), 16)));
                return (short) Integer.parseInt(operand.substring(2), 2);
            }
            return Short.decode(operand);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid operand: " + operand);
        }


    }

    public short[] assembleFile(String filePath, String outputPath) {
        createAssemblyTarget();
        DarkComputers.LOGGER.info("Assembling File: " + filePath + " To: " + outputPath);
        collectInformation(filePath);
        System.out.println("Labels: " + labels.toString() + " Variables: " + constants.toString());

        List<Short> shortList = new ArrayList<>(); // To store the shorts

        try (InputStream inputStream = new FileInputStream(filePath)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath));
                     DataOutputStream arrayDos = new DataOutputStream(byteArrayOutputStream)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.split(";", 2)[0].trim();

                        if (!line.isEmpty()) {
                            String[] lineArgs = line.split("\\s+");
                            String tag = lineArgs[0];

                            if (instructions.containsKey(tag)) {
                                short opcode = instructions.get(tag).opcode;
                                String[] operandTypes = instructions.get(tag).operands;

                                DarkComputers.LOGGER.info(Arrays.toString(operandTypes));

                                byte[] result = new byte[8];
                                result[0] = (byte) ((opcode >> 8) & 0xFF);
                                result[1] = (byte) (opcode & 0xFF);

                                int argIndex = 1;
                                for (int i = 0; i < 3; i++) {
                                    String operandType = operandTypes[i];
                                    short value = 0x0000;

                                    if (!operandType.equals("IGN")) {
                                        if (argIndex < lineArgs.length) {
                                            value = parseOperand(lineArgs[argIndex++]);
                                        } else {
                                            argIndex++;
                                        }
                                    }

                                    result[2 + (i * 2)] = (byte) ((value >> 8) & 0xFF);
                                    result[3 + (i * 2)] = (byte) (value & 0xFF);
                                }
                                dos.write(result);
                                arrayDos.write(result);
                            } else {
                                System.err.println("UNKNOWN OPCODE: " + tag);
                            }
                        }
                    }
                }

                byte[] rawBytes = byteArrayOutputStream.toByteArray();
                for (int i = 0; i < rawBytes.length; i += 2) {
                    short s = (short) (((rawBytes[i] & 0xFF) << 8) | (rawBytes[i+1] & 0xFF));
                    shortList.add(s);
                }

            }
        } catch (FileNotFoundException e) {
            DarkComputers.LOGGER.info("File not found: " + filePath);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        short[] resultArray = new short[shortList.size()];
        for (int i = 0; i < shortList.size(); i++) {
            resultArray[i] = shortList.get(i);
        }
        return resultArray;
    }

    public void collectInformation(String filePath) {
        // MODIFICATION HERE: Use FileInputStream for the external file
        try (InputStream inputStream = new FileInputStream(filePath)) { // Use FileInputStream directly
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;

                labels = new HashMap<>();
                constants = new HashMap<>();

                int currentAddress = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.split(";", 2)[0].trim();

                    if (!line.isEmpty()) {
                        String[] lineArgs = line.split("\\s+");

                        String tag = lineArgs[0];

                        switch (tag) {
                            case (ORIGINATOR_TAG): {
                                currentAddress = Integer.decode(lineArgs[1]);
                                break;
                            }
                            case (CONSTANT_TAG): {
                                constants.put(lineArgs[1].replace(",", ""), Short.decode(lineArgs[2]));
                                break;
                            }
                            case (LABEL_TAG): {
                                labels.put(lineArgs[1], (short) currentAddress);
                                break;
                            }
                        }
                        if (instructions.containsKey(tag)) {
                            currentAddress += 8;
                        }
                    }

                }
            }
        } catch (FileNotFoundException e) {
            DarkComputers.LOGGER.info("File not found for information collection: " + filePath);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}