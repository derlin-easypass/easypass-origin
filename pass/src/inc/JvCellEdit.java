package inc;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * This class is used to record the modifications made by the user in order to
 * implement undo/redo actions.
 * 
 * See the UndoManager class and the PassTableModel setValue method for more
 * informations
 * 
 * @author lucy linder
 * @date Dec 21, 2012
 *
 */
class JvCellEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = 3227903502002101373L;
    
    protected PassTableModel tableModel;
    protected Object oldValue;
    protected Object newValue;
    protected int row;
    protected int column;
    
    
    public JvCellEdit(PassTableModel tableModel, Object oldValue,
            Object newValue, int row, int column) {
        this.tableModel = tableModel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.row = row;
        this.column = column;
    }
    
    
    @Override
    public String getPresentationName() {
        return "edit";
    }
    
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        
        tableModel.setValueAt( oldValue, row, column, false );
    }
    
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        
        tableModel.setValueAt( newValue, row, column, false );
        
    }
}
