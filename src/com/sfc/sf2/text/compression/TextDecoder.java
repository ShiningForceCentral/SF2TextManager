/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.compression;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author wiz
 */
public class TextDecoder {
    
    private static byte PREVIOUS_SYMBOL;
    private static int STRING_BYTE_COUNTER;    
    private static int STRING_BIT_COUNTER; 
    
    private static short[] huffmanTreeOffsets;    
    private static HuffmanTree[] trees;
    
    
    public static void parseOffsets(byte[] data){
            huffmanTreeOffsets = new short[data.length/2];
            ByteBuffer wrappedData = ByteBuffer.wrap(data);
            wrappedData.asShortBuffer().get(huffmanTreeOffsets);
            System.out.println("com.sfc.sf2.text.compression.HuffmanTree.parseOffsets() - huffmanTreeOffsets : "
                + "\n" + Arrays.toString(huffmanTreeOffsets));
            trees = new HuffmanTree[huffmanTreeOffsets.length];
    }
    
    public static void parseTrees(byte[] data){
        System.out.println("com.sfc.sf2.text.compression.HuffmanTree.parseTrees() - Parsing trees ...");
        for(int i = 0;i<huffmanTreeOffsets.length;i++){
            if(huffmanTreeOffsets[i]==-1){
                trees[i] = null;
            }else{
                trees[i] = new HuffmanTree(i,data,huffmanTreeOffsets[i]);
            }
        }
        logTrees();
        System.out.println("sfc.segahr.BusinessLayer.parseTrees() - Trees parsed.");            
    }  
    
    private static void logTrees(){
        System.out.println("com.sfc.sf2.text.compression.HuffmanTree.parseOffsets() - Parsed trees :");
        for(int i = 0;i<trees.length;i++){
            if(trees[i] != null){
                System.out.println(trees[i].toString());
            }else{
                System.out.println("No tree for index " + i);
            }
        }
        System.out.println("com.sfc.sf2.text.compression.HuffmanTree.parseOffsets() - End of parsed trees :");
    }        
    
    public static String[] parseTextbank(byte[] data, int textbankIndex){
        return parseTextbank(data, textbankIndex, 256);
    }
    
    public static String[] parseTextbank(byte[] data, int textbankIndex, int linesToParse){
        short bankPointer = 0;
        String[] textbankStrings = new String[linesToParse];
        int i;
        for(i=0;i<linesToParse;i++){
            int lineLength = (data[bankPointer]&0xFF);
            String s = parseString(data,(short)(bankPointer+1));
            String stringIndex = Integer.toString(textbankIndex*256+i,16).toUpperCase();
            while(stringIndex.length()<4){
                stringIndex = "0"+stringIndex;
            }
            System.out.println("$"+stringIndex+"("+lineLength+")="+s);
            //System.out.println(Arrays.toString(Arrays.copyOfRange(data,bankPointer+1,(bankPointer+data[bankPointer]+1))));
            textbankStrings[i] = s;
            bankPointer += (data[bankPointer]&0xFF)+1;
            if(bankPointer+1>=data.length){
                textbankStrings = Arrays.copyOfRange(textbankStrings,0,i+1);
                break;
            }
        }
        return textbankStrings;
    }
    
    private static String parseString(byte[] data, short offset){
        StringBuilder string = new StringBuilder();
        StringBuilder bitsString = new StringBuilder();
        PREVIOUS_SYMBOL = (byte)0xFE;
        STRING_BYTE_COUNTER = 0;
        STRING_BIT_COUNTER = 0;
        while(true){
            byte symbol = parseNextSymbol(trees[(int)PREVIOUS_SYMBOL&0xFF],data,offset,bitsString);
            if((symbol&0xFF) == 0xFE){
                //System.out.println(sb.toString());
                break;
            }else{
                String symbolString;
                if((PREVIOUS_SYMBOL&0xFF)== 0xFC || (PREVIOUS_SYMBOL&0xFF) == 0xFD){
                    symbolString = ";" + Integer.toString((int)symbol) + "}";
                }else{
                    symbolString = Symbols.TABLE[(int)symbol&0xFF];
                }
                string.append(symbolString);
            }
            PREVIOUS_SYMBOL = symbol;
        }
        System.out.println(bitsString.toString());
        return string.toString();
    }     
    
    private static byte parseNextSymbol(HuffmanTree huffmanTree, byte[] data, short offset, StringBuilder bitsString){
        byte symbol;
        int symbolsSkipCounter = 0;
        int nonLeafCounter;
        int leafCounter;
        byte treeByte = huffmanTree.tree[0];
        int treeByteCounter = 0;
        int treeBitCounter = 0;
        byte stringByte;
        bitsString.append("[");
        while(true){
            if(treeBitCounter>7){
                treeByteCounter++;
                treeByte = huffmanTree.tree[treeByteCounter];
                treeBitCounter = 0;
            }            
            int treeBit = (treeByte >> (7-treeBitCounter)) & 1;
            treeBitCounter++;
            if(treeBit == 0){
                if(STRING_BIT_COUNTER>7){
                    STRING_BYTE_COUNTER++;
                    STRING_BIT_COUNTER = 0;
                }
                stringByte = data[offset+STRING_BYTE_COUNTER];
                int stringBit = (stringByte >> (7-STRING_BIT_COUNTER)) & 1;
                STRING_BIT_COUNTER++;
                if(stringBit == 0){
                    bitsString.append("0");
                }else{
                    nonLeafCounter = 0;
                    leafCounter = 0;
                    while(leafCounter<=nonLeafCounter){                        
                        if(treeBitCounter>7){
                            treeByteCounter++;
                            treeByte = huffmanTree.tree[treeByteCounter];
                            treeBitCounter = 0;
                        }
                        int bit = (treeByte >> (7-treeBitCounter)) & 1;
                        treeBitCounter++;
                        if(bit == 0){
                            nonLeafCounter++;
                        }else{
                            leafCounter++;
                        }
                    }
                    symbolsSkipCounter += leafCounter;
                    bitsString.append("1");
                }
            }else{
                break;
            }
        }
        bitsString.append("] ");
        symbol = huffmanTree.symbols[huffmanTree.symbols.length-1-symbolsSkipCounter];
        return symbol;
    }
    
    
}
