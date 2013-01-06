package inc;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The
 * clipboard data format used by the adapter is compatible with the clipboard
 * format used by Excel. This provides for clipboard interoperability between
 * enabled JTables and Excel.
 */
public class PassExcelAdapter implements ActionListener {
    private PassTable table;
    
    
    /**
     * The Excel Adapter is constructed with a JTable on which it enables
     * Copy-Paste and acts as a Clipboard listener.
     */
    public PassExcelAdapter(PassTable myJTable) {
        table = myJTable;
        KeyStroke copy = KeyStroke.getKeyStroke( KeyEvent.VK_C,
                ActionEvent.CTRL_MASK, false );
        // Identifying the copy KeyStroke user can modify this
        // to copy on some other Key combination.
        KeyStroke paste = KeyStroke.getKeyStroke( KeyEvent.VK_V,
                ActionEvent.CTRL_MASK, false );
        // Identifying the Paste KeyStroke user can modify this
        // to copy on some other Key combination.
        table.registerKeyboardAction( this, "Copy", copy,
                JComponent.WHEN_FOCUSED );
        table.registerKeyboardAction( this, "Paste", paste,
                JComponent.WHEN_FOCUSED );
        // system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    
    
    /**
     * Public Accessor methods for the Table on which this adapter acts.
     */
    public JTable getJTable() {
        return table;
    }
    
    
    public void setJTable( PassTable jTable1 ) {
        this.table = jTable1;
    }
    
    
    /**
     * This method is activated on the Keystrokes we are listening to in this
     * implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed. Paste is done by aligning the upper
     * left corner of the selection with the 1st element in the current
     * selection of the JTable.
     */
    public void actionPerformed( ActionEvent e ) {
        
        StringSelection strSelection;
        
        if( e.getActionCommand().equals( "Copy" ) ){
            
            if( table.getSelectedRowCount() == 0
                    && table.getSelectedColumnCount() == 0 ){
                return;
                
            }else if( table.getSelectedRowCount() == 1
                    && table.getSelectedColumnCount() == 1 ){
                strSelection = new StringSelection( (String) table.getValueAt(
                        table.getSelectedRow(), table.getSelectedColumn() ) );
                
            }else{
                StringBuffer buffer = new StringBuffer();
                for( int row : table.getSelectedRows() ){
                    for( int col : table.getSelectedColumns() ){
                        buffer.append( table.getValueAt( row, col ) + "\t" );
                    }
                    buffer.append( "\n" );
                }
                strSelection = new StringSelection( buffer.toString() );
            }
            
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents( strSelection, strSelection );
            
        }else if( e.getActionCommand().equals( "Paste" ) ){
            
            int startRow, currentRow;
            startRow = currentRow = table.getSelectedRow();
            int startCol, currentCol;
            startCol = currentCol = table.getSelectedColumn();
            
            ArrayList<String> oldValues = new ArrayList<String>(), newValues = new ArrayList<String>();
            
            try{
                String clipboardContent = (String) ( Toolkit
                        .getDefaultToolkit().getSystemClipboard()
                        .getContents( this )
                        .getTransferData( DataFlavor.stringFlavor ) );
                
                // StringTokenizer stLines = new StringTokenizer(
                // clipboardContent, "\n" );
                //
                // while( stLines.hasMoreTokens()
                // && currentRow < table.getRowCount() ){
                //
                // StringTokenizer stCells = new StringTokenizer(
                // stLines.nextToken(), "\t" );
                //
                // while( stCells.hasMoreTokens()
                // && currentCol < table.getColumnCount() ){
                //
                // table.setValueAt( stCells.nextToken(), currentRow,
                // currentCol );
                //
                // currentCol++;
                // }// end while cols
                // currentCol = 0;
                // currentRow++;
                // }// end while lines
                //
                StringTokenizer stLines = new StringTokenizer(
                        clipboardContent, "\n" );
                
                for( int i = 0; stLines.hasMoreTokens(); i++ ){
                    StringTokenizer stCols = new StringTokenizer(
                            stLines.nextToken(), "\t" );
                    
                    for( int j = 0; stCols.hasMoreTokens(); j++ ){
                        if( startRow + i < table.getRowCount()
                                && startCol + j < table.getColumnCount() ){
                            oldValues.add( (String)table.getValueAt( startRow + i, startCol + j ) );
                            String newValue = stCols.nextToken();
                            ((PassTableModel)table.getModel()).setValueAt( newValue, startRow + i, startCol + j, false );
                            newValues.add(newValue);
                        }//end if
                    }//end for cols
                    
                }//end for lines
                
                
            }catch( HeadlessException e1 ){
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }catch( UnsupportedFlavorException e1 ){
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }catch( IOException e1 ){
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        // if( e.getActionCommand().compareTo( "Paste" ) == 0 ){
        // System.out.println( "Trying to Paste" );
        // int startRow = ( table.getSelectedRows() )[ 0 ];
        // int startCol = ( table.getSelectedColumns() )[ 0 ];
        // try{
        // String trstring = (String) ( system.getContents( this )
        // .getTransferData( DataFlavor.stringFlavor ) );
        // System.out.println( "String is:" + trstring );
        // StringTokenizer st1 = new StringTokenizer( trstring, "\n" );
        // for( int i = 0; st1.hasMoreTokens(); i++ ){
        // rowstring = st1.nextToken();
        // StringTokenizer st2 = new StringTokenizer( rowstring, "\t" );
        // for( int j = 0; st2.hasMoreTokens(); j++ ){
        // value = (String) st2.nextToken();
        // if( startRow + i < table.getRowCount()
        // && startCol + j < table.getColumnCount() )
        // table.setValueAt( value, startRow + i, startCol
        // + j );
        // System.out.println( "Putting " + value + "at row="
        // + startRow + i + "column=" + startCol + j );
        // }
        // }
        // }catch( Exception ex ){
        // ex.printStackTrace();
        // }
        // }
    }
}// end class