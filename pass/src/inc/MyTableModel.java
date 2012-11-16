package inc;
import java.awt.*;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

public class MyTableModel extends AbstractTableModel implements Serializable {


	private static final long serialVersionUID = -9197706602911166047L;
	
	private String[] columnNames;
	private Object[][] data;


	public MyTableModel(String[] columnNames, Object[][] data){
		
		super();		
		this.columnNames = columnNames;
		this.data = data;	
	}
	

	
	public String[] getColumnNames() {
		return columnNames;
	}



	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}



	public Object[][] getData() {
		return data;
	}



	public void setData(Object[][] data) {
		this.data = data;
	}

   
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public void setValueAt(Object value, int row, int col){
        
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    
    
    @SuppressWarnings("rawtypes")
    public Class getColumnClass(int col) {  
        if (col == 0)       //first column (id) accepts only Integer values  
            return Integer.class;  
        else return String.class; 
    }
    
    
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (row < 0 || col < 1) {
            return false;
        } else {
            return true;
        }
    }   
    
}
