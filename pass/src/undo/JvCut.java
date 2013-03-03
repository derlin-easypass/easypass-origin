package undo;

import table.PassTableModel;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * This class is used to record the cut actions in order to
 * implement undo/redo actions.
 * 
 * See the UndoManager class, the PassTable setKeyBindings method and the
 * PassTableModel cut method for more informations
 * 
 * @author lucy linder
 * @date Dec 21, 2012
 * 
 */
public class JvCut extends AbstractUndoableEdit {
    
    private static final long serialVersionUID = 3227903502002101373L;
    
    protected PassTableModel tableModel;
    protected Object[] oldValues;
    protected int[] selectedRows, selectedCols;
    
    
    public JvCut(PassTableModel tableModel, Object[] oldValues,
            int[] selectedRows, int[] selectedCols) {
        this.tableModel = tableModel;
        this.oldValues = oldValues;
        this.selectedRows = selectedRows;
        this.selectedCols = selectedCols;
        
    }
    
    
    @Override
    public String getPresentationName() {
        return "cut";
    }
    
    
    @Override
    public void undo() throws CannotUndoException {
        
        super.undo();
        
        int index = 0;
        
        for(int row : selectedRows){
            for(int col : selectedCols ){
                this.tableModel.setValueAt( this.oldValues[index++], row, col, false );
            }
        }
    }
    
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        
        for(int row : selectedRows){
            for(int col : selectedCols ){
                this.tableModel.setValueAt( "\0", row, col, false );
            }
        }
    }
}
