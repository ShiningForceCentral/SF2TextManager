/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author wiz
 */
public class TextEncoder {
    
    private static Map<Integer,Integer>[] newSymbolCounters;
    private static HuffmanTreeNode[] newHuffmanTreeTopNodes;
    private static byte[][] newHuffmanSymbols;
    private static byte[][] newHuffmanTrees;
    private static byte[] newHuffmanTreesFileBytes;
    private static byte[] newHuffmantreeOffsetsFileBytes;
    
    private static byte[][] newStringBytes;
    private static byte[] newTextbank;
    private static byte[][] newTextbanks;     
    
    public static HuffmanTreeNode makeTree(List<HuffmanTreeNode> nodes){
        if(nodes.isEmpty()){
            return null;
        } else if(nodes.size()==1){
            return nodes.get(0);
        } else{
            HuffmanTreeNode parent = new HuffmanTreeNode();
            parent.isLeaf = false;
            parent.leftChild = nodes.get(0);
            parent.rightChild = nodes.get(1);
            parent.weight = parent.leftChild.weight + parent.rightChild.weight;
            nodes.remove(0);
            nodes.remove(0);
            int insertIndex = 0;
            for(int i=0;i<nodes.size();i++){
                if(nodes.get(i).weight<parent.weight
                        && i == nodes.size()-1){
                    insertIndex = nodes.size();
                    break;
                } else if(nodes.get(i).weight>parent.weight){
                    insertIndex = i;
                    break;
                }
            }
            nodes.add(insertIndex, parent);
            return makeTree(nodes);
        }
    }
    
    public static void attributeCodes(HuffmanTreeNode node,StringBuilder bitString){
        if(bitString == null){
            bitString = new StringBuilder();
        }
        if(node.isLeaf){
            node.codeBitString = bitString.toString();
        }else{
            attributeCodes(node.leftChild,new StringBuilder(bitString).append("0"));
            attributeCodes(node.rightChild,new StringBuilder(bitString).append("1"));
        }
    }
    
    public static void produceTrees(String[] gamescript){
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Producing trees ...");  
        countSymbols(gamescript);
        makeTrees();
        produceTreeFileBytes();
        System.out.println("sfc.segahr.BusinessLayer.produceTrees() - Trees produced.");
    }  
    
    private static void countSymbols(String[] gamescript){
        System.out.println("sfc.segahr.BusinessLayer.countSymbols() - Counting symbols ...");
        Map<Integer,Map<Integer,Integer>> symbolCounters = new HashMap<>();
        byte previousSymbol = (byte)0xFE;
        for (String string : gamescript) {
            //System.out.println(string);
            int stringPointer = 0;
            int symbolsPointer = 0;
            byte[] symbols = new byte[string.length()+1];
            while(stringPointer<string.length()){
                for(int k=0;k<Symbols.TABLE.length;k++){
                    if(((previousSymbol&0xFF)!=0xFC) && ((previousSymbol&0xFF)!=0xFD)){
                        if(string.substring(stringPointer).indexOf(Symbols.TABLE[k])==0){
                            byte symbol = (byte)k;
                            symbols[symbolsPointer] = symbol;
                            symbolsPointer++;
                            stringPointer = stringPointer + Symbols.TABLE[k].length();
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
                    if(k+1==Symbols.TABLE.length){
                        System.err.println("Current character "+string.charAt(stringPointer)+" is not recognized as the beginning of a known symbol, and will be ignored.");
                        stringPointer++;
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
        List<Integer> sortedKeys=new ArrayList<>(symbolCounters.keySet());
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
            System.out.println("Counters after character " + index + ":'" + Symbols.TABLE[i&0xFF] 
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
                System.out.println("Symbol '"+Symbols.TABLE[i]+"' ("+i+") data :");
                System.out.println("\tsymbols : "+Arrays.toString(symbols));
                System.out.println("\tweights : "+Arrays.toString(weights));
                int huffmanTreeNodeIndex = 0;
                List<HuffmanTreeNode> nodeList = new ArrayList<>();
                for(Integer symbol : map.keySet()){
                    HuffmanTreeNode node = new HuffmanTreeNode();
                    node.isLeaf = true;
                    node.symbol = symbol;
                    node.weight = map.get(symbol);
                    node.symbolString = Symbols.TABLE[symbol];
                    nodeList.add(node);
                    huffmanTreeNodeIndex++;
                }
                HuffmanTreeNode topNode = TextEncoder.makeTree(nodeList);
                TextEncoder.attributeCodes(topNode,null);
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
                System.out.println("Symbol '"+Symbols.TABLE[i]+"' ("+i+") data :\n\t"+Arrays.toString(newHuffmanTrees[i]));
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
    
    public static void produceTextbanks(String[] gamescript){
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
                for(int k=0;k<Symbols.TABLE.length;k++){
                    if(((previousSymbol&0xFF)!=0xFC) && ((previousSymbol&0xFF)!=0xFD)){
                        if(string.substring(stringPointer).indexOf(Symbols.TABLE[k])==0){
                            byte symbol = (byte)k;
                            symbols[symbolsPointer] = symbol;
                            symbolsPointer++;
                            stringPointer = stringPointer + Symbols.TABLE[k].length();
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
                    if(k+1==Symbols.TABLE.length){
                        System.err.println("Current character "+string.charAt(stringPointer)+" is not recognized as the beginning of a known symbol, and will be ignored.");
                        stringPointer++;
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
    
    public static byte[] getNewHuffmanTreesFileBytes() {
        return newHuffmanTreesFileBytes;
    }

    public static byte[] getNewHuffmantreeOffsetsFileBytes() {
        return newHuffmantreeOffsetsFileBytes;
    }

    public static byte[][] getNewTextbanks() {
        return newTextbanks;
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
            sb.append(index).append(":'").append(Symbols.TABLE[key]).append("'=").append(map.get(key)).append(", ");
        }
        sb.delete(sb.length()-2,sb.length());
        sb.append("]");
        return sb.toString();
    }    
    
}
