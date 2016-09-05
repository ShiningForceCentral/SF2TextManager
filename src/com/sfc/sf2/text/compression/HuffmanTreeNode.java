/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.compression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wiz
 */
public class HuffmanTreeNode {
    public boolean isLeaf;
    public int symbol;
    public String symbolString;
    public String codeBitString;
    public HuffmanTreeNode parent;
    public HuffmanTreeNode leftChild;
    public HuffmanTreeNode rightChild;
    public int weight;
    
    public String getCodeBitString(byte symbol){
        if(isLeaf && ((this.symbol&0xFF) == (symbol&0xFF))){
            return this.codeBitString;
        }else{
            if(isLeaf){
                return null;
            }else{
                String ls = leftChild.getCodeBitString(symbol);
                if(ls!=null){
                    return ls;
                }else{
                    String rs = rightChild.getCodeBitString(symbol);
                    if(rs!=null){
                        return rs;
                    }else{
                        return null; 
                    }                   
                }
            }
        }
    }
    
    public byte[] makeTreeSymbolBytes(){
        List<Integer> symbols = getTreeSymbolSequence(null);
        byte[] symbolBytes = new byte[symbols.size()];
        for(int i=0;i<symbols.size();i++){
            symbolBytes[i] = (byte)(symbols.get(i)&0xFF);
        } 
        return symbolBytes;
    }
    
    private List<Integer> getTreeSymbolSequence(List<Integer> symbolSequence){
        if(symbolSequence == null){
            symbolSequence = new ArrayList<>();
        }
        if(isLeaf){
            symbolSequence.add(0, symbol);
            return symbolSequence;
        }else{
            symbolSequence = leftChild.getTreeSymbolSequence(symbolSequence);
            symbolSequence = rightChild.getTreeSymbolSequence(symbolSequence);
            return symbolSequence;
        }
    }
    
    public String getTreeBitString(StringBuilder bitString){
        if(bitString == null){
            bitString = new StringBuilder();
        }
        if(isLeaf){
            return bitString.append("1").toString();
        }else{
            bitString.append("0");
            leftChild.getTreeBitString(bitString);
            rightChild.getTreeBitString(bitString);
            return bitString.toString();
        }
    }
    
    public static byte[] makeTreeBytes(String treeBitString){
        byte[] treeBytes;
        StringBuilder sb = new StringBuilder(treeBitString);
        while(sb.length()%8!=0){
            sb.append("0");
        }
        treeBytes = new byte[sb.length()/8];
        for(int i=0;i<sb.length();i+=8){
            Byte b = (byte)Integer.parseInt(sb.substring(i, i+8), 2);
            treeBytes[i/8] = b;
        }
        return treeBytes;
    }
    
    @Override
    public String toString(){
        if(isLeaf){
            return "("+symbol+":"+"'"+symbolString+"'="+weight+"->'" + codeBitString + "')";
        }else{
            return "(" + leftChild.toString() + "," + rightChild.toString() + ")";
        }
    }
}
