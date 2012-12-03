package inc;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.*;

class JvUndoManager extends UndoManager {
	protected Action undoAction;
	protected Action redoAction;

	public JvUndoManager() {
		this.undoAction = new JvUndoAction(this);
		this.redoAction = new JvRedoAction(this);

		synchronizeActions(); // to set initial names
	}

	public Action getUndoAction() {
		return undoAction;
	}

	public Action getRedoAction() {
		return redoAction;
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		try {
			return super.addEdit(anEdit);
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
}

class JvUndoAction extends AbstractAction {
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
}

class JvRedoAction extends AbstractAction {
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
}