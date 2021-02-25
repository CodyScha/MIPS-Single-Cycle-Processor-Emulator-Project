/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


public class Test {
    
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
    
    public static void main(String[] args) {
        String opCode;
        Scanner fin = null;
        try {
            fin = new Scanner(new File("test1_bin.txt"));
        } catch (FileNotFoundException ex) {
            System.exit(1);
        }
        
        String testInst = "10000000000000000000000000000000";
        
        while (fin.hasNext()) {
            testInst = fin.nextLine();
            if (isValid(testInst)) {
                opCode = determineOP(testInst);
                System.out.println(opCode);
            }
            else {
                System.out.println("Invalid Instruction");
            }
        }
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

}