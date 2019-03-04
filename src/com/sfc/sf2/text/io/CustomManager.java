/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.io;

import com.sfc.sf2.text.TextManager;
import com.sfc.sf2.text.compression.TextDecoder;
import com.sfc.sf2.text.compression.TextEncoder;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class CustomManager {
    
    private static final int LINES_PER_BANK = 256;
    private static final int TEXTBANK_LOADING_BUFFER_SIZE = 32768;
    
    private static File romFile;  
    private static byte[] romData;
    
    public static String[] importRom(String romFilePath, int huffmanTreeOffsetsBegin, int huffmanTreeOffsetsEnd, int huffmanTreesOffsetsBegin, int huffmanTreesOffsetsEnd, int textbanksOffsetsPointerOffset, int lastLineIndex){
        System.out.println("com.sfc.sf2.text.io.CustomManager.importRom() - Importing ROM ...");
        CustomManager.openFile(romFilePath);
        CustomManager.parseOffsets(huffmanTreeOffsetsBegin, huffmanTreeOffsetsEnd);
        CustomManager.parseTrees(huffmanTreesOffsetsBegin, huffmanTreesOffsetsEnd);
        String[] gamescript = CustomManager.parseAllTextbanks(textbanksOffsetsPointerOffset, lastLineIndex);        
        System.out.println("com.sfc.sf2.text.io.CustomManager.importRom() - ROM imported.");
        return gamescript;
    }
    
    public static void exportRom(String[] gamescript, String romFilePath, int huffmanTreeOffsetsOffset, int huffmanTreesOffset, int textbanksPointerOffset, int textbanksOffset){
        System.out.println("com.sfc.sf2.text.io.CustomManager.exportRom() - Exporting ROM ...");
        CustomManager.produceTrees(gamescript);
        CustomManager.produceTextbanks(gamescript);
        CustomManager.writeFile(romFilePath, huffmanTreeOffsetsOffset, huffmanTreesOffset, textbanksPointerOffset, textbanksOffset);
        System.out.println("com.sfc.sf2.text.io.CustomManager.exportRom() - ROM exported.");        
    }    
    
    private static void openFile(String romFilePath){
        try {
            System.out.println("com.sfc.sf2.text.io.CustomManager.openFiles() - ROM file path : " + romFilePath);
            romFile = new File(romFilePath);
            romData = Files.readAllBytes(Paths.get(romFile.getAbsolutePath()));
            System.out.println("com.sfc.sf2.text.io.CustomManager.openFiles() - File opened.");
        } catch (IOException ex) {
            Logger.getLogger(CustomManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void parseOffsets(int huffmanTreeOffsetsBegin, int huffmanTreeOffsetsEnd){
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseOffsets() - Parsing offsets ...");
        byte[] data = Arrays.copyOfRange(romData,huffmanTreeOffsetsBegin,huffmanTreeOffsetsEnd);
        TextDecoder.parseOffsets(data);
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseOffsets() - Offsets parsed.");
    }
    
    private static void parseTrees(int huffmanTreesOffsetsBegin, int huffmanTreesOffsetsEnd){
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseTrees() - Parsing trees ...");
        byte[] data = Arrays.copyOfRange(romData,huffmanTreesOffsetsBegin,huffmanTreesOffsetsEnd);
        TextDecoder.parseTrees(data);
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseTrees() - Trees parsed.");
    }
    
    private static String[] parseAllTextbanks(int textbanksOffsetsPointerOffset, int lastLineIndex){
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseTextbank() - Parsing textbank ...");
        String[] gamescript = new String[0]; 
        byte[] textbanksOffsetsPointerBytes = Arrays.copyOfRange(romData,textbanksOffsetsPointerOffset,textbanksOffsetsPointerOffset+4);
        int textbanksOffsetsPointer = bytesToInt(textbanksOffsetsPointerBytes);
        int numberOfTextbanks = (lastLineIndex+1 + LINES_PER_BANK-1) / LINES_PER_BANK;
        int lastTextbankLines = ((lastLineIndex+1) % LINES_PER_BANK);
        for(int i=0;i<numberOfTextbanks;i++){ 
            String index = Integer.toString(i);
            while(index.length()<2){
                index = "0"+index;
            }
            int linesToParse = (i==numberOfTextbanks-1)? lastTextbankLines : LINES_PER_BANK;
            int textbankBegin = bytesToInt(Arrays.copyOfRange(romData,textbanksOffsetsPointer+(4*i),textbanksOffsetsPointer+(4*i)+4));
            byte[] data = Arrays.copyOfRange(romData,textbankBegin,textbankBegin+TEXTBANK_LOADING_BUFFER_SIZE); 
            String[] textbankStrings = TextDecoder.parseTextbank(data, i, linesToParse);
            String[] workingStringArray = Arrays.copyOf(gamescript, gamescript.length + textbankStrings.length);
            System.arraycopy(textbankStrings, 0, workingStringArray, gamescript.length, textbankStrings.length);
            gamescript = workingStringArray;
        }
        System.out.println("com.sfc.sf2.text.io.CustomManager.parseTextbank() - Textbanks all parsed.");
        return gamescript;
    }
    
    private static void produceTrees(String[] gamescript) {
        System.out.println("com.sfc.sf2.text.io.CustomManager.produceTrees() - Producing trees ...");
        TextEncoder.produceTrees(gamescript);
        System.out.println("com.sfc.sf2.text.io.CustomManager.produceTrees() - Trees produced.");
    }

    private static void produceTextbanks(String[] gamescript) {
        System.out.println("com.sfc.sf2.text.io.CustomManager.produceTextbanks() - Producing text banks ...");
        TextEncoder.produceTextbanks(gamescript);
        System.out.println("com.sfc.sf2.text.io.CustomManager.produceTextbanks() - Text banks produced.");
    }    
  
    private static void writeFile(String romFilePath, int huffmanTreeOffsetsOffset, int huffmanTreesOffset, int textbanksPointerOffset, int textbanksOffset){

        try {
            System.out.println("com.sfc.sf2.text.io.CustomManager.writeFiles() - Writing file ...");

            romFile = new File(romFilePath);
            Path romPath = Paths.get(romFile.getAbsolutePath());
            romData = Files.readAllBytes(romPath);
            byte[] newHuffmantreeOffsetsFileBytes = TextEncoder.getNewHuffmantreeOffsetsFileBytes();
            System.arraycopy(newHuffmantreeOffsetsFileBytes, 0, romData, huffmanTreeOffsetsOffset, newHuffmantreeOffsetsFileBytes.length);
            System.out.println("Huffman tree offsets : "+newHuffmantreeOffsetsFileBytes.length 
                    + " bytes written at offset 0x" + Integer.toHexString(huffmanTreeOffsetsOffset).toUpperCase()
                    + "..0x" + Integer.toHexString(huffmanTreeOffsetsOffset+newHuffmantreeOffsetsFileBytes.length).toUpperCase()); 
            byte[] newHuffmanTreesFileBytes = TextEncoder.getNewHuffmanTreesFileBytes();
            System.arraycopy(newHuffmanTreesFileBytes, 0, romData, huffmanTreesOffset, newHuffmanTreesFileBytes.length);
            System.out.println("Huffman trees : "+newHuffmanTreesFileBytes.length 
                    + " bytes written at offset 0x" + Integer.toHexString(huffmanTreesOffset).toUpperCase()
                    + "..0x" + Integer.toHexString(huffmanTreesOffset+newHuffmanTreesFileBytes.length).toUpperCase());           
           
            byte[][] newTextbanks = TextEncoder.getNewTextbanks();
            int[] textbankOffsets = new int[newTextbanks.length];
            int textbankCursor = textbanksOffset;
            for(int i=0;i<newTextbanks.length;i++){
                String index = String.valueOf(i);
                while(index.length()<2){
                    index = "0"+index;
                }
                System.arraycopy(newTextbanks[i], 0, romData, textbankCursor, newTextbanks[i].length);
                System.out.println("Textbank "+index+" : "+newTextbanks[i].length 
                        + " bytes written at offset 0x" + Integer.toHexString(textbankCursor).toUpperCase()
                        + "..0x" + Integer.toHexString(textbankCursor+newTextbanks[i].length).toUpperCase());
                textbankOffsets[i] = textbankCursor;
                textbankCursor+=newTextbanks[i].length;
            }
            if(textbankCursor%2>0){
                textbankCursor++; /* align on even address */
            }
            int textbanksPointerValue = textbankCursor;
            byte[] pointerBytes = intToFourByteArray(textbanksPointerValue);
            System.arraycopy(pointerBytes, 0, romData, textbanksPointerOffset, 4);
            for(int i=0;i<textbankOffsets.length;i++){
                System.arraycopy(intToFourByteArray(textbankOffsets[i]), 0, romData, textbankCursor, 4);
                textbankCursor+=4;
            }
            System.out.println("Textbank offsets : "+textbankOffsets.length*4 
                    + " bytes written at offset 0x" + Integer.toHexString(textbanksPointerValue).toUpperCase()
                    + "..0x" + Integer.toHexString(textbanksPointerValue+textbankOffsets.length*4).toUpperCase());
            
            Files.write(romPath,romData);
            System.out.println(romData.length + " bytes into " + romFilePath);  
            System.out.println("com.sfc.sf2.text.io.CustomManager.writeFiles() - File written.");
        } catch (IOException ex) {
            Logger.getLogger(TextManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
    private static int bytesToInt(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }
    
    public static  byte[] intToFourByteArray(int value){
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
    }
    
}
