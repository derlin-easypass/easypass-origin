package ch.derlin.easypass.multiline;


import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

/**
 * User: lucy
 * Date: 06/03/13
 * Version: 0.1
 */
public class MultiLineCellEditorTry extends JScrollPane implements TableCellEditor {

    JTextArea textArea = new JTextArea();
    private String originalValue;

    private boolean editing;
    private Vector<CellEditorListener> listeners;
    private static final int CLICK_COUNT_TO_START = 2;


    public MultiLineCellEditorTry( final JTable table ) {
        super();
        setName( "Table.editor" );

        textArea = new JTextArea();
        textArea.setWrapStyleWord( true );
        textArea.setLineWrap( true );
        textArea.setMargin( new Insets( 3, 3, 3, 3 ) );

        //makes the tab give focus to the next cell
        //since by default, the textarea will prints the tab
        textArea.addKeyListener( new KeyAdapter() {
            @Override
            public void keyPressed( KeyEvent e ) {
                if( e.getKeyCode() == KeyEvent.VK_TAB ) {

                    //calculates the next cell coordinates
                    int nextRow, nextCol;
                    //next cell is column + 1
                    nextCol = table.getEditingColumn() + 1;
                    nextRow = table.getEditingRow();

                    //if the current edited cell is the last one, increments row index
                    if( nextCol == table.getColumnCount() ) {
                        nextCol = 0;
                        nextRow++;
                        //if next row does not exist, goes back to the first one,
                        if( nextRow == table.getRowCount() ) {
                            nextRow = 0;
                        }
                    }
                    stopCellEditing();
                    table.changeSelection( nextRow, nextCol, false, false );
                }
            }
        } ); // */

        setViewportView( textArea );
        setBorder( null );
        //        delegate = new DefaultCellEditor.EditorDelegate() {
        //
        //            public void setValue( Object value ) {
        //                this.setValue( value );
        //            }
        //        };

    }//end MultiLineCellEditor


    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
                                                  int row, int column ) {
        this.setValue( value );
        originalValue = String.valueOf( value );
        return this;
    }


    public void setValue( Object value ) {
        textArea.setText( ( value != null ) ? value.toString() : "" );
    }


    public Object getCellEditorValue() {
        return this.textArea.getText();
    }


    @Override
    public boolean isCellEditable( EventObject anEvent ) {
        return !( anEvent instanceof MouseEvent ) || ( ( MouseEvent ) anEvent ).getClickCount()
                >= CLICK_COUNT_TO_START;
    }


    @Override
    public boolean shouldSelectCell( EventObject anEvent ) {
        return true;
    }


    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        editing = false;
        return true;
    }


    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
        editing = false;
    }


    public void addCellEditorListener( CellEditorListener cel ) {
        if( listeners == null ) listeners = new Vector<CellEditorListener>();
        listeners.addElement( cel );
    }


    public void removeCellEditorListener( CellEditorListener cel ) {
        listeners.removeElement( cel );
    }


    protected void fireEditingCanceled() {
        setValue( originalValue );
        if( listeners == null ) return;
        ChangeEvent ce = new ChangeEvent( this );
        for( int i = listeners.size() - 1; i >= 0; i-- ) {
            ( ( CellEditorListener ) listeners.elementAt( i ) ).editingCanceled( ce );
        }
    }


    protected void fireEditingStopped() {
        if( listeners == null ) return;
        ChangeEvent ce = new ChangeEvent( this );
        for( int i = listeners.size() - 1; i >= 0; i-- ) {
            ( ( CellEditorListener ) listeners.elementAt( i ) ).editingStopped( ce );
        }
    }

}//end class


