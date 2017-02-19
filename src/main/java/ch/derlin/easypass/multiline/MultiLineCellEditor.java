package ch.derlin.easypass.multiline;


import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: lucy
 * Date: 06/03/13
 * Version: 0.1
 */
public class MultiLineCellEditor extends DefaultCellEditor {

    JTextArea textArea;
    JScrollPane scrollPane;


    public MultiLineCellEditor( final JTable table ) {
        super( new JTextField() );
        getComponent().setName( "Table.editor" );
        setClickCountToStart( 2 );

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
        } );

        scrollPane = new JScrollPane();
        scrollPane.setViewportView( textArea );
        scrollPane.setBorder( null );
        editorComponent = scrollPane;

    }//end MultiLineCellEditor


    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
                                                  int row, int column ) {
        this.setValue( value );
        scrollPane.setBorder( new LineBorder( Color.black ) );
        return this.scrollPane;
    }


    public void setValue( Object value ) {
        textArea.setText( ( value != null ) ? value.toString() : "" );
    }


    public Object getCellEditorValue() {
        return this.textArea.getText();
    }
}//end class


