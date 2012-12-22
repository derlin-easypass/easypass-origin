package inc;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.*;

/**
 * this class, alongside JvCellEdit, JvRowsEdit and PassTableModel, manages the undo/redo actions.
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 *
 */
class JvUndoManager extends UndoManager {

    private static final long serialVersionUID = -8283248203575191285L;
    protected Action undoAction;
	protected Action redoAction;

    /********************************************************************
     * constructor                   
    /********************************************************************/
	public JvUndoManager() {
		this.undoAction = new JvUndoAction(this);
		this.redoAction = new JvRedoAction(this);


		synchronizeActions(); // to set initial names
	}

    /********************************************************************
     * getters and setters                  
    /********************************************************************/
	
	public Action getUndoAction() {
		return undoAction;
	}

	public Action getRedoAction() {
		return redoAction;
	}

    /********************************************************************
     * override methods                  
    /********************************************************************/
	
	@Override
	public boolean addEdit(UndoableEdit edit) {
		try {
			return super.addEdit(edit);
		} finally {
			synchronizeActions();
		}
	}

	@Override
	protected void undoTo(UndoableEdit edit) throws CannotUndoException {
		try {
			super.undoTo(edit);
		} finally {
			synchronizeActions();
		}
	}

	@Override
	protected void redoTo(UndoableEdit edit) throws CannotRedoException {
		try {
			super.redoTo(edit);
		} finally {
			synchronizeActions();
		}
	}

	protected void synchronizeActions() {
		undoAction.setEnabled(canUndo());
		undoAction.putValue(Action.NAME, getUndoPresentationName());


		redoAction.setEnabled(canRedo());
		redoAction.putValue(Action.NAME, getRedoPresentationName());
	}
}//end class




/********************************************************************
 * Actions classes                
/********************************************************************/

class JvUndoAction extends AbstractAction {

    private static final long serialVersionUID = 2331596696022415927L;
    protected final UndoManager manager;

	public JvUndoAction(UndoManager manager) {
		this.manager = manager;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			manager.undo();
		} catch (CannotUndoException ex) {
			ex.printStackTrace();
		}
	}
}//end class

class JvRedoAction extends AbstractAction {

    private static final long serialVersionUID = -3169421200120733379L;
    protected final UndoManager manager;

	public JvRedoAction(UndoManager manager) {
		this.manager = manager;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			manager.redo();
		} catch (CannotRedoException ex) {
			ex.printStackTrace();
		}
	}
}//end class