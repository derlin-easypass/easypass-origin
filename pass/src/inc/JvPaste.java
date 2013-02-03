package inc;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * This class is used to record the paste actions in order to 
 * implement undo/redo actions.
 * 
 * See the UndoManager class, the PassTable setKeyBindings method and the
 * PassTableModel paste method for more informations
 * 
 * @author lucy linder
 * @date Dec 21, 2012
 * 
 */
class JvPaste extends AbstractUndoableEdit {
    
    private static final long serialVersionUID = 3227903502002101373L;
    
    protected PassTableModel tableModel;
    protected String oldValues;
    protected String newValues;
    protected int startRow, startCol;
    
    
    public JvPaste(PassTableModel tableModel, String oldValues,
            String newValues, int startRow, int startCol) {
        this.tableModel = tableModel;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.startRow = startRow;
        this.startCol = startCol;
        
    }
    
    
    @Override
    public String getPresentationName() {
        return "paste";
    }
    
    
    @Override
    public void undo() throws CannotUndoException {
        
        super.undo();
        this.tableModel.paste( this.oldValues, this.startRow, this.startCol,
                false );
        
    }
    
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        
        this.tableModel.paste( this.newValues, this.startRow, this.startCol,
                false );
    }
}