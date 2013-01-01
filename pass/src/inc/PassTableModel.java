package inc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;

/**
 * This class is at the core of the application. It is a custom implementation
 * of the JTable Model, offering additional methods and using ArrayLists and
 * array of Object instead of Vectors to manipulate the data.
 * 
 * In combination with the UndoManager and JvCellEdit-JvRowsEdit class, it also
 * offer the undo/redo functionalities.
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 * 
 */
public class PassTableModel extends AbstractTableModel implements Serializable {
    
    private static final long serialVersionUID = -9197706602911166047L;
    
    private String[] columnNames;
    private List<Object[]> data;
    
    private boolean isModified;
    
    
    /********************************************************************
     * constructors /
     ********************************************************************/
    
    public PassTableModel(String[] columnNames, List<Object[]> data) {
        
        super();
        this.columnNames = columnNames;
        this.data = data;
        this.isModified = false;
    }
    
    
    public PassTableModel(String[] columnNames) {
        
        super();
        this.columnNames = columnNames;
        this.data = new ArrayList<Object[]>();
        this.isModified = false;
    }
    
    
    /********************************************************************
     * getters and setters /
     ********************************************************************/
    
    /**
     * returns true if a change occurred since the last modification reset
     * 
     * @return
     */
    public boolean isModified() {
        return isModified;
    }
    
    
    /**
     * sets the isModified variable to false
     */
    public void resetModified() {
        this.isModified = false;
    }
    
    
    /**
     * returns the headers of the table
     * 
     * @return
     */
    public String[] getColumnNames() {
        return columnNames;
    }
    
    
    /**
     * returns the header the specified column.
     */
    public String getColumnName( int col ) {
        return columnNames[ col ];
    }
    
    
    /**
     * sets the headers of the table
     * 
     * @param columnNames
     */
    public void setColumnNames( String[] columnNames ) {
        this.columnNames = columnNames;
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Class getColumnClass( int col ) {
        // if (col == 0)
        // return Integer.class;
        // else
        return String.class;
    }
    
    
    /**
     * returns the number of columns in the table
     */
    public int getColumnCount() {
        return columnNames.length;
    }
    
    
    /**
     * returns the number of rows in the table
     */
    public int getRowCount() {
        return data.size();
    }
    
    
    /**
     * returns true if the cell is editable, false otherwise
     */
    public boolean isCellEditable( int row, int col ) {
        // Note that the data/cell address is constant,
        // no matter where the cell appears onscreen.
        if( row < 0 ){
            return false;
        }else{
            return true;
        }
    }
    
    
    /**
     * returns the data contained in the table model as a list of rows.Each row
     * values are contained in an array of Objects.
     * 
     * @return
     */
    public List<Object[]> getData() {
        return data;
    }
    
    
    /**
     * sets the data contained in the table
     * 
     * @param data
     */
    public void setData( ArrayList<Object[]> data ) {
        this.data = data;
    }
    
    
    public Object getValueAt( int row, int col ) {
        return data.get( row )[ col ];
    }
    
    
    /**
     * sets the value of the given cell
     */
    public void setValueAt( Object value, int row, int col ) {
        // true, a boolean specifying that this action is undoable
        this.setValueAt( value, row, col, true );
        
    }
    
    
    /**
     * sets the value of the given cell
     * 
     * @param value
     * @param row
     * @param col
     * @param undoable
     */
    public void setValueAt( Object value, int row, int col, boolean undoable ) {
        
        Object oldValue = getValueAt( row, col );
        
        // if no change at all, return
        if( ( (String) oldValue ).equals( (String) value ) ){
            return;
        }

        UndoableEditListener listeners[] = getListeners( UndoableEditListener.class );
        if( undoable == false || listeners == null ){
            data.get( row )[ col ] = value;
            fireTableCellUpdated( row, col );
            this.isModified = true;
            return;
        }
        
        data.get( row )[ col ] = value;
        fireTableCellUpdated( row, col );
        this.isModified = true;
        JvCellEdit cellEdit = new JvCellEdit( this, oldValue, value, row, col );
        UndoableEditEvent editEvent = new UndoableEditEvent( this, cellEdit );
        for( UndoableEditListener listener : listeners )
            listener.undoableEditHappened( editEvent );
    }
    
    
    /********************************************************************
     * adds and delete rows /
     ********************************************************************/
    
    /**
     * adds the specified row to the model
     * 
     * @param row
     */
    public void addRow( Object[] row ) {
        
        this.addRow( row, -1, false );
    }
    
    
    /**
     * adds an empty row to the model, and makes it undoable
     */
    public void addRow() {
        
        Object[] row = new Object[columnNames.length];
        for( int i = 0; i < row.length; i++ ){
            row[ i ] = "";
        }
        this.addRow( row, -1, true );
        
    }
    
    
    /**
     * adds an empty row to the model at the specified index, but does not
     * record it as undoable
     */
    public void addRow( int index ) {
        
        Object[] row = new Object[columnNames.length];
        for( int i = 0; i < row.length; i++ ){
            row[ i ] = "";
        }
        
        this.addRow( row, index, false );
    }
    
    
    /**
     * adds a row to the specified index, but does not record it a undoable
     * 
     * @param row
     * @param index
     */
    public void addRow( Object[] row, int index ) {
        this.addRow( row, index, false );
    }
    
    
    /**
     * inserts the specified row to the model at the specified index. If the
     * index is out of bounds, the row will be inserted at the end of the table
     * 
     * @param row
     * @param index
     */
    public void addRow( Object[] row, int index, boolean undoable ) {
        
        UndoableEditListener listeners[] = getListeners( UndoableEditListener.class );
        
        // if the index is valid, adds a row at the index
        if( index >= 0 && index < data.size() ){
            data.add( index, row );
            this.fireTableRowsInserted( index, index );
            this.isModified = true;
            
        }else{ // if the index is out of range, adds a row at the end of the
               // table
            data.add( row );
            index = data.size() - 1;
            this.fireTableRowsInserted( index, index );
        }
        
        // finally, adds the undo object to the undo queue
        if( listeners != null && undoable ){
            JvRowsAdd rowsEdit = new JvRowsAdd( this, index );
            UndoableEditEvent editEvent = new UndoableEditEvent( this, rowsEdit );
            for( UndoableEditListener listener : listeners )
                listener.undoableEditHappened( editEvent );
        }
        
    }// end addRow
    
    
    /**
     * deletes the row at the specified index and makes it undoable
     * 
     * @param index
     */
    public void deleteRow( int index ) {
        deleteRow( index, true );
    }
    
    
    /**
     * deletes the row at the specified index.
     * 
     * @param index
     * @param undoable
     *            specified if the action must be added to the undoable list
     */
    public void deleteRow( int index, boolean undoable ) {
        
        if( index >= 0 && index < data.size() ){
            
            UndoableEditListener listeners[] = getListeners( UndoableEditListener.class );
            // if undoListeners are set and the event is undoable, adds it to
            // the undoManager list
            if( listeners != null && undoable ){
                
                // creates an undoable object and informs the listeners of the
                // change
                JvRowsDelete rowsEdit = new JvRowsDelete( this,
                        data.get( index ), index );
                UndoableEditEvent editEvent = new UndoableEditEvent( this,
                        rowsEdit );
                for( UndoableEditListener listener : listeners )
                    listener.undoableEditHappened( editEvent );
            }
            
            // removes the row
            data.remove( index );
            this.fireTableDataChanged();
            this.isModified = true;
        }
        
    }// end deleteRow
    
    
    /********************************************************************
     * manages undo listeners /
     ********************************************************************/
    
    /**
     * manages the listeners for the undo/redo actions
     * 
     * @param listener
     */
    public void addUndoableEditListener( UndoableEditListener listener ) {
        listenerList.add( UndoableEditListener.class, listener );
    }
    
}// end class
