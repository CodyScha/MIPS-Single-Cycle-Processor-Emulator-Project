/*
 * CS286-002 MIPS Single Cycle Emulation Assignment 
 * Instructor: Mark McKenney
 * Authors: Scott DeLozier, Cody Schaefer, Jared Wright
 */
package Project_1;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;

public class mipssim {
    
    //oooo globals
    public static int PC = 96;
    public static int dataStart;
    public static int[] register = new int[32];
    public static int[] data = new int[0];
    public static final int ZERO_OP = 0;
    public static final int ADD_OP = ZERO_OP;
    public static final int ADD_SPEC = 32;
    public static final int ADDI_OP = 8;
    public static final int AND_OP = ZERO_OP;
    public static final int AND_SPEC = 36;
    public static final int BEQ_OP = 4;
    public static final int BLTZ_OP = 1;
    public static final int J_OP = 2;
    public static final int JR_OP = ZERO_OP;
    public static final int JR_SPEC = 8;
    public static final int LW_OP = 3;
    public static final int MOVZ_OP = ZERO_OP;
    public static final int MOVZ_SPEC = 10;
    public static final int MUL_OP = 28;
    public static final int MUL_SPEC = 2;
    public static final int OR_OP = ZERO_OP;
    public static final int OR_SPEC = 37;
    public static final int SLL_OP = ZERO_OP;
    public static final int SLL_SPEC = 0;
    public static final int SRL_OP = ZERO_OP;
    public static final int SRL_SPEC = 2;
    public static final int SUB_OP = ZERO_OP;
    public static final int SUB_SPEC = 34;
    public static final int SW_OP = 11;
    public static final int BREAK_OP = ZERO_OP;
    public static final int BREAK_SPEC = 13;
    
    public static void main(String[] args) throws IOException {
        PrintWriter foutSim = new PrintWriter(new File("OUTPUTFILENAME_sim.txt"));
        PrintWriter foutDis = new PrintWriter(new File("OUTPUTFILENAME_dis.txt"));
        String[] binary = new String[0];
        
        //yoinked from provided "EX_readBinaryFile.java"
        File file = new File("test3.bin");
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        for (int i = 0; i < fileData.length; i += 4) {
            int x = 0;
            x = x | ((fileData[i] & 0x000000FF) << 24);
            x = x | ((fileData[i+1] & 0x000000FF) << 16);
            x = x | ((fileData[i+2] & 0x000000FF) << 8);
            x = x | (fileData[i+3] & 0x000000FF);
            
            binary = addToBinary(x, binary);
        }   

        String testInst;
        boolean isBreak = false;
        int cycleNum = 1;
        
        for (int i = 0; i < binary.length; ++i) {
            testInst = binary[i];
            isBreak = printDis(testInst, isBreak, foutDis);
            PC += 4;
        }
        foutDis.close();
        
        PC = 96;
        while (!determineOP(binary[(PC - 96) / 4]).equals("BREAK")) {
            testInst = binary[(PC - 96) / 4];
            if (isValid(testInst)) {
                PC = printSim(testInst, cycleNum, foutSim);
                cycleNum++;
            } 
            PC += 4;
        }
        testInst = binary[(PC - 96) / 4];
        PC = printSim(testInst, cycleNum, foutSim);
        
        foutSim.close();
    }
    
    //simulates and prints the disassembler
    public static boolean printDis(String testInst, boolean isBreak, PrintWriter fout) {
        long twosLong;
        String formatTest;
        String opCode;
        
        if (!isBreak) {
                formatTest = testInst.substring(0, 1) + " " + testInst.substring(1, 6) + " " + testInst.substring(6, 11) + " " 
                             + testInst.substring(11, 16) + " " + testInst.substring(16, 21) + " " + testInst.substring(21, 26)
                             + " " + testInst.substring(26, 32);
                fout.printf("%-40s%-8d", formatTest, PC);
                if (isValid(testInst)) {
                    opCode = determineOP(testInst);
                    fout.printf("%-8s", opCode);
                    fout.print(getInstString(testInst));
                    fout.println();
                    if (opCode.equals("BREAK")) {
                        isBreak = true;
                        dataStart = PC + 4;
                    }
                }
                else {
                    fout.println("Invalid Instruction");
                }
            }
            else {
                twosLong = determineTwos(testInst);
                fout.printf("%-40s%-8d", testInst, PC);
                fout.print(twosLong);
                fout.println();
                data = addToData(data, twosLong);
            }
        
        return isBreak;
    }
    
    //simulates and prints the simulator
    public static int printSim(String test, int cycle, PrintWriter fout) {
        int i;
        String opCode = determineOP(test);
        
        fout.println("====================");
        fout.println("cycle:" + cycle + "\t" + PC + "\t" + opCode + "\t" + getInstString(test));
        ALU(test);
        fout.println();
        fout.println("registers:");
        fout.print("r00:");
        for (i = 0; i < 8; ++i) {
            fout.print("\t" + register[i]);
        }
        fout.print("\nr08:");
        for (i = 8; i < 16; ++i) {
            fout.print("\t" + register[i]);
        }
        fout.print("\nr16:");
        for (i = 16; i < 24; ++i) {
            fout.print("\t" + register[i]);
        }
        fout.print("\nr24:");
        for (i = 24; i < 32; ++i) {
            fout.print("\t" + register[i]);
        }
        fout.println("\n");

        fout.println("data:");
        fout.print(dataStart + ":");
        for (i = 0; i < 8; ++i) {
            if (i <= data.length - 1) {
                fout.print("\t" + data[i]);
            }
        }
        if (data.length > 8) {
            fout.print("\n" + (dataStart + 32) + ":");
        }
        for (i = 8; i < 16; ++i) {
            if (i <= data.length - 1) {
                fout.print("\t" + data[i]);
            }
        }
        if (data.length > 16) {
            fout.print("\n" + (dataStart + 64) + ":");
        }
        for (i = 16; i < 24; ++i) {
            if (i <= data.length - 1) {
                fout.print("\t" + data[i]);
            }
        }
        fout.println("\n");
        
        return PC;
    }
    
    //Simulates the ALU and calls appropriate method
    public static void ALU(String test) {
        String opCode = determineOP(test);
        
        switch (opCode) { //this did look a lot cleaner but Linux doesn't support switch expressions >:(
            case "ADD":
                ADD(test);
                break;
            case "ADDI":
                ADDI(test);
                break;
            case "LW":
                LW(test);
                break;
            case "SW":
                SW(test);
                break;
            case "MOVZ":
                MOVZ(test);
                break;
            case "MUL":
                MUL(test);
                break;
            case "SLL":
                SLL(test);
                break;
            case "SRL":
                SRL(test);
                break;
            case "SUB":
                SUB(test);
                break;
            case "J":
                J(test);
                break;
            case "BLTZ":
                BLTZ(test);
                break;
            case "JR":
                JR(test);
                break;
            default: {
                //amogus
                break;
            }
        }
    } 
    
    //these do exactly what they say they do
    public static void ADD(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        
        register[RD] = register[RS] + register[RT];
    }
            
    public static void ADDI(String test) {
        int immVal = (int)determineTwos(test.substring(16, 32));
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        
        register[RT] = register[RS] + immVal;
    }
    
    public static void LW(String test) {
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int base = Integer.parseInt(test.substring(6, 11), 2);
        int offset = (int)determineTwos(test.substring(16, 32));
        
        register[RT] = data[(register[base] + offset - 172) / 4];
    }
    
    public static void SW(String test) {
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int base = Integer.parseInt(test.substring(6, 11), 2);
        int offset = (int)determineTwos(test.substring(16, 32));
        
        data[(register[base] + offset - 172) / 4] = register[RT];
    }
    
    public static void MOVZ(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        
        if (register[RT] == 0) {
            register[RD] = register[RS];
        }
    }
    
    public static void MUL(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        
        register[RD] = register[RS] * register[RT];
    }
    
    public static void SLL(String test) {
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        int SA = Integer.parseInt(test.substring(21, 26), 2);
        
        register[RD] = register[RT] << SA;
    }
    
    public static void SRL(String test) {
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        int SA = Integer.parseInt(test.substring(21, 26), 2);
        
        register[RD] = register[RT] >> SA;
    }
    
    public static void SUB(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        int RT = Integer.parseInt(test.substring(11, 16), 2);
        int RD = Integer.parseInt(test.substring(16, 21), 2);
        
        register[RD] = register[RS] - register[RT];
    }
    
    public static void J(String test) {
        int target = Integer.parseInt(test.substring(6, 32), 2) << 2;
        
        PC = target - 4;
    }
    
    public static void JR(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        
        PC = register[RS] - 4;
    }
    
    public static void BLTZ(String test) {
        int RS = Integer.parseInt(test.substring(6, 11), 2);
        int offset = Integer.parseInt(test.substring(16, 32), 2) << 2;
        
        if (register[RS] < 0) {
            PC += offset;
        }
    }
    
    //determines whether an instruction is valid
    public static boolean isValid(String test) {
        boolean isValid;
        
        isValid = (test.charAt(0) != '0');
        
        return isValid;
    }
    
    //determines type of instruction
    public static String determineOP(String test) {
        String opName;
        String op = test.substring(1, 6);
        int opInt = Integer.parseInt(op, 2);
        
        if (test.equals("10000000000000000000000000000000")) {
            return "NOP";
        }
        switch (opInt) {
            case ZERO_OP:
                opName = determineSpecial(test);
                break;
            case ADDI_OP:
                opName = "ADDI";
                break;
            case BEQ_OP:
                opName = "BEQ";
                break;
            case BLTZ_OP:
                opName = "BLTZ";
                break;
            case J_OP:
                opName = "J";
                break;
            case LW_OP:
                opName = "LW";
                break;
            case MUL_OP:
                opName = "MUL";
                break;
            case SW_OP:
                opName = "SW";
                break;
            default:
                opName = "Not a valid instruction";
                break;
        }
        return opName;

    }
    
    //distinguises between instructions with 00000 for opcode
    public static String determineSpecial(String test) {
        String specName;
        String spec = test.substring(26, 32);
        int specInt = Integer.parseInt(spec, 2);
        
        switch (specInt) {
            case ADD_SPEC: 
                specName = "ADD";
                break;
            case AND_SPEC:
                specName = "AND";
                break;
            case JR_SPEC:
                specName = "JR";
                break;
            case MOVZ_SPEC:
                specName = "MOVZ";
                break;
            case OR_SPEC:
                specName = "OR";
                break;
            case SLL_SPEC:
                specName = "SLL";
                break;
            case SRL_SPEC:
                specName = "SRL";
                break;
            case SUB_SPEC:
                specName = "SUB";
                break;
            case BREAK_SPEC:
                specName = "BREAK";
                break;
            default:
                specName = "Not a valid instruction";
                break;
        }
        
        return specName;
    }
    
    //converts to int using two's complement
    //Showed in class on Stack Overflow but we came up with this a week before on our own >:)
    public static long determineTwos(String test) {
        int signBit = Integer.parseInt(test.substring(0,1), 2);
        long twosLong;
        if (signBit == 1) {
            String newTest = "0";
            for (int i = 1; i < test.length(); ++i) {
                if (test.charAt(i) == '1') {
                    newTest += "0";
                }
                else {
                    newTest += "1";
                }
            }
            test = newTest;
        }
        
        twosLong = Long.parseLong(test.substring(1, test.length()), 2);
        if (signBit == 1) {
            twosLong += 1;
            twosLong *= -1;
        }
        
        return twosLong;
    }
    
    //returns small string of registers and data being used for instruction
    //coined "The Brick"
    public static String getInstString(String test) {
        String opName = determineOP(test);
        String instString;
        switch (opName) {
            case "ADD":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "AND":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "MOVZ":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "MUL":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "OR":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "SUB":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
                break;
            case "JR":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString();
                break;
            case "ADDI":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32))).toString();
                break;
            case "BEQ":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32))).toString(); //easter egg B)
                break;
            case "BLTZ":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32) + "00")).toString();
                break;
            case "J":
                instString = "#" + ((Long)determineTwos(test.substring(6, 32) + "00")).toString();
                break;
            case "LW":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    ((Long)determineTwos(test.substring(16, 32))).toString() + "(R" +
                    ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ")";
                break;
            case "SW":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    ((Long)determineTwos(test.substring(16, 32))).toString() + "(R" +
                    ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ")";
                break;
            case "SLL":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " + 
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " + "#" + 
                    ((Integer)(Integer.parseInt(test.substring(21, 26), 2))).toString();
                break;
            case "SRL":
                instString = "R" + ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " + 
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " + "#" + 
                    ((Integer)(Integer.parseInt(test.substring(21, 26), 2))).toString();
                break;
            default:
                instString = "";
                break;
            //yeah we should probably create another method so this isnt a paragraph.    Too Bad!!
        }
        
        return instString;
    }
    
    //adds instruction to binary array
    public static String[] addToBinary(int x, String[] binary) {
        String[] newBinary = new String[binary.length + 1];
        int dif;
        String newString = "";
        for (int i = 0; i < binary.length; ++i) {
            newBinary[i] = binary[i];
        }
        dif = 32 - (Integer.toBinaryString(x)).length();
        for (int i = 0; i < dif; ++i) {
            newString += "0";
        }
        newString += Integer.toBinaryString(x);
        newBinary[newBinary.length - 1] = newString;
        
        return newBinary;
    }
    
    //adds data to data array
    public static int[] addToData(int[] data, long val) {
        int[] newData = new int[data.length + 1];
        for (int i = 0; i < data.length; ++i) {
            newData[i] = data[i];
        }
        newData[newData.length - 1] = (int)val;

        return newData;
       
    }

} //no way you read all of this code dont lie
