package inc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;

public class PassTableModel extends AbstractTableModel implements Serializable {

	private static final long serialVersionUID = -9197706602911166047L;

	private String[] columnNames;
	private List<Object[]> data;
	
	

	
	public PassTableModel(String[] columnNames, ArrayList<Object[]> data) {

		super();
		this.columnNames = columnNames;
		this.data = data;
	}

	
	public PassTableModel(String[] columnNames) {

		super();
		this.columnNames = columnNames;
		this.data = new ArrayList<Object[]>();
	}

	
	public String[] getColumnNames() {
		return columnNames;
	}

	
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	
	public List<Object[]> getData() {
		return data;
	}

	
	public void setData(ArrayList<Object[]> data) {
		this.data = data;
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return data.size();
	}

	
	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}

	
	public void setValueAt(Object value, int row, int col) {

		this.setValueAt(value, row, col, true);
		
	}

    public void setValueAt(Object value, int row, int col, boolean undoable)
    {
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null)
        {
        	data.get(row)[col] = value;
    		fireTableCellUpdated(row, col);
            return;
        }


        Object oldValue = getValueAt(row, col);
        data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
        JvCellEdit cellEdit = new JvCellEdit(this, oldValue, value, row, col);
        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
        for (UndoableEditListener listener : listeners)
            listener.undoableEditHappened(editEvent);
    }
    
    public void addUndoableEditListener(UndoableEditListener listener){
        listenerList.add(UndoableEditListener.class, listener);
    }
    
	public void addRow(Object[] row) {

		data.add(row);
		this.fireTableRowsInserted(this.getRowCount() - 1,
				this.getRowCount() - 1);
	}

	
	public void addRow() {

		Object[] row = new Object[columnNames.length];
		for (int i = 0; i < row.length; i++) {
			row[i] = "";
		}
		data.add(row);
		this.fireTableDataChanged();
	}

	
	public void deleteRow(int index) {
		if (index >= 0 && index < data.size()) {
			data.remove(index);
			this.fireTableDataChanged();
		}
	}

	
	@SuppressWarnings("rawtypes")
	public Class getColumnClass(int col) {
		// if (col == 0)
		// return Integer.class;
		// else
		return String.class;
	}

	
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		if (row < 0) {
			return false;
		} else {
			return true;
		}
	}

}
