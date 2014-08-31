package multiline;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * User: lucy
 * Date: 17/08/13
 * Version: 0.1
 */
public class TextAreaCellEditor implements TableCellEditor {
    private final JScrollPane scroll;
    private JTextArea textArea = new JTextArea();


    public TextAreaCellEditor( final JTable table) {
        scroll = new JScrollPane( textArea );
        scroll.setBorder( BorderFactory.createEmptyBorder() );
        //scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        textArea.setLineWrap( true );
        textArea.setBorder( BorderFactory.createEmptyBorder( 1, 5, 1, 5 ) );
        KeyStroke enter = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_MASK );
        textArea.getInputMap( JComponent.WHEN_FOCUSED ).put( enter, new AbstractAction() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                stopCellEditing();
            }
        } );

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
        } );
    }


    @Override
    public Object getCellEditorValue() {
        return textArea.getText();
    }


    @Override
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
                                                  int row, int column ) {
        textArea.setFont( table.getFont() );
        textArea.setText( ( value != null ) ? value.toString() : "" );
        EventQueue.invokeLater( new Runnable() {
            @Override
            public void run() {
                //textArea.setCaretPosition( textArea.getText().length() );
                textArea.requestFocusInWindow();
            }
        } );
        return scroll;
    }


    @Override
    public boolean isCellEditable( final EventObject e ) {
        if( e instanceof MouseEvent ) {
            return ( ( MouseEvent ) e ).getClickCount() >= 2;
        }
        EventQueue.invokeLater( new Runnable() {
            @Override
            public void run() {
                if( e instanceof KeyEvent ) {
                    KeyEvent ke = ( KeyEvent ) e;
                    char kc = ke.getKeyChar();
                    if( Character.isUnicodeIdentifierStart( kc ) ) {
                        textArea.setText( textArea.getText() + kc );
                        System.out.println( "3. invokeLater: isCellEditable" );
                    }
                }
            }
        } );
        return true;
    }


    //Copid from AbstractCellEditor
    protected EventListenerList listenerList = new EventListenerList();
    transient protected ChangeEvent changeEvent = null;


    @Override
    public boolean shouldSelectCell( EventObject e ) {
        return true;
    }


    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }


    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }


    @Override
    public void addCellEditorListener( CellEditorListener l ) {
        listenerList.add( CellEditorListener.class, l );
    }


    @Override
    public void removeCellEditorListener( CellEditorListener l ) {
        listenerList.remove( CellEditorListener.class, l );
    }


    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners( CellEditorListener.class );
    }


    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if( listeners[ i ] == CellEditorListener.class ) {
                // Lazily create the event:
                if( changeEvent == null ) changeEvent = new ChangeEvent( this );
                ( ( CellEditorListener ) listeners[ i + 1 ] ).editingStopped( changeEvent );
            }
        }
    }


    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if( listeners[ i ] == CellEditorListener.class ) {
                // Lazily create the event:
                if( changeEvent == null ) changeEvent = new ChangeEvent( this );
                ( ( CellEditorListener ) listeners[ i + 1 ] ).editingCanceled( changeEvent );
            }
        }
    }
}
