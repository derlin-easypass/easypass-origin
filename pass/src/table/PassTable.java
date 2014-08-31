package table;

import multiline.MultiLineCellRenderer;
import multiline.PasswordMultiCellRenderer;
import multiline.TextAreaCellEditor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
public class PassTable extends JTable{

    private static final long serialVersionUID = -5878840678754267165L;
    private static final String PASS_COLUMN_NAME = "password";
    public final static int DEFAULT_HEIGHT = 30;


    private TableRowSorter<AbstractTableModel> sorter; // used for the search bar
    // ("find")


    /**
     * *****************************************************************
     * constructors and update /
     * ******************************************************************
     */

    public PassTable( AbstractTableModel model ){
        super( model );
        // stops the editing when the user clicks on a button or else
        // so when the user clicks on delete rows for example, there is no
        // editing ghost cell hanging in the void !

        putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
        setCellSelectionEnabled( true );
        setAutoCreateRowSorter( true );
        setFillsViewportHeight( true );
        setRowHeight( DEFAULT_HEIGHT );
        setSurrendersFocusOnKeystroke( true );
        setDefaultRenderer( Object.class, new MultiLineCellRenderer() );
        setDefaultEditor( Object.class, new TextAreaCellEditor( this ) );//MultiLineCellEditorTry
        // (this) );
        if( this.getColumn( PASS_COLUMN_NAME ) != null ){
            setPasswordColumns( PASS_COLUMN_NAME );
        }

        sorter = new TableRowSorter<>( model );
        setRowSorter( sorter );
    }// end constructor


    /**
     * Implements the search bar logic :updates the row filter regular
     * expression from the expression in the text box.
     */
    public void setTableFilter( String text ){
        RowFilter<AbstractTableModel, Object> rf;
        ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<>();

        try{
            String[] textArray = text.split( " " );

            for( String aTextArray : textArray ){
                rfs.add( RowFilter.regexFilter( "(?i)" + aTextArray, 0, 1, 2, 3, 4 ) );
            }//end for

            rf = RowFilter.andFilter( rfs );

        }catch( java.util.regex.PatternSyntaxException e ){
            return;
        }

        sorter.setRowFilter( rf );
    }
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
    public void setColSizes( int[] sizes ){

        int colCount = this.getColumnModel().getColumnCount();

        if( colCount > sizes.length ) colCount = sizes.length;

        for( int i = 0; i < colCount; i++ ){
            if( sizes[ i ] != 0 ){
                this.getColumnModel().getColumn( i ).setPreferredWidth( sizes[ i ] );
            }
        }
        System.out.println( "col sizes set" );
    }// end setColSizes


    public void setPasswordColumns( String... names ){

        PasswordMultiCellRenderer renderer;

        renderer = new PasswordMultiCellRenderer();
        //        }
        for( String name : names ){
            if( this.getColumn( name ) != null ){
                this.getColumn( name ).setCellRenderer( renderer );
            }
        }//end for

    }//end setPasswordColumns


    /** this method customizes the global appearance of the jtable. */
    public void setStyle(){
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
    public Component prepareRenderer( TableCellRenderer renderer, int row, int column ){
        JComponent c = ( JComponent ) super.prepareRenderer( renderer, row, column );

        if( isCellSelected( row, column ) ){
            c.setBackground( UIManager.getColor( "Table.selectionBackground" ) );
            c.setForeground( UIManager.getColor( "Table.selectionForeground" ) );
        }else{
            // sets a light gray background to every non-selected odd row
            c.setBackground( row % 2 == 0 ? Color.WHITE : new Color( 240, 240, 240 ) );
            c.setForeground( Color.BLACK );
            c.setFont( getFont().deriveFont( isRowSelected( row ) ? Font.BOLD : Font.PLAIN ) );
        }

        return c;
    }//end prepareRenderer


    @Override
    public String getToolTipText( MouseEvent event ){
        if( this.getModel() instanceof PassTableModel ){
            PassTableModel model = ( PassTableModel ) this.getModel();
            return model.getTooltipForRow( this.rowAtPoint( event.getPoint() ) );
        }else{
            return super.getToolTipText( event );
        }
    }


    /**
     * *****************************************************************
     * utilities
     * ******************************************************************
     */


    public void stopEditing(){
        if( this.isEditing() ){
            this.getCellEditor().stopCellEditing();
        }
    }//end stopEditing


    public int getViewRowCount(){
        return sorter.getViewRowCount();
    }//end getViewRowCount


    /**
     * returns an array of int representing the model indexes corresponding to the given tablerow
     * indexes
     *
     * @return an array of row indexes converted to model indexes
     */
    public int[] getSelectedRowsConvertedToModel(){

        int[] rows = this.getSelectedRows();
        int[] convertedRows = new int[ rows.length ];

        for( int i = 0; i < rows.length; i++ ){
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
    public int[] getSelectedColumnsConvertedToModel(){

        int[] cols = this.getSelectedColumns();
        int[] convertedCols = new int[ cols.length ];

        for( int i = 0; i < cols.length; i++ ){
            convertedCols[ i ] = this.convertColumnIndexToModel( cols[ i ] );
        }

        return convertedCols;
    }//end getSelectedColumnsConvertedToModel
}// end MyTable
