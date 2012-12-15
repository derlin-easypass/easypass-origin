package inc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class PassTable extends JTable {
    
    public PassTable(String[] columnNames, List<Object[]> data) {
        this( new PassTableModel( columnNames, data ) );
    }
    
    
    public PassTable(PassTableModel model) {
        super( model );
        // stops the editing when the user clicks on a button or else
        // so when the user clicks on delete rows for example, there is no
        // editing ghost cell hanging in the void !
        this.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
    }
    
    
    public void update( String[] colname, List<Object[]> data ) {
        this.setModel( new PassTableModel( colname, data ) );
        this.updateUI();
    }
    
    
    // TODO
    public Component prepareRenderer( TableCellRenderer renderer, int row,
            int column ) {
        JComponent c = (JComponent) super.prepareRenderer( renderer, row,
                column );
        
        if( !isRowSelected( row ) ){
            c.setBackground( row % 2 == 0 ? getBackground() : new Color( 250,
                    250, 250 ) );
            c.setForeground( Color.BLACK );
        }else{
            c.setBackground( new Color( 232, 242, 254 ) );
            c.setForeground( Color.BLACK);
            c.setFont( getFont().deriveFont( Font.BOLD ) );
            if( getSelectedRowCount() == 1 && getSelectedColumnCount() == 1
                    && isRowSelected( row ) && isColumnSelected( column ) ){
                c.setBackground( new Color( 184, 202, 238 ) );
            }
        }
        // else if(getSelectedRowCount() == 1 && getSelectedColumnCount() == 1
        // && isCellSelected( row, column )){
        // c.setBackground( new Color( 0, 250, 0 ) );
        // c.setBorder( BorderFactory.createLineBorder( Color.RED, 2 ) );
        //
        // }else{
        //
        // c.setFont( getFont().deriveFont( Font.BOLD ) );
        // if( getSelectedRowCount() == 1 && getSelectedColumnCount() == 1
        // && isRowSelected( row ) && isColumnSelected( column ) ){
        //
        // c.setBackground( new Color( 0, 0, 250 ) );
        //
        // }else{
        // System.out.println( "true" );
        // c.setBorder( BorderFactory.createLineBorder( Color.CYAN, 2 ) );
        // c.setBackground( new Color( 250, 0, 0 ) );
        // }
        // }
        //
        return c;
    }
    
    
    public TableCellRenderer getCellRenderer( int row, int column ) {
        if( column == 2 ){
            return new PasswordCellRenderer();
        }
        return super.getCellRenderer( row, column );
    }
    
    
    public Component prepareEditor( TableCellEditor editor, int row, int column ) {
        JComponent c = (JComponent) super.prepareEditor( editor, row, column );
        c.setFont( c.getFont().deriveFont( Font.BOLD ) );
        c.setBorder( BorderFactory.createLineBorder( new Color( 52, 153, 255 ), 1 ) );
        return c;
    }
    
    
    public void setStyle() {
        // try {
        // for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        // if ("Nimbus".equals(info.getName())) {
        // UIManager.setLookAndFeel(info.getClassName());
        // break;
        // }
        // }
        // } catch (Exception e) {
        // // If Nimbus is not available, you can set the GUI to another look
        // and feel.
        // }
        
        // this.setSelectionBackground(new Color(115, 164, 209));
        JTableHeader header = this.getTableHeader();
        
        header.setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
        
    }
    
    
    public void setColSizes( int[] sizes ) {
        
        TableColumn col;
        int colCount = this.getColumnModel().getColumnCount();
        
        if( colCount > sizes.length )
            colCount = sizes.length;
        
        for( int i = 0; i < colCount; i++ ){
            this.getColumnModel().getColumn( i ).setPreferredWidth( sizes[ i ] );
        }
        System.out.println( "col sizes set" );
    }
    
    
    // TODO
    public AbstractAction getDeleteRowsAction() {
        return new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                int[] selectedRows = getSelectedRows();
                System.out.println( "\ndeleteing rows:" );
                for( int i = 0; i < selectedRows.length; i++ ){
                    // row index minus i since the table size shrinks by 1
                    // everytime
                    ( (PassTableModel) getModel() ).deleteRow( selectedRows[ i ]
                            - i );
                }
            }
        };
        
    }
    
}// end MyTable
