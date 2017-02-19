package ch.derlin.easypass.undo;

import ch.derlin.easypass.table.PassTableModel;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * This class is used to record the cut actions in order to
 * implement ch.derlin.easypass.undo/redo actions.
 * <br />
 * See the UndoManager class, the PassTableModel cut method and the createCutListener in the ch.derlin.easypass.gui
 * package for more information
 *
 * @author lucy linder
 * @date Dec 21, 2012
 */
public class JvCut extends AbstractUndoableEdit {

    private static final long serialVersionUID = 3227903502002101373L;

    protected PassTableModel tableModel;
    protected Object[] oldValues;
    protected int[] selectedRows, selectedCols;


    public JvCut( PassTableModel tableModel, Object[] oldValues, int[] selectedRows,
                  int[] selectedCols ) {
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

        for( int row : selectedRows ) {
            for( int col : selectedCols ) {
                this.tableModel.setValueAt( this.oldValues[ index++ ], row, col, false );
            }
        }
    }


    @Override
    public void redo() throws CannotUndoException {
        super.redo();

        for( int row : selectedRows ) {
            for( int col : selectedCols ) {
                this.tableModel.setValueAt( "\0", row, col, false );
            }
        }
    }
}
