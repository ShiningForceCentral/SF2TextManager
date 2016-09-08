/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text;

import com.sfc.sf2.text.io.CsvManager;
import com.sfc.sf2.text.io.DisassemblyManager;
import com.sfc.sf2.text.io.RomManager;

/**
 *
 * @author wiz
 */
public class TextManager {
    
    private static String[] gamescript;
       
    
    public static void importDisassembly(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.importDisassembly() - Importing disassembly ...");
        TextManager.gamescript = DisassemblyManager.importDisassembly(huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.importDisassembly() - Disassembly imported.");
    }
    
    public static void exportDisassembly(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.importDisassembly() - Exporting disassembly ...");
        DisassemblyManager.exportDisassembly(gamescript, huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.importDisassembly() - Disassembly exported.");        
    }   
    
    public static void importOriginalRom(String originalRomFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.importOriginalRom() - Importing original ROM ...");
        TextManager.gamescript = RomManager.importRom(RomManager.ORIGINAL_ROM_TYPE,originalRomFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.importOriginalRom() - Original ROM imported.");
    }
    
    public static void exportOriginalRom(String originalRomFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.exportOriginalRom() - Exporting original ROM ...");
        RomManager.exportRom(RomManager.ORIGINAL_ROM_TYPE, TextManager.gamescript, originalRomFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.exportOriginalRom() - Original ROM exported.");        
    }   
    
    public static void importCaravanRom(String caravanRomFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.importCaravanRom() - Importing Caravan ROM ...");
        TextManager.gamescript = RomManager.importRom(RomManager.CARAVAN_ROM_TYPE,caravanRomFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.importCaravanRom() - Original ROM imported.");
    }
    
    public static void exportCaravanRom(String caravanRomFilePath){
        System.out.println("com.sfc.sf2.text.TextManager.exportCaravanRom() - Exporting original ROM ...");
        RomManager.exportRom(RomManager.CARAVAN_ROM_TYPE, TextManager.gamescript, caravanRomFilePath);
        System.out.println("com.sfc.sf2.text.TextManager.exportCaravanRom() - Caravan ROM exported.");        
    }    
    
    public static void importCsv(String filepath){
        System.out.println("com.sfc.sf2.text.TextManager.importCsv() - Importing CSV ...");
        gamescript = CsvManager.importCsv(filepath);
        System.out.println("com.sfc.sf2.text.TextManager.importCsv() - CSV imported.");
    }
    
    public static void exportCsv(String filepath){
        System.out.println("com.sfc.sf2.text.TextManager.exportCsv() - Exporting CSV ...");
        CsvManager.exportCsv(gamescript, filepath);
        System.out.println("com.sfc.sf2.text.TextManager.exportCsv() - CSV exported.");       
    }
    
    
}
