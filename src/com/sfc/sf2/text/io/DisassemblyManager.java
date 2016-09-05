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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class DisassemblyManager {
    
    private static File huffmanTreeOffsetsFile;
    private static File huffmanTreesFile;
    private static File textbankFile;   
    
    public static String[] importDisassembly(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.importDisassembl() - Importing disassembly ...");
        DisassemblyManager.openFiles(huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        DisassemblyManager.parseOffsets();
        DisassemblyManager.parseTrees();
        String[] gamescript = DisassemblyManager.parseAllTextbanks();        
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.importDisassembl() - Disassembly imported.");
        return gamescript;
    }
    
    public static void exportDisassembly(String[] gamescript, String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Exporting disassembly ...");
        DisassemblyManager.produceTrees(gamescript);
        DisassemblyManager.produceTextbanks(gamescript);
        DisassemblyManager.writeFiles(huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Disassembly exported.");        
    }    
    
    private static String openFiles(String huffmanTreeOffsetsFilepath, String huffmanTreesFilepath, String textbankFilepath){
        System.out.println("sfc.segahr.BusinessLayer.openFiles() - Filepaths :"
                + "\nHuffman tree offsets : " + huffmanTreeOffsetsFilepath
                + "\nHuffman trees : " + huffmanTreesFilepath
                + "\nTextbank : " + textbankFilepath);
        huffmanTreeOffsetsFile = new File(huffmanTreeOffsetsFilepath);
        huffmanTreesFile = new File(huffmanTreesFilepath);
        textbankFile = new File(textbankFilepath);
        System.out.println("sfc.segahr.BusinessLayer.openFiles() - Files opened.");
        return textbankFile.getParent();
    }
    
    private static void parseOffsets(){
        try{
            System.out.println("sfc.segahr.BusinessLayer.parseOffsets() - Parsing offsets ...");
            Path path = Paths.get(huffmanTreeOffsetsFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);
            TextDecoder.parseOffsets(data);
            System.out.println("sfc.segahr.BusinessLayer.parseOffsets() - Offsets parsed.");
        } catch(IOException e){
            System.err.println("sfc.segahr.BusinessLayer.parseOffsets() - Error while parsing huffmanTreeOffsetsFile data : "+e);
        }
    }
    
    private static void parseTrees(){
        try{
            System.out.println("sfc.segahr.BusinessLayer.parseTrees() - Parsing trees ...");
            Path path = Paths.get(huffmanTreesFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);
            TextDecoder.parseTrees(data);
            System.out.println("sfc.segahr.BusinessLayer.parseTrees() - Trees parsed.");            
        } catch(IOException e){
            System.err.println("sfc.segahr.BusinessLayer.parseTrees() - Error while parsing huffmanTreesFile data : "+e);
        }
    }
    
    private static String[] parseAllTextbanks(){
        System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsing textbank ...");
        String[] gamescript = new String[0];        
        try{
            for(int i=0;i<100;i++){
                String index = Integer.toString(i);
                while(index.length()<2){
                    index = "0"+index;
                }
                Path path = Paths.get(textbankFile.getParent()+"\\textbank"+index+".bin");
                byte[] data = Files.readAllBytes(path); 
                String[] textbankStrings = TextDecoder.parseTextbank(data, i);
                String[] workingStringArray = Arrays.copyOf(gamescript, gamescript.length + textbankStrings.length);
                System.arraycopy(textbankStrings, 0, workingStringArray, gamescript.length, textbankStrings.length);
                gamescript = workingStringArray;
            }
        }catch(IOException e){
            System.out.println("No more textbank files to parse.");
        }catch(Exception e){
             System.err.println("sfc.segahr.BusinessLayer.parseTextbank() - Error while parsing textbankFile data : "+e);
        } 
        System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Textbanks all parsed.");
        return gamescript;
    }
    
    public static void produceTrees(String[] gamescript) {
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.produceTrees() - Producing trees ...");
        TextEncoder.produceTrees(gamescript);
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.produceTrees() - Trees produced.");
    }

    public static void produceTextbanks(String[] gamescript) {
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.produceTextbanks() - Producing text banks ...");
        TextEncoder.produceTextbanks(gamescript);
        System.out.println("com.sfc.sf2.text.io.DisassemblyManager.produceTextbanks() - Text banks produced.");
    }    
  
    private static void writeFiles(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        try {
            System.out.println("com.sfc.sf2.text.BusinessLayer.writeFiles() - Writing files ...");
            Date d = new Date();
            DateFormat df = new SimpleDateFormat("YYMMddHHmmss");
            String dateString = df.format(d);
            Path offsetsFilePath = Paths.get(huffmanTreeOffsetsFilePath);
            Path treesFilePath = Paths.get(huffmanTreesFilePath);
            Path parentPath = Paths.get(firstTextbankFilePath).getParent();
            byte[] newHuffmantreeOffsetsFileBytes = TextEncoder.getNewHuffmantreeOffsetsFileBytes();
            byte[] newHuffmanTreesFileBytes = TextEncoder.getNewHuffmanTreesFileBytes();
            byte[][] newTextbanks = TextEncoder.getNewTextbanks();
            Files.write(offsetsFilePath,newHuffmantreeOffsetsFileBytes);
            System.out.println(newHuffmantreeOffsetsFileBytes.length + " bytes into " + offsetsFilePath);
            Files.write(treesFilePath, newHuffmanTreesFileBytes);
            System.out.println(newHuffmanTreesFileBytes.length + " bytes into " + treesFilePath);
            for(int i=0;i<newTextbanks.length;i++){
                String index = String.valueOf(i);
                while(index.length()<2){
                    index = "0"+index;
                }
                Path textbankFilePath = Paths.get(parentPath+"\\textbank"+index+"-"+dateString+".bin");
                Files.write(textbankFilePath, newTextbanks[i]);
                System.out.println(newTextbanks[i].length + " bytes into " + textbankFilePath);
            }
            System.out.println("com.sfc.sf2.text.BusinessLayer.writeFiles() - Files written.");
        } catch (IOException ex) {
            Logger.getLogger(TextManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    
}
