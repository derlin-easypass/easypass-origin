package undo;

import table.PassTableModel;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * this class is used to implement the undoManager. It stores the index and the
 * data of a deleted row, enabling a undo/redo action to be performed.
 * 
 * Note that it is managed by the undoManager, but it uses a method implemented
 * in the PassTableModel class. See the deleteRow(int index) and addRow methods of the
 * former for further informations.
 * 
 * @author lucy linder
 * @date Dec 21, 2012
 * 
 */
public class JvRowsDelete extends AbstractUndoableEdit {
    private static final long serialVersionUID = 5470678378853711947L;
    
    protected PassTableModel tableModel;
    protected Object[] deletedRow;
    protected int rowIndex;
    
    
    public JvRowsDelete(PassTableModel tableModel, Object[] deletedRow, int index) {
        this.tableModel = tableModel;
        this.deletedRow = deletedRow;
        this.rowIndex = index;
    }
    
    
    /**
     * returns the name of the undo/redo action to be displayed in the menu
     */
    @Override
    public String getPresentationName() {
        return "delete";
    }
    
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        tableModel.addRow( deletedRow, rowIndex, false );
    }
    
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        tableModel.deleteRow( rowIndex, false );
    }
}// end class
