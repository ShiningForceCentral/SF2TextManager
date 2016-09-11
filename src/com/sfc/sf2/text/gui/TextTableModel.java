/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.text.gui;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author wiz
 */
public class TextTableModel extends AbstractTableModel {
    
    private final String[][] tableData;
    private final String[] columns = {"Index", "Line"};
 
    public TextTableModel(String[] gamescript) {
        super();
        tableData = new String[gamescript.length][];
        for(int i=0;i<gamescript.length;i++){
            tableData[i] = new String[2];
            String hexIndex = Integer.toHexString(i).toUpperCase();
            while(hexIndex.length()<4){
                hexIndex = "0" + hexIndex;
            }
            tableData[i][0] = hexIndex;
            tableData[i][1] = gamescript[i];
        }
    }
 
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }    
    
    @Override
    public int getRowCount() {
        return tableData.length;
    }
 
    @Override
    public int getColumnCount() {
        return columns.length;
    }
 
    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }
 
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableData[rowIndex][columnIndex];
    }
}
