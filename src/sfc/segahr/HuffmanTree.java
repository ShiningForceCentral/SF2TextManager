/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfc.segahr;

import java.util.Arrays;

/**
 *
 * @author wiz
 */
public class HuffmanTree {
    
    private int index;
    private byte[] symbols;
    private byte[] tree;
    
    public static byte STRING_BYTE;
    public static int STRING_BYTE_COUNTER;    
    public static int STRING_BIT_COUNTER;
    
    public HuffmanTree(int index, byte[] data, short offset){
        this.index = index;
        splitTree(data,offset);
        /* System.out.println("sfc.segahr.HuffmanTree.<init>() - Created new HuffmanTree with following data :"
                + "\nsymbols : " + Arrays.toString(symbols)
                + "\ntree : " + Arrays.toString(tree)); */
    }
    
    private void splitTree(byte[] data, short offset){
        int nonLeafCounter = 0;
        int leafCounter = 0;
        int treeByteCounter = 0;
        byte workByte = data[offset];
        int workByteBitCounter = 0;
        while(leafCounter<=nonLeafCounter){
            if(workByteBitCounter>7){
                treeByteCounter++;
                workByte = data[offset+treeByteCounter];
                workByteBitCounter = 0;
            }
            int bit = (workByte >> (7-workByteBitCounter)) & 1;
            if(bit == 0){
                nonLeafCounter++;
            }else{
                leafCounter++;
            }
            workByteBitCounter++;
        }
        symbols = new byte[leafCounter];
        System.arraycopy(data,offset-leafCounter,symbols,0,leafCounter);
        tree = new byte[treeByteCounter+1];
        System.arraycopy(data,offset,tree,0,treeByteCounter+1);
    }
    
    public String toString(){
        return "HuffmanTree " + index + " for symbol \'" + Constants.SYMBOLS[index] + "\' : "
                + "\n\tSymbols (" + symbols.length + ") : " + printSymbols()
                + "\n\tTree : " + printTree();
    }
    
    private String printSymbols(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<symbols.length;i++){
            sb.append(symbols[i]+":\'" + Constants.SYMBOLS[(int)symbols[i]&0xFF] + "\' ");
        }
        return sb.toString();
    }
    
     private String printTree(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<tree.length;i++){
            sb.append(String.format("%8s", Integer.toBinaryString(tree[i] & 0xFF)).replace(' ', '0')+" ");
        }
        return sb.toString();
    }
    
    public byte parseNextSymbol(byte[] data, short offset){
        byte symbol = 0;
        int symbolsSkipCounter = 0;
        int nonLeafCounter = 0;
        int leafCounter = 0;
        byte treeByte = tree[0];
        int treeByteCounter = 0;
        int treeBitCounter = 0;
        byte stringByte = data[offset];
        while(true){
            if(treeBitCounter>7){
                treeByteCounter++;
                treeByte = tree[treeByteCounter];
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
                    
                }else{
                    nonLeafCounter = 0;
                    leafCounter = 0;
                    while(leafCounter<=nonLeafCounter){                        
                        if(treeBitCounter>7){
                            treeByteCounter++;
                            treeByte = tree[treeByteCounter];
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
                }
            }else{
                break;
            }
        }
        symbol = symbols[symbols.length-1-symbolsSkipCounter];
        return symbol;
    }
    
}
