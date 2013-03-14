package table;

import multiline.MultiLineCellEditor;
import multiline.MultiLineCellRenderer;
import multiline.PasswordMultiCellRenderer;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * This class is an extension of the basic JTable, adapted in order to use a
 * PassTableModel only.
 * <p/>
 * The following methods are merely modifying the rendering/appearance of the
 * table. All the logic is implemented in the PassTableModel class.
 *
 * @author Lucy Linder
 * @date Dec 21, 2012
 */
public class PassTable extends JTable {

    private static final long serialVersionUID = -5878840678754267165L;
    private static final String PASS_COLUMN_NAME = "password";
    public final static int DEFAULT_HEIGHT = 30;

    /**
     * *****************************************************************
     * constructors and update /
     * ******************************************************************
     */

    public PassTable( PassTableModel model ) {
        super( model );
        // stops the editing when the user clicks on a button or else
        // so when the user clicks on delete rows for example, there is no
        // editing ghost cell hanging in the void !

        putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
        setCellSelectionEnabled( true );
        setAutoCreateRowSorter( true );
        setFillsViewportHeight( true );
        setRowHeight( DEFAULT_HEIGHT );
        setDefaultRenderer( Object.class, new MultiLineCellRenderer() );
        setDefaultEditor( Object.class, new MultiLineCellEditor(this) );
        if( this.getColumn( PASS_COLUMN_NAME ) != null ) {
            setPasswordColumns( PASS_COLUMN_NAME );
        }


    }// end constructor


    public PassTable( String[] columnNames, List<Object[]> data ) {
        this( new PassTableModel( columnNames, data ) );
    }


    /**
     * replaces the current PassTableModel by a new one passed as a parameter
     *
     * @param colname an array containing the headers
     * @param data
     */
    public void update( String[] colname, List<Object[]> data ) {
        this.setModel( new PassTableModel( colname, data ) );
        this.updateUI();
    }// end update


//    @Override
//    public void setRowHeight( int rowHeight ) {
//        super.setRowHeight( rowHeight + 4 );    //To change body of overridden methods use
//        // File |
//        // Settings | File Templates.
//    }
//
//
//    @Override
//    public int getRowHeight() {
//        return super.getRowHeight() - 4;    //To change body of overridden methods use File |
//        // Settings | File Templates.
//    }

    /********************************************************************
     * styles and appearances management methods /
     ********************************************************************/

    /**
     * this method enables to set all the column sizes at once. If the the array
     * given in parameter is too long, only the first indexes will be taken into
     * account
     *
     * @param sizes the sizes of the columns, in increasing order (from col 0 to
     *              n)
     */
    public void setColSizes( int[] sizes ) {

        int colCount = this.getColumnModel().getColumnCount();

        if( colCount > sizes.length ) colCount = sizes.length;

        for( int i = 0; i < colCount; i++ ) {
            if( sizes[ i ] != 0 ) {
                this.getColumnModel().getColumn( i ).setPreferredWidth( sizes[ i ] );
            }
        }
        System.out.println( "col sizes set" );
    }// end setColSizes


    public void setPasswordColumns( String... names ) {

        PasswordMultiCellRenderer renderer;

        //        if( this.getColumn( PASS_COLUMN_NAME ) != null && this.getColumn(
        // PASS_COLUMN_NAME )
        //                .getCellRenderer() instanceof PasswordMultiCellRenderer ) {
        //            renderer = ( PasswordMultiCellRenderer ) this.getColumn( PASS_COLUMN_NAME )
        //                    .getCellRenderer();
        //        } else {
        renderer = new PasswordMultiCellRenderer();
        //        }
        for( String name : names ) {
            if( this.getColumn( name ) != null ) {
                this.getColumn( name ).setCellRenderer( renderer );
            }
        }//end for

    }//end setPasswordColumns


    /**
     * this method customizes the global appearance of the jtable.
     */
    public void setStyle() {
        // TODO
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
     * <p/>
     * For currently edited cell rendering, see the prepareEditor method
     */
    @Override
    public Component prepareRenderer( TableCellRenderer renderer, int row, int column ) {
        JComponent c = ( JComponent ) super.prepareRenderer( renderer, row, column );

        // sets a light gray background to every non-selected odd row
        if( !isCellSelected( row, column ) ) {
            c.setBackground( row % 2 == 0 ? Color.WHITE : new Color( 240, 240, 240 ) );
            c.setForeground( Color.BLACK );
        } else {
            c.setFont( getFont().deriveFont( Font.BOLD ) );
            c.setBackground( UIManager.getColor( "Table.selectionBackground" ) );
            c.setForeground( UIManager.getColor( "Table.selectionForeground" ) );
        }

        return c;
    }//end prepareRenderer


    /**
     * this method sets a special renderer for the password column, so its
     * content is replaced by asterisks when the cell is not in edition mode
     */
    //    @Override
    //    public TableCellRenderer getCellRenderer( int row, int column ) {
    //        if( this.getColumnName( column ).equals( PASS_COLUMN_NAME ) ) {
    //            return new PasswordCellRenderer();
    //        }
    //        //		return super.getCellRenderer(row, column);
    //        return new MultiLineCellRenderer();
    //        return new TextAreaRenderer();
    //    }

    /**
     * this method customs the rendering of currently edited cells. It mainly
     * sets the text in bold and draws a blue border around it
     */
    // @Override
    // public Component prepareEditor( TableCellEditor editor, int row, int
    // column ) {
    // JComponent c = (JComponent) super.prepareEditor( editor, row, column );
    // c.setFont( c.getFont().deriveFont( Font.BOLD ) );
    // c.setBorder( BorderFactory.createLineBorder( new Color( 52, 153, 255 ),
    // 1 ) );
    // return c;
    // }


    /**
     * *****************************************************************
     * utilities
     * ******************************************************************
     */

    /**
     * returns an array of int representing the model indexes corresponding to the given tablerow
     * indexes
     *
     * @return an array of row indexes converted to model indexes
     */
    public int[] getSelectedRowsConvertedToModel() {

        int[] rows = this.getSelectedRows();
        int[] convertedRows = new int[ rows.length ];

        for( int i = 0; i < rows.length; i++ ) {
            convertedRows[ i ] = this.convertRowIndexToModel( rows[ i ] );
        }

        return convertedRows;
    }//end getSelectedRowsConvertedToModel


    /**
     * returns an array of int representing the model indexes corresponding to the given table
     * columns  indexes
     *
     * @return an array of column indexes converted to model indexes
     */
    public int[] getSelectedColumnsConvertedToModel() {

        int[] cols = this.getSelectedColumns();
        int[] convertedCols = new int[ cols.length ];

        for( int i = 0; i < cols.length; i++ ) {
            convertedCols[ i ] = this.convertColumnIndexToModel( cols[ i ] );
        }

        return convertedCols;
    }//end getSelectedColumnsConvertedToModel
}// end MyTable
