package undo;

import table.PassTableModel;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * This class is used to record the paste actions in order to
 * implement undo/redo actions.
 * <p/>
 * See the UndoManager class, the PassTableModel paste method and the createPasteListener in the
 * gui
 * package for more information
 *
 * @author lucy linder
 * @date Dec 21, 2012
 */
public class JvPaste extends AbstractUndoableEdit {

    private static final long serialVersionUID = 3227903502002101373L;

    protected PassTableModel tableModel;
    protected String oldValues;
    protected String newValues;
    protected int startRow, startCol;


    public JvPaste( PassTableModel tableModel, String oldValues, String newValues, int startRow,
                    int startCol ) {
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
        this.tableModel.paste( this.oldValues, this.startRow, this.startCol, false );

    }


    @Override
    public void redo() throws CannotUndoException {
        super.redo();

        this.tableModel.paste( this.newValues, this.startRow, this.startCol, false );
    }
}
