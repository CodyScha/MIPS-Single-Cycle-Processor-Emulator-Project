/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;

public class Test {
    
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
        
        File file = new File("test1.bin");
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

        int PC = 96;
        String testInst;
        boolean isBreak = false;
        int cycleNum = 1;
        
        for (int i = 0; i < binary.length; ++i) {
            testInst = binary[i];
            isBreak = printDis(testInst, PC, isBreak);
            PC += 4;
        }
        isBreak = false;
        PC = 96;
        for (int i = 0; i < binary.length; ++i) {
            testInst = binary[i];
            if (isValid(testInst)) {
                isBreak = printSim(testInst, PC, isBreak, cycleNum, foutSim);
                cycleNum++;
            }
            PC += 4;
        }
        
        foutSim.close();
        foutDis.close();
    }
    public static boolean printDis(String testInst, int PC, boolean isBreak) {
        long twosLong;
        String formatTest;
        String opCode;
        
        if (!isBreak) {
                formatTest = testInst.substring(0, 1) + " " + testInst.substring(1, 6) + " " + testInst.substring(6, 11) + " " 
                             + testInst.substring(11, 16) + " " + testInst.substring(16, 21) + " " + testInst.substring(21, 26)
                             + " " + testInst.substring(26, 32);
                System.out.printf("%-40s%-8d", formatTest, PC);
                if (isValid(testInst)) {
                    opCode = determineOP(testInst);
                    System.out.printf("%-8s", opCode);
                    System.out.print(getInstString(testInst));
                    System.out.println();
                    if (opCode.equals("BREAK")) {
                        isBreak = true;
                    }
                }
                else {
                    System.out.println("Invalid Instruction");
                }
            }
            else {
                twosLong = determineTwos(testInst);
                System.out.printf("%-40s%-8d", testInst, PC);
                System.out.print(twosLong);
                System.out.println();
                data = addToData(data, twosLong);
            }
        
        return isBreak;
    }
    
    public static boolean printSim(String test, int PC, boolean isBreak, int cycle, PrintWriter fout) {
        int i;
        String opCode = determineOP(test);
        
        if (!isBreak) {
            fout.println("====================");
            fout.println("cycle:" + cycle + "\t" + PC + "\t" + determineOP(test) + "\t" + getInstString(test));
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
            fout.print("172:");
            for (i = 0; i < 8; ++i) {
                if (i <= data.length) {
                    fout.print("\t" + data[i]);
                }
            }
            fout.print("\n204:");
            for (i = 8; i < 16; ++i) {
                if (i <= data.length) {
                    fout.print("\t" + data[i]);
                }
            }
            fout.print("\n236:");
            for (i = 16; i < 24; ++i) {
                if (i <= data.length) {
                    fout.print("\t" + data[i]);
                }
            }
            fout.println("\n");
        }
        if (opCode.equals("BREAK")) {
            isBreak = true;
        }
        
        return isBreak;
    }
     
    public static boolean isValid(String test) {
        boolean isValid;
        
        isValid = (test.charAt(0) != '0');
        
        return isValid;
    }
    
    public static String determineOP(String test) {
        String opName;
        String op = test.substring(1, 6);
        int opInt = Integer.parseInt(op, 2);
        
        if (test.equals("10000000000000000000000000000000")) {
            return "NOP";
        }
        opName = switch (opInt) {
            case ZERO_OP -> determineSpecial(test);
            case ADDI_OP -> "ADDI";
            case BEQ_OP -> "BEQ";
            case BLTZ_OP -> "BLTZ";
            case J_OP -> "J";
            case LW_OP -> "LW";
            case MUL_OP -> "MUL";
            case SW_OP -> "SW";
            default -> "Not a valid instruction";
        };
        return opName;

    }
    
    public static String determineSpecial(String test) {
        String specName;
        String spec = test.substring(26, 32);
        int specInt = Integer.parseInt(spec, 2);
        
        specName = switch (specInt) {
            case ADD_SPEC -> "ADD";
            case AND_SPEC -> "AND";
            case JR_SPEC -> "JR";
            case MOVZ_SPEC -> "MOVZ";
            case OR_SPEC -> "OR";
            case SLL_SPEC -> "SLL";
            case SRL_SPEC -> "SRL";
            case SUB_SPEC -> "SUB";
            case BREAK_SPEC -> "BREAK";
            default -> "Not a valid instruction";
        };
        
        return specName;
    }
    
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
    
    public static String getInstString(String test) {
        String opName = determineOP(test);
        String instString;
        instString = switch (opName) {
            case "ADD", "AND", "MOVZ", "MUL", "OR", "SLL", "SRL", "SUB" -> "R" + 
                    ((Integer)(Integer.parseInt(test.substring(16, 21), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString();
            case "JR" -> "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString();
            case "ADDI" -> "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32))).toString();
            case "BEQ" -> "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32))).toString();
            case "BLTZ" -> "R" + ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ", " +
                    "#" + ((Long)determineTwos(test.substring(16, 32) + "00")).toString();
            case "J" -> "#" + ((Long)determineTwos(test.substring(6, 32) + "00")).toString();
            case "LW", "SW" -> "R" + ((Integer)(Integer.parseInt(test.substring(11, 16), 2))).toString() + ", " +
                    ((Long)determineTwos(test.substring(16, 32))).toString() + "(R" +
                    ((Integer)(Integer.parseInt(test.substring(6, 11), 2))).toString() + ")";
            default ->   "";
            
        };
        
        return instString;
    }
    
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
    
    public static int[] addToData(int[] data, long val) {
        int[] newData = new int[data.length + 1];
        for (int i = 0; i < data.length; ++i) {
            newData[i] = data[i];
        }
        newData[newData.length - 1] = (int)val;
        
        return newData;
    }

}