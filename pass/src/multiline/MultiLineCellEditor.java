package multiline;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: lucy
 * Date: 06/03/13
 * Version: 0.1
 */
public class MultiLineCellEditor extends DefaultCellEditor implements TableCellEditor {

    public MultiLineCellEditor( final JTable table ) {
        super( new JTextField() );

        JScrollPane scrollPane;
        final JTextArea textArea;

        System.out.println( "clicks: " + getClickCountToStart() );
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

        scrollPane = new JScrollPane( textArea );
        scrollPane.setBorder( null );

        editorComponent = scrollPane;
        delegate = new DefaultCellEditor.EditorDelegate() {

            public void setValue( Object value ) {
                textArea.setText( ( value != null ) ? value.toString() : "" );
            }


            public Object getCellEditorValue() {
                return textArea.getText();
            }
        };

    }//end MultiLineCellEditor
}//end class


