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
public class BusinessLayer {
    
    private static File huffmanTreeOffsetsFile;
    private static File huffmanTreesFile;
    private static File textbankFile;
    
    private static short[] huffmanTreeOffsets;
    private static HuffmanTree[] huffmanTrees;
    private static String[] textbankStrings;
    
    private static String[] gamescript;
    
    private static Map<Integer,Integer>[] newSymbolCounters;
    private static HuffmanTreeNode[] newHuffmanTreeTopNodes;
    private static byte[][] newHuffmanSymbols;
    private static byte[][] newHuffmanTrees;
    private static byte[] newHuffmanTreesFileBytes;
    private static byte[] newHuffmantreeOffsetsFileBytes;
    
    private static File newHuffmanTreesFile;
    private static File newHuffmanTreeOffsetsFile;
    
    private static byte[][] newStringBytes;
    private static byte[] newTextbank;
    private static byte[][] newTextbanks;
    
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
            //System.out.println(Arrays.toString(Arrays.copyOfRange(data,bankPointer+1,(bankPointer+data[bankPointer]+1))));
            textbankStrings[i] = s;
            bankPointer += data[bankPointer]+1;
            if(bankPointer+1>=data.length){
                textbankStrings = Arrays.copyOfRange(textbankStrings,0,i+1);
                break;
            }
        }         
    }
    
    public static void parseAllTextbanks(){
       try{
            System.out.println("sfc.segahr.BusinessLayer.parseTextbank() - Parsing textbank ...");
            try{
                gamescript = new String[0];
                for(int i=0;i<100;i++){
                    String index = Integer.toString(i);
                    while(index.length()<2){
                        index = "0"+index;
                    }
                    Path path = Paths.get(textbankFile.getParent()+"\\textbank"+index+".bin");
                    byte[] data = Files.readAllBytes(path); 
                    parseTextbank(data, i);
                    String[] workingStringArray = Arrays.copyOf(gamescript, gamescript.length + textbankStrings.length);
                    System.arraycopy(textbankStrings, 0, workingStringArray, gamescript.length, textbankStrings.length);
                    gamescript = workingStringArray;
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
        StringBuilder string = new StringBuilder();
        StringBuilder bitsString = new StringBuilder();
        HuffmanTree.PREVIOUS_SYMBOL = (byte)0xFE;
        HuffmanTree.STRING_BYTE_COUNTER = 0;
        HuffmanTree.STRING_BIT_COUNTER = 0;
        while(true){
            byte symbol = huffmanTrees[(int)HuffmanTree.PREVIOUS_SYMBOL&0xFF].parseNextSymbol(data,offset,bitsString);
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
                string.append(symbolString);
            }
            HuffmanTree.PREVIOUS_SYMBOL = symbol;
        }
        System.out.println(bitsString.toString());
        return string.toString();
    }    
    
    public static void produceTrees(){
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Producing trees ...");  
        countSymbols();
        makeTrees();
        produceTreeFileBytes();
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Trees produced.");
    }  
    
    private static void countSymbols(){
        System.out.println("sfc.segahr.BusinessLayer.countSymbols() - Counting symbols ...");
        Map<Integer,Map<Integer,Integer>> symbolCounters = new HashMap<Integer,Map<Integer,Integer>>();
        byte previousSymbol = (byte)0xFE;
        for(int i = 0;i<gamescript.length;i++){
            String string = gamescript[i];
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
        for(Integer i : symbolCounters.keySet()){
            symbolCounters.put(i,sortByValueDesc(symbolCounters.get(i)));
        }
        List<Integer> sortedKeys=new ArrayList<Integer>(symbolCounters.keySet());
        Collections.sort(sortedKeys);
        newSymbolCounters = new Map[256];
        for(Integer i : sortedKeys){
            newSymbolCounters[i] = symbolCounters.get(i);
        }
        for(int i=0;i<newSymbolCounters.length;i++){
            String index = Integer.toString(i,16).toUpperCase();
            while(index.length()<2){
                index = "0"+index;
            }
            System.out.println("Counters after character " + index + ":'" + Constants.SYMBOLS[i&0xFF] 
                    + "' : "+((symbolCounters.get(i)!=null)?symbolCountersToString(symbolCounters.get(i)):"Unused symbol, no tree !"));
        }
        System.out.println("sfc.segahr.BusinessLayer.countSymbols() - Symbols counted.");        
    }
    
    private static void makeTrees(){
        System.out.println("sfc.segahr.BusinessLayer.makeTrees() - Making trees ...");
        newHuffmanTrees = new byte[newSymbolCounters.length][];
        newHuffmanSymbols = new byte[newSymbolCounters.length][];
        newHuffmanTreeTopNodes = new HuffmanTreeNode[newSymbolCounters.length];
        for(int i=0;i<newSymbolCounters.length;i++){
            if(newSymbolCounters[i]!=null){
                Map<Integer,Integer> map = sortByValueAsc(newSymbolCounters[i]);
                Integer[] symbols = new Integer[map.size()];
                ((Set<Integer>)map.keySet()).toArray(symbols);
                Integer[] weights = new Integer[map.size()];
                ((Collection<Integer>)map.values()).toArray(weights);
                System.out.println("Symbol '"+Constants.SYMBOLS[i]+"' ("+i+") data :");
                System.out.println("\tsymbols : "+Arrays.toString(symbols));
                System.out.println("\tweights : "+Arrays.toString(weights));
                HuffmanTreeNode[] huffmanTreeNodes = new HuffmanTreeNode[map.size()];
                int huffmanTreeNodeIndex = 0;
                List<HuffmanTreeNode> nodeList = new ArrayList<HuffmanTreeNode>();
                for(Integer symbol : map.keySet()){
                    HuffmanTreeNode node = new HuffmanTreeNode();
                    node.isLeaf = true;
                    node.symbol = symbol;
                    node.weight = map.get(symbol);
                    node.symbolString = Constants.SYMBOLS[symbol];
                    huffmanTreeNodes[huffmanTreeNodeIndex] = node;
                    nodeList.add(node);
                    huffmanTreeNodeIndex++;
                }
                HuffmanTreeNode topNode = HuffmanTreeNode.makeTree(nodeList);
                HuffmanTreeNode.attributeCodes(topNode,null);
                System.out.println("\tHuffman Tree : "+topNode);
                String treeBitString = topNode.getTreeBitString(null);
                System.out.println("\tTree Bit String : "+treeBitString);
                byte[] treeSymbolBytes = topNode.makeTreeSymbolBytes();
                System.out.println("\tTree Symbols : " + Arrays.toString(treeSymbolBytes));
                byte[] treeBytes = HuffmanTreeNode.makeTreeBytes(treeBitString);
                System.out.println("\tTree Bytes : " + Arrays.toString(treeBytes));
                newHuffmanSymbols[i] = treeSymbolBytes;
                newHuffmanTrees[i] = treeBytes;
                newHuffmanTreeTopNodes[i] = topNode;
            }else{
                newHuffmanSymbols[i] = new byte[0];
                newHuffmanTrees[i] = new byte[0];
                newHuffmanTreeTopNodes[i] = null;
                System.out.println("Symbol '"+Constants.SYMBOLS[i]+"' ("+i+") data :\n\t"+Arrays.toString(newHuffmanTrees[i]));
            }
        }
        System.out.println("sfc.segahr.BusinessLayer.makeTrees() - Trees made.");
    }
    
    public static void produceTreeFileBytes(){
        System.out.println("sfc.segahr.BusinessLayer.produceTreeFileBytes() - Producing Tree File Bytes ...");
        newHuffmanTreesFileBytes = new byte[0];
        newHuffmantreeOffsetsFileBytes =new byte[255*2];
        short treePointer = 0;
        for(int i=0;i<255;i++){
            treePointer += newHuffmanSymbols[i].length;
            byte[] workingByteArray = Arrays.copyOf(newHuffmanTreesFileBytes, newHuffmanTreesFileBytes.length + newHuffmanSymbols[i].length + newHuffmanTrees[i].length);
            System.arraycopy(newHuffmanSymbols[i], 0, workingByteArray, newHuffmanTreesFileBytes.length, newHuffmanSymbols[i].length);
            System.arraycopy(newHuffmanTrees[i], 0, workingByteArray, newHuffmanTreesFileBytes.length + newHuffmanSymbols[i].length, newHuffmanTrees[i].length);
            newHuffmanTreesFileBytes = workingByteArray;
            if(newHuffmanTrees[i].length==0 && newHuffmanSymbols[i].length==0){
                newHuffmantreeOffsetsFileBytes[i*2] = (byte)0xFF;
                newHuffmantreeOffsetsFileBytes[i*2+1] = (byte)0xFF;
            }else{
                newHuffmantreeOffsetsFileBytes[i*2] = (byte)((treePointer&0xFF00)>>8);
                newHuffmantreeOffsetsFileBytes[i*2+1] = (byte)(treePointer&0xFF);
            }
            treePointer += newHuffmanTrees[i].length;
        }
        System.out.println("sfc.segahr.BusinessLayer.produceTreeFileBytes() - Tree File Bytes produced.");
    }
    
    public static void writeFiles(){
        try {
            Date d = new Date();
            DateFormat df = new SimpleDateFormat("YYMMddhhmmss");
            String dateString = df.format(d);
            Path offsetsFilePath = Paths.get(huffmanTreeOffsetsFile.getParent()+"\\huffmantreeoffsets-"+dateString+".bin");
            Path treesFilePath = Paths.get(huffmanTreesFile.getParent()+"\\huffmantrees-"+dateString+".bin");
            Files.write(offsetsFilePath,newHuffmantreeOffsetsFileBytes);
            Files.write(treesFilePath, newHuffmanTreesFileBytes);
            for(int i=0;i<newTextbanks.length;i++){
                String index = String.valueOf(i);
                while(index.length()<2){
                    index = "0"+index;
                }
                Path textbankFilePath = Paths.get(huffmanTreeOffsetsFile.getParent()+"\\textbank"+index+"-"+dateString+".bin");
                Files.write(textbankFilePath, newTextbanks[i]);
            }
        } catch (IOException ex) {
            Logger.getLogger(BusinessLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public static void produceTextbanks(){
        System.out.println("sfc.segahr.BusinessLayer.produceTextbanks() - Producing text banks ...");
        newStringBytes = new byte[gamescript.length][];
        byte previousSymbol = (byte)0xFE;
        for(int i = 0;i<gamescript.length;i++){
            String string = gamescript[i];
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
            StringBuilder sb = new StringBuilder();
            for(int l=0;l<symbols.length;l++){
                sb.append(newHuffmanTreeTopNodes[previousSymbol&0xFF].getCodeBitString(symbols[l]));
                previousSymbol = (byte)(symbols[l]&0xFF);
            }
            while(sb.length()%8!=0){
                sb.append("0");
            }
            byte[] stringBytes = new byte[sb.length()/8];
            for(int m=0;m<sb.length();m+=8){
                Byte b = (byte)Integer.parseInt(sb.substring(m, m+8), 2);
                stringBytes[m/8] = b;
            }
            newStringBytes[i] = stringBytes;
            System.out.println(string+"\n"+sb.toString()+"->"+Arrays.toString(stringBytes));
            
        }
        
        byte[] textbankBytes = new byte[0];
        int stringIndex = 0;
        int textbankIndex = 0;
        newTextbanks = new byte[(newStringBytes.length/256)+1][];
        for(int i=0;i<newStringBytes.length;i++){
            byte stringBytesLength = (byte)newStringBytes[i].length;
            byte[] workingByteArray = Arrays.copyOf(textbankBytes, textbankBytes.length+1+newStringBytes[i].length);
            workingByteArray[textbankBytes.length] = stringBytesLength;
            System.arraycopy(newStringBytes[i], 0, workingByteArray, textbankBytes.length+1, newStringBytes[i].length);
            textbankBytes = workingByteArray;
            stringIndex++;
            if(stringIndex==256||i==newStringBytes.length-1){
                newTextbank = textbankBytes;
                newTextbanks[textbankIndex] = newTextbank;
                textbankBytes = new byte[0];
                stringIndex = 0;
                textbankIndex++;
            }
        }
        System.out.println("sfc.segahr.BusinessLayer.produceTextbanks() - Text banks produced.");
    }

    
    
}
