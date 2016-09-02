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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static String[][] gamescript;
   
    
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
        try {
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsing textbank ...");
            Path path = Paths.get(textbankFile.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);
            parseTextbank(data,0);
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Textbank parsed.");
        } catch (IOException ex) {
            Logger.getLogger(BusinessLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void parseTextbank(byte[] data, int textbankIndex){
        short bankPointer = 0;
        textbankStrings = new String[256];
        int i;
        for(i=0;i<256;i++){
            String s = parseString(data,(short)(bankPointer+1));
            String stringIndex = Integer.toString(textbankIndex*256+i,16).toUpperCase();
            while(stringIndex.length()<4){
                stringIndex = "0"+stringIndex;
            }
            System.out.println("$"+stringIndex+";"+s);
            textbankStrings[i] = s;
            bankPointer += data[bankPointer]+1;
            if(bankPointer+1>=data.length){
                textbankStrings = Arrays.copyOfRange(textbankStrings,0,i+1);
                break;
            }
        }
        short offset = 1;
        String s = parseString(data,offset);         
    }
    
    public static void parseAllTextbanks(){
       try{
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsing textbank ...");
            try{
                gamescript = new String[0][];
                for(int i=0;i<100;i++){
                    String index = Integer.toString(i);
                    while(index.length()<2){
                        index = "0"+index;
                    }
                    Path path = Paths.get(textbankFile.getParent()+"\\textbank"+index+".bin");
                    byte[] data = Files.readAllBytes(path); 
                    parseTextbank(data, i);
                    gamescript = Arrays.copyOf(gamescript,gamescript.length + 1);
                    gamescript[i] = textbankStrings;
                }
            }catch(IOException e){
                System.out.println("No more textbank files to parse.");
            }
            
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Textbanks all parsed.");
        } catch(Exception e){
            System.err.println("sfc.segahr.BusinessLayer.parseTextbank() - Error while parsing textbankFile data : "+e);
        }            
    }    
    
    private static String parseString(byte[] data, short offset){
        StringBuilder sb = new StringBuilder();
        HuffmanTree.PREVIOUS_SYMBOL = (byte)0xFE;
        HuffmanTree.STRING_BYTE_COUNTER = 0;
        HuffmanTree.STRING_BIT_COUNTER = 0;
        while(true){
            byte symbol = huffmanTrees[(int)HuffmanTree.PREVIOUS_SYMBOL&0xFF].parseNextSymbol(data,offset);
            if((symbol&0xFF) == 0xFE){
                //System.out.println(sb.toString());
                break;
            }else{
                String symbolString = null;
                if((HuffmanTree.PREVIOUS_SYMBOL&0xFF)== 0xFC || (HuffmanTree.PREVIOUS_SYMBOL&0xFF) == 0xFD){
                    symbolString = ";" + Integer.toString((int)symbol) + "}";
                }else{
                    symbolString = Constants.SYMBOLS[(int)symbol&0xFF];
                }
                sb.append(symbolString);
            }
            HuffmanTree.PREVIOUS_SYMBOL = symbol;
        }
        return sb.toString();
    }    
    
    public static void produceTrees(){
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Producing trees ...");  
        Map<Integer,Map<Integer,Integer>> symbolCounters = new HashMap<Integer,Map<Integer,Integer>>();
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Counting symbols ...");
        byte previousSymbol = (byte)0xFE;
        for(int i = 0;i<gamescript.length;i++){
        //for(int i = 1;i<2;i++){
            for(int j = 0;j<gamescript[i].length;j++){
            //for(int j = 222;j<226;j++){
                String string = gamescript[i][j];
                //System.out.println(string);
                int stringPointer = 0;
                int symbolsPointer = 0;
                byte[] symbols = new byte[string.length()+1];
                while(stringPointer<string.length()){
                    for(int k=0;k<Constants.SYMBOLS.length;k++){
                        if(((previousSymbol&0xFF)!=0xFC) && ((previousSymbol&0xFF)!=0xFD)){
                            if(string.substring(stringPointer).indexOf(Constants.SYMBOLS[k])==0){
                                byte symbol = (byte)k;
                                symbols[symbolsPointer] = symbol;
                                symbolsPointer++;
                                stringPointer = stringPointer + Constants.SYMBOLS[k].length();
                                previousSymbol = symbol;
                                break;
                            }                            
                        }else{
                            String numberString = string.substring(stringPointer+1,string.indexOf("}",stringPointer+1));
                            byte symbol = Byte.valueOf(numberString);
                            symbols[symbolsPointer] = symbol;
                            symbolsPointer++;
                            stringPointer = stringPointer + 2 + numberString.length();
                            previousSymbol = symbol;
                            break;                        
                        }
                    }
                }
                symbols[symbolsPointer] = (byte)0xFE;
                if(symbolsPointer<stringPointer){
                    symbols = Arrays.copyOf(symbols,symbolsPointer+1);
                }
                //System.out.println("Symbol bytes : "+Arrays.toString(symbols));
                previousSymbol = (byte)0xFE;
                for(int l=0;l<symbols.length;l++){
                    if(!symbolCounters.containsKey(previousSymbol&0xFF)){
                        symbolCounters.put(previousSymbol&0xFF,new HashMap<Integer,Integer>());
                        symbolCounters.get(previousSymbol&0xFF).put(symbols[l]&0xFF,1);
                    }else{             
                        if(!symbolCounters.get(previousSymbol&0xFF).containsKey(symbols[l]&0xFF)){
                            symbolCounters.get(previousSymbol&0xFF).put(symbols[l]&0xFF, 1);
                        }else{
                            int counter = symbolCounters.get(previousSymbol&0xFF).get(symbols[l]&0xFF);
                            symbolCounters.get(previousSymbol&0xFF).put((int)(symbols[l]&0xFF),counter+1);
                        }
                    }
                    previousSymbol = (byte)(symbols[l]&0xFF);
                }
                
            }
        }
        for(Integer i : symbolCounters.keySet()){
            symbolCounters.put(i,sortByValueDesc(symbolCounters.get(i)));
        }
        List<Integer> sortedKeys=new ArrayList<Integer>(symbolCounters.keySet());
        Collections.sort(sortedKeys);
        for(Integer i : sortedKeys){
            String index = Integer.toString(i,16).toUpperCase();
            while(index.length()<2){
                index = "0"+index;
            }
            System.out.println("Counters after character " + index + ":'" + Constants.SYMBOLS[i&0xFF] + "' : "+symbolCountersToString(symbolCounters.get(i)));
        }
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Symbols counted.");
        
        //TODO Huffman tree generation algorithm
        
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Trees produced.");
    }    
 
    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValueDesc( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return ( o2.getValue() ).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
    
   public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValueAsc( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return ( o1.getValue() ).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }    
    
    private static String symbolCountersToString(Map<Integer,Integer> map){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(Integer key : map.keySet()){
            String index = Integer.toString(key,16).toUpperCase();
            while(index.length()<2){
                index = "0"+index;
            }
            sb.append(index).append(":'").append(Constants.SYMBOLS[key]).append("'=").append(map.get(key)).append(", ");
        }
        sb.delete(sb.length()-2,sb.length());
        sb.append("]");
        return sb.toString();
    }
}
