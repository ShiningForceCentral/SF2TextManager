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
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class AsmManager {
    
    public static final String HUFFMANTREEOFFSETS_FILENAME = "huffmantreeoffsets.bin";
    public static final String HUFFMANTREES_FILENAME = "huffmantrees.bin";
    public static final String TEXTBANK_FILENAME = "textbankXX.bin";  
    
    public static String[] importAsm(String path, String[] gamescript){
        System.out.println("com.sfc.sf2.text.io.AsmManager.importAsm() - Importing ASM ...");
        String[] script = gamescript;
        int textCursor=0;
        int baseIndex=0;
        int scanLineNumber = 0;
        try{
            File file = new File(path);
            Scanner scan = new Scanner(file);
            while(scan.hasNext()){
                String line = scan.nextLine();
                scanLineNumber++;
                try{
                    line = removeName(line).trim(); 
                    if(line.trim().startsWith("txt")&&line.contains(";")&&line.contains("\"")){
                        String indexString = line.substring(3, line.indexOf(';')).trim();
                        String textLine = line.substring(line.indexOf("\"")+1,line.lastIndexOf("\""));
                        int index = 0;
                        if(indexString.startsWith("$")){
                            index = Integer.parseInt(indexString.substring(1), 16);
                        }else{
                            index = Integer.parseInt(indexString);
                        }
                        updateLine(textLine, index, script);
                    }else if(line.trim().startsWith("textCursor")){
                        String indexString = line.trim().substring(line.trim().indexOf(" ")).trim();
                        int index = 0;
                        if(indexString.startsWith("$")){
                            index = Integer.parseInt(indexString.substring(1), 16);
                        }else{
                            index = Integer.parseInt(indexString);
                        }
                        textCursor = index;
                        System.out.println("textCursor="+textCursor);
                    }else if(
                            (
                            line.trim().startsWith("nextSingleText")
                            ||line.trim().startsWith("nextText")
                            )
                            &&line.contains(";")&&line.contains("\"")){
                        String textLine = line.substring(line.indexOf("\"")+1,line.lastIndexOf("\""));
                        updateLine(textLine, textCursor, script);
                        textCursor++;
                    }else if(
                            (
                            line.trim().startsWith("nextSingleTextVar")
                            ||line.trim().startsWith("nextTextVar")
                            )
                            &&line.contains(";")&&line.contains("\"")){
                        String textLine = line.substring(line.indexOf("\"")+1,line.lastIndexOf("\""));
                        updateLine(textLine, textCursor, script);
                        textCursor++;
                    }
                }catch(Exception e){
                    System.err.println("com.sfc.sf2.text.io.AsmManager.importAsm() - Error while parsing line at index "+scanLineNumber+" : "+e);
                    e.printStackTrace();
                }
            }          
        }catch(Exception e){
             System.err.println("com.sfc.sf2.text.io.AsmManager.importAsm() - Error while parsing line at index "+scanLineNumber+" : "+e);
             e.printStackTrace();
        }    
        System.out.println("com.sfc.sf2.text.io.AsmManager.importAsm() - ASM imported.");
        return gamescript;
    }

    
    private static String[] updateLine(String line, int index, String[] script){
        if(index<script.length){
            script[index] = line;
        }else{
            String[] newScript = new String[index+1];
            System.arraycopy(script, 0, newScript, 0, script.length);
            newScript[index] = line;
            script = newScript;
        }   
        System.out.println(getLineIndexString(index)+"="+line);
        return script;
    }
    
    
    private static String getLineIndexString(int index){
        String indexString = Integer.toHexString(index);
        while(indexString.length()<4){
            indexString="0"+indexString;
        }
        return indexString;
    }
    
    private static String removeName(String line){
        if(line.indexOf(":")>=0 && 
                (
                line.indexOf(":")<line.indexOf(" ")
                || line.indexOf(":")<line.indexOf("\t")
                || line.indexOf(":")<line.indexOf(";")
                )
                    ){
            line = line.substring(line.indexOf(":")+1);
        }
        return line;
    }
    
}
