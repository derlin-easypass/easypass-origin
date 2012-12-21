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

/**
 * This class is an extension of the basic JTable, adapted in order to use a
 * PassTableModel only.
 * 
 * The following methods are merely modifying the rendering/appearance of the
 * table. All the logic is implemented in the PassTableModel class.
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 * 
 */
public class PassTable extends JTable {
    
    /********************************************************************
     * constructors and update /
     ********************************************************************/
    
    public PassTable(PassTableModel model) {
        super( model );
        // stops the editing when the user clicks on a button or else
        // so when the user clicks on delete rows for example, there is no
        // editing ghost cell hanging in the void !
        this.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
    }// end constructor
    
    
    public PassTable(String[] columnNames, List<Object[]> data) {
        this( new PassTableModel( columnNames, data ) );
    }
    
    
    /**
     * replaces the current PassTableModel by a new one passed as a parameter
     * 
     * @param colname
     *            an array containing the headers
     * @param data
     */
    public void update( String[] colname, List<Object[]> data ) {
        this.setModel( new PassTableModel( colname, data ) );
        this.updateUI();
    }// end update
    
    
    /********************************************************************
     * styles and appearances managements methods /
     ********************************************************************/
    
    /**
     * this method enables to set all the column sizes at once. If the the array
     * given in parameter is too long, only the first indexes will be taken into
     * account
     * 
     * @param sizes
     *            the sizes of the columns, in increasing order (from col 0 to
     *            n)
     */
    public void setColSizes( int[] sizes ) {
        
        TableColumn col;
        int colCount = this.getColumnModel().getColumnCount();
        
        if( colCount > sizes.length )
            colCount = sizes.length;
        
        for( int i = 0; i < colCount; i++ ){
            this.getColumnModel().getColumn( i ).setPreferredWidth( sizes[ i ] );
        }
        System.out.println( "col sizes set" );
    }// end setColSizes
    
    
    /**
     * this method customizes the global appearance of the jtable.
     */
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
    
    
    /**
     * this method is called whenever the table needs to be redrawn. It customs
     * a little bit the appearance of the non-edited cells : colors in light
     * gray every odd row, sets the text of selected rows in bold and draws a
     * blue border around the cell which has the focus.
     * 
     * For currently edited cell rendering, see the prepareEditor method
     */
    @Override
    public Component prepareRenderer( TableCellRenderer renderer, int row,
            int column ) {
        JComponent c = (JComponent) super.prepareRenderer( renderer, row,
                column );
        
        // sets a light gray background to every non-selected odd row
        if( !isRowSelected( row ) ){
            c.setBackground( row % 2 == 0 ? getBackground() : new Color( 250,
                    250, 250 ) );
            c.setForeground( Color.BLACK );
            
            // sets the text of selected rows in bold and draws a blue border
            // around the selected/focused cell
        }else{
            c.setBackground( new Color( 232, 242, 254 ) );
            c.setForeground( Color.BLACK );
            c.setFont( getFont().deriveFont( Font.BOLD ) );
            if( getSelectedRowCount() == 1 && getSelectedColumnCount() == 1
                    && isRowSelected( row ) && isColumnSelected( column ) ){
                c.setBackground( new Color( 184, 202, 238 ) );
            }
        }
        return c;
    }
    
    
    /**
     * this method sets a special renderer for the password column, so its
     * content is replaced by asterisks when the cell is not in edition mode
     */
    @Override
    public TableCellRenderer getCellRenderer( int row, int column ) {
        if( column == 2 ){
            return new PasswordCellRenderer();
        }
        return super.getCellRenderer( row, column );
    }
    
    
    /**
     * this method customs the rendering of currently edited cells. It mainly
     * sets the text in bold and draws a blue border around it
     */
    @Override
    public Component prepareEditor( TableCellEditor editor, int row, int column ) {
        JComponent c = (JComponent) super.prepareEditor( editor, row, column );
        c.setFont( c.getFont().deriveFont( Font.BOLD ) );
        c.setBorder( BorderFactory.createLineBorder( new Color( 52, 153, 255 ),
                1 ) );
        return c;
    }
    
}// end MyTable
