/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfc.segahr;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author wiz
 */
public class BusinessLayer {
    
    private static File huffmanTreeOffsetsFile;
    private static File huffmanTreesFile;
    private static File textbankFile;
    
    private static short[] huffmanTreeOffsets;
    private static HuffmanTree[] huffmanTrees;
    private static String[] textbankStrings;
    
    private static byte PREVIOUS_SYMBOL;
    
    public static void openFiles(String huffmanTreeOffsetsFilepath, String huffmanTreesFilepath, String textbankFilepath){
        System.out.println("sfc.segahr.BusinessLayer.openFiles() - Filepaths :"
                + "\nHuffman tree offsets : " + huffmanTreeOffsetsFilepath
                + "\nHuffman trees : " + huffmanTreesFilepath
                + "\nTextbank : " + textbankFilepath);
        huffmanTreeOffsetsFile = new File(huffmanTreeOffsetsFilepath);
        huffmanTreesFile = new File(huffmanTreesFilepath);
        textbankFile = new File(textbankFilepath);
        System.out.println("sfc.segahr.BusinessLayer.openFiles() - Files opened.");
    }
    
    public static void parseOffsets(){
        try{
            System.out.println("sfc.segahr.BusinessLayer.parseOffsets() - Parsing offsets ...");
            Path path = Paths.get(huffmanTreeOffsetsFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);
            huffmanTreeOffsets = new short[data.length/2];
            ByteBuffer wrappedData = ByteBuffer.wrap(data);
            wrappedData.asShortBuffer().get(huffmanTreeOffsets);
            System.out.println("sfc.segahr.BusinessLayer.parseOffsets() - huffmanTreeOffsets : "
                    + "\n" + Arrays.toString(huffmanTreeOffsets));
            System.out.println("sfc.segahr.BusinessLayer.parseOffsets() - Offsets parsed.");
        } catch(IOException e){
            System.err.println("sfc.segahr.BusinessLayer.parseOffsets() - Error while parsing huffmanTreeOffsetsFile data : "+e);
        }
        
    }
    
    public static void parseTrees(){
        try{
            System.out.println("sfc.segahr.BusinessLayer.parseTrees() - Parsing trees ...");
            Path path = Paths.get(huffmanTreesFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);
            huffmanTrees = new HuffmanTree[huffmanTreeOffsets.length];
            for(int i = 0;i<huffmanTreeOffsets.length;i++){
                if(huffmanTreeOffsets[i]==-1){
                    huffmanTrees[i] = null;
                }else{
                    huffmanTrees[i] = new HuffmanTree(i,data,huffmanTreeOffsets[i]);
                }
            }
            logTrees();
            System.out.println("sfc.segahr.BusinessLayer.parseTrees() - Trees parsed.");            
        } catch(IOException e){
            System.err.println("sfc.segahr.BusinessLayer.parseTrees() - Error while parsing huffmanTreesFile data : "+e);
        }
    }
    
    private static void logTrees(){
        System.out.println("sfc.segahr.BusinessLayer.logTrees() - Parsed trees :");
        for(int i = 0;i<huffmanTrees.length;i++){
            if(huffmanTrees[i] != null){
                System.out.println(huffmanTrees[i].toString());
            }else{
                System.out.println("No tree for index " + i);
            }
        }
    }
    
    public static void parseTextbank(){
       try{
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsing textbank ...");
            Path path = Paths.get(textbankFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path); 
            short offset = 1;
            String s = parseString(data,offset);
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsed String :"
                    + "\n" + s);
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Textbank parsed.");
        } catch(IOException e){
            System.err.println("sfc.segahr.BusinessLayer.parseTextbank() - Error while parsing textbankFile data : "+e);
        }            
    }
    
    private static String parseString(byte[] data, short offset){
        StringBuilder sb = new StringBuilder();
        PREVIOUS_SYMBOL = (byte)0xFE;
        HuffmanTree.STRING_BYTE_COUNTER = 0;
        HuffmanTree.STRING_BIT_COUNTER = 0;
        while(true){
            byte symbol = huffmanTrees[(int)PREVIOUS_SYMBOL&0xFF].parseNextSymbol(data,offset);
            PREVIOUS_SYMBOL = symbol;
            int i = (int)PREVIOUS_SYMBOL&0xFF;
            sb.append(Constants.SYMBOLS[i]);
            System.out.println(symbol);
            System.out.println(sb.toString());
            if((PREVIOUS_SYMBOL&0xFF) == 0xFE){
                break;
            }
        }
        return sb.toString();
    }    
    
}
