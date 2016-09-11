/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.io;

import com.sfc.sf2.text.TextManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class TxtManager {
    
    public static String[] importTxt(String filepath){
        String[] gamescript = new String[0];
        try {
            System.out.println("com.sfc.sf2.text.io.CsvManager.importTxt() - Importing TXT ...");
            Path path = Paths.get(filepath);
            List<String> lines = Files.readAllLines(path, Charset.forName(System.getProperty("file.encoding")));
            gamescript = new String[lines.size()];
            int i = 0;
            for(String line : lines){
                int semiColonIndex = line.indexOf("=");
                if(semiColonIndex!=-1){
                    String lineWithoutIndex = line.substring(semiColonIndex+1);
                    gamescript[i] = lineWithoutIndex;
                    System.out.println("Line "+i+" : "+lineWithoutIndex);
                }
                i++;
            }
            System.out.println("com.sfc.sf2.text.io.CsvManager.importTxt() - TXT imported.");
        } catch (IOException ex) {
            Logger.getLogger(TextManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gamescript;                
    }
    
    public static void exportTxt(String[] gamescript, String filepath){
        try {
            System.out.println("com.sfc.sf2.text.io.CsvManager.exportTxt() - Exporting TXT ...");
            Path path = Paths.get(filepath);
            PrintWriter pw;
            pw = new PrintWriter(path.toString(),System.getProperty("file.encoding"));
            int i = 0;
            for(String line : gamescript){
                String hexIndex = Integer.toHexString(i).toUpperCase();
                while(hexIndex.length()<4){
                    hexIndex = "0" + hexIndex;
                }
                line = hexIndex+"="+line;
                if(i+1!=gamescript.length){
                    pw.println(line);
                }else{
                    pw.print(line);
                }
                i++;
                System.out.println("Line "+i+" : "+line);
            }
            pw.close();
            System.out.println("com.sfc.sf2.text.io.CsvManager.exportTxt() - TXT exported.");
        } catch (IOException ex) {
            Logger.getLogger(TextManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
                
    }        
    
}
