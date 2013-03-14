package table;

import undo.*;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        //TODO : check that the data and the colnames are the same length
        this.isModified = false;
    }
    
    
    public PassTableModel(String[] columnNames) {
        this( columnNames, new ArrayList<Object[]>() );
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
        return ( row >= 0 );
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

        data.get( row )[ col ] = value;
        this.isModified = true;

        if( areUndoListenersRegistered() && undoable ){
            JvCellEdit cellEdit = new JvCellEdit( this, oldValue, value, row, col );
            notifyUndoableActionHappened( new UndoableEditEvent( this,
                    cellEdit ) );
        }//end if

        fireTableCellUpdated( row, col );
    }//end setValueAt
    
    
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
        if( areUndoListenersRegistered() && undoable ){
            JvRowsAdd rowsEdit = new JvRowsAdd( this, index );
            notifyUndoableActionHappened( new UndoableEditEvent( this,
                    rowsEdit ) );
        }//end if
        
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
            
            // if undoListeners are set and the event is undoable, adds it to
            // the undoManager list
            if( areUndoListenersRegistered() && undoable ){
                JvRowsDelete rowsDelete = new JvRowsDelete( this,
                        data.get( index ), index );
                notifyUndoableActionHappened( new UndoableEditEvent( this,
                        rowsDelete ) );
            }//end if
            
            // removes the row
            data.remove( index );
            this.fireTableDataChanged();
            this.isModified = true;
        }
        
    }// end deleteRow
    
    
    /********************************************************************
     * manages copy and paste - Excel style
     ********************************************************************/
    
    /**
     * copies the selected content of the table using the excel style, i.e. :<br>
     * <ul>
     * <li>the new line character \n delimits the rows
     * <li>the tab character \t (tab) delimits the columns
     * </ul>
     * <br>
     * A string consisting of the rows and columns selected is then copied to
     * the clipboard
     * 
     * @param selectedRows
     *            the contiguous rows to copy
     * @param selectedCols
     *            the contiguous columns to copy
     */
    public void copy( int[] selectedRows, int[] selectedCols ) {
        
        StringSelection strSelection;
        
        // if nothing selected, returns
        if( selectedRows.length == 0 && selectedCols.length == 0 ){
            return;
            
            // if only one cell is selected, just copies its content
        }else if( selectedRows.length == 1 && selectedCols.length == 1 ){
            strSelection = new StringSelection( (String) this.getValueAt(
                    selectedRows[ 0 ], selectedCols[ 0 ] ) );
            
            // if more than one cell is selected, constructs a string with \n
            // and \t to delimit the rows and cols
        }else{
            StringBuffer buffer = new StringBuffer();
            for( int row : selectedRows ){
                for( int col : selectedCols ){
                    buffer.append( this.getValueAt( row, col ) + "\t" );
                }
                buffer.append( "\n" );
            }
            
            strSelection = new StringSelection( buffer.toString() );
            
        }// end if
        
        // copies the content (as a string) to the clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents( strSelection, strSelection );
    }// end copy
    
    
    /**
     * cuts data from the model and copies them to the clipboard.
     * @param selectedRows
     * @param selectedCols
     * @param undoable
     */
    public void cut( int[] selectedRows, int[] selectedCols, boolean undoable ) {
        
        if( undoable )
            this.copy( selectedRows, selectedCols );
        
        ArrayList<String> oldValues = new ArrayList<String>();
        
        for( int row : selectedRows ){
            for( int col : selectedCols ){
                String oldValue = (String) this.getValueAt( row, col );
                oldValues.add( oldValue );
                this.setValueAt( "", row, col, false );
            }
        }
        
        // if the cut is undoable, creates an undoable event of type
        // JvCut
        if( areUndoListenersRegistered() && undoable ){
            JvCut rowsCut = new JvCut( this, oldValues.toArray(), selectedRows,
                    selectedCols );
            notifyUndoableActionHappened( new UndoableEditEvent( this,
                    rowsCut ) );
        }//end if

        // spreads the changes
        this.fireTableDataChanged();
    }
    
    
    /**
     * pastes the specified content to the table using the excel style, i.e. :<br>
     * <ul>
     * <li>the new line character \n delimits the rows
     * <li>the tab character \t (tab) delimits the columns
     * </ul>
     * <br>
     * <br>
     * 
     * Note that if the row or column indexes are out of range, an exception is
     * thrown. No check is done !<br>
     * 
     * @param clipboardContent
     *            the content to paste
     * @param startRow
     *            the row of the table in which to begin pasting
     * @param startCol
     *            the column of the table in which to begin pasting
     * @param undoable
     *            true if the pasting is undoable, false otherwise
     */
    
    // TODO : problem when some cells are empty
    public void paste( String clipboardContent, int startRow, int startCol,
            boolean undoable ) {
        
        try{
            // Stringbuilder to gather the old data into a string
            // using \n and \t to delimit the rows and cols respectively
            StringBuilder builder = new StringBuilder();
            
            // splits the clipboard contents into rows
            Scanner stLines = new Scanner( clipboardContent );
            stLines.useDelimiter( "\n" );
            
            for( int i = 0; stLines.hasNext(); i++ ){
                // if it is not the first row, adds a delimiter to the old
                // values String
                if( i != 0 ){
                    builder.append( "\n" );
                }
                // gets next row. we need to add smthing at the end of the line
                // for the split method to function properly, even if there are
                // only tabs
                String line = stLines.next() + "\0";
                System.out.println( "line length " + line.length() );
                String[] tabs = line.split( "\t" );
                System.out.println( "tabs length " + tabs.length );
                
                for( int j = 0; j < tabs.length; j++ ){
                    // if we are in the boundaries of the table, pastes the new
                    // value
                    if( startRow + i < this.getRowCount()
                            && startCol + j < this.getColumnCount() ){
                        // if not the first col, adds a delimiter to the
                        // oldValues string
                        if( j != 0 )
                            builder.append( "\t" );
                        
                        // records the old value
                        String oldValue = (String) this.getValueAt( startRow
                                + i, startCol + j );
                        builder.append(  oldValue );
                        
                        // gets the new values and updates the table
                        String newValue = tabs[ j ].trim();
                        System.out.println( "newvalue " + newValue );
                        this.setValueAt( newValue, startRow + i, startCol + j,
                                false );
                    }else{ // if outreached the table boudaries, just skips to
                           // the next row
                        break;
                    }// end if
                }// end for cols
                
            }//end forLines

            
            // if the paste is undoable, creates an undoable event of type
            // JvPaste
            if( areUndoListenersRegistered() && undoable ){
                JvPaste rowsPaste = new JvPaste( this, builder.toString(),
                        clipboardContent, startRow, startCol );
                notifyUndoableActionHappened( new UndoableEditEvent( this,
                        rowsPaste ) );
            }// end if
            
            // spreads the changes
            this.fireTableDataChanged();

        }catch( HeadlessException e1 ){
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
    }// end paste
    
    
    /********************************************************************
     * manages undo listeners /
     ********************************************************************/
    
    /**
     * manages the listeners for the undo/redo actions
     * 
     * @param listener
     */
    public void addUndoableEditListener( UndoableEditListener listener ) {
        this.listenerList.add( UndoableEditListener.class, listener );
    }//end addUndoableEditListener


    public boolean areUndoListenersRegistered() {
        return getListeners( UndoableEditListener.class ) != null;
    }//end areUndoListenersRegistered


    public void notifyUndoableActionHappened( UndoableEditEvent edit ){
        for( UndoableEditListener listener : getListeners( UndoableEditListener.class ) )
            listener.undoableEditHappened( edit );
    }//end notifyUndoableActionHappened

}// end class
