package manager;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.awt.event.ActionEvent;

/**
 * this class, alongside JvCellEdit, JvRowsEdit and PassTableModel, manages the undo/redo actions.
 *
 * @author Lucy Linder
 * @date Dec 21, 2012
 */
public class UndoManager extends javax.swing.undo.UndoManager {

    private static final long serialVersionUID = -8283248203575191285L;
    protected JTable table;
    protected Action undoAction;
    protected Action redoAction;


    /**
     * *****************************************************************
     * constructor
     * /*******************************************************************
     */
    public UndoManager( JTable table ) {
        this.undoAction = new JvUndoAction( this );
        this.redoAction = new JvRedoAction( this );
        this.table = table;

        synchronizeActions(); // to set initial names
    }


    /**
     * *****************************************************************
     * getters and setters
     * /*******************************************************************
     */

    public Action getUndoAction() {
        return undoAction;
    }


    public Action getRedoAction() {
        return redoAction;
    }


    public JTable getTable() {
        return table;
    }


    public void setTable( JTable table ) {
        this.table = table;
    }


    /**
     * *****************************************************************
     * override methods
     * /*******************************************************************
     */

    @Override
    public boolean addEdit( UndoableEdit edit ) {
        try {
            return super.addEdit( edit );
        } finally {
            synchronizeActions();
        }
    }


    @Override
    protected void undoTo( UndoableEdit edit ) throws CannotUndoException {
        try {
            super.undoTo( edit );
        } finally {
            synchronizeActions();
        }
    }


    @Override
    protected void redoTo( UndoableEdit edit ) throws CannotRedoException {
        try {
            super.redoTo( edit );
        } finally {
            synchronizeActions();
        }
    }


    protected void synchronizeActions() {
        undoAction.setEnabled( canUndo() );
        undoAction.putValue( Action.NAME, getUndoPresentationName() );


        redoAction.setEnabled( canRedo() );
        redoAction.putValue( Action.NAME, getRedoPresentationName() );
    }
}//end class


/**
 * *****************************************************************
 * Actions classes
 * /*******************************************************************
 */

class JvUndoAction extends AbstractAction {

    private static final long serialVersionUID = 2331596696022415927L;
    protected final javax.swing.undo.UndoManager manager;


    public JvUndoAction( javax.swing.undo.UndoManager manager ) {
        this.manager = manager;
    }


    public void actionPerformed( ActionEvent e ) {
        try {
            if( manager instanceof UndoManager && ( ( UndoManager ) manager ).getTable()
                    .isEditing() ) {
                ( ( UndoManager ) manager ).getTable().getCellEditor().stopCellEditing();
            }
            manager.undo();
        } catch( CannotUndoException ex ) {
            ex.printStackTrace();
        }
    }
}//end class

class JvRedoAction extends AbstractAction {

    private static final long serialVersionUID = -3169421200120733379L;
    protected final javax.swing.undo.UndoManager manager;


    public JvRedoAction( javax.swing.undo.UndoManager manager ) {
        this.manager = manager;
    }


    public void actionPerformed( ActionEvent e ) {
        try {
            if( manager instanceof UndoManager && ( ( UndoManager ) manager ).getTable()
                    .isEditing() ) {
                ( ( UndoManager ) manager ).getTable().getCellEditor().stopCellEditing();
            }
            manager.redo();
        } catch( CannotRedoException ex ) {
            ex.printStackTrace();
        }
    }
}//end class