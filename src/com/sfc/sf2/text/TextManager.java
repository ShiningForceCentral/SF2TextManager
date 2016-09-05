/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text;

import com.sfc.sf2.text.compression.Symbols;
import com.sfc.sf2.text.compression.HuffmanTreeNode;
import com.sfc.sf2.text.compression.TextEncoder;
import com.sfc.sf2.text.io.CsvManager;
import com.sfc.sf2.text.io.DisassemblyManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class TextManager {
    
    private static String[] gamescript;
       
    
    public static void importDisassembly(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Importing disassembly ...");
        TextManager.gamescript = DisassemblyManager.importDisassembly(huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Disassembly imported.");
    }
    
    public static void exportDisassembly(String huffmanTreeOffsetsFilePath, String huffmanTreesFilePath, String firstTextbankFilePath){
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Exporting disassembly ...");
        DisassemblyManager.exportDisassembly(gamescript, huffmanTreeOffsetsFilePath,huffmanTreesFilePath,firstTextbankFilePath);
        System.out.println("com.sfc.sf2.text.BusinessLayer.importDisassembly() - Disassembly exported.");        
    }     
    
    public static void importCsv(String filepath){
        System.out.println("com.sfc.sf2.text.BusinessLayer.importCsv() - Importing CSV ...");
        gamescript = CsvManager.importCsv(filepath);
        System.out.println("com.sfc.sf2.text.BusinessLayer.importCsv() - CSV imported.");
    }
    
    public static void exportCsv(String filepath){
        System.out.println("com.sfc.sf2.text.BusinessLayer.exportCsv() - Exporting CSV ...");
        CsvManager.exportCsv(gamescript, filepath);
        System.out.println("com.sfc.sf2.text.BusinessLayer.exportCsv() - CSV exported.");       
    }
    
    
}
