package ch.derlin.easypass.multiline;

/**
 * User: lucy
 * Date: 05/03/13
 * Version: 0.1
 */

import ch.derlin.easypass.table.PassTable;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Multiline Table Cell Renderer.
 * xource : http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
 *
 */
public class MultiLineCellRenderer extends JTextArea
        implements TableCellRenderer {

    private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
    int counter = 0;

    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setMargin(new Insets(3,3,3,3));
    }

    public Component getTableCellRendererComponent (
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {


        if (value != null) {
            setText(value.toString());
        } else {
            setText("");
        }

        if(table.isRowSelected( row )){
            adjustRowHeight(table, row, column);
        }else if(table.getRowHeight( row ) != 30){
            table.setRowHeight( row, PassTable.DEFAULT_HEIGHT );
        }


        return this;

    }

    /**
     * Calculate the new preferred height for a given row, and sets the height on the ch.derlin.easypass.table.
     */
    public void adjustRowHeight( JTable table, int row, int column ) {
        //The trick to get this to work properly is to set the width of the column to the
        //textarea. The reason for this is that getPreferredSize(), without a width tries
        //to place all the text in one line. By setting the size with the with of the column,
        //getPreferredSize() returns the proper height which the row should have in
        //order to make room for the text.
        int cWidth = table.getTableHeader().getColumnModel().getColumn( column ).getWidth();
        setSize( new Dimension( cWidth, 1000 ) );
        int prefH = getPreferredSize().height;
        while( rowColHeight.size() <= row ) {
            rowColHeight.add( new ArrayList<Integer>( column ) );
        }
        List<Integer> colHeights = rowColHeight.get( row );
        while( colHeights.size() <= column ) {
            colHeights.add( 0 );
        }
        colHeights.set( column, prefH );
        int maxH = prefH;
        for( Integer colHeight : colHeights ) {
            if( colHeight > maxH ) {
                maxH = colHeight;
            }
        }
        if( maxH > PassTable.DEFAULT_HEIGHT && table.getRowHeight( row ) != maxH ) {
            table.setRowHeight( row, maxH );
        }
    }



    @Override
    public void setBackground( Color color ) {
        if( color instanceof ColorUIResource ) {
            color = new Color( color.getRGB() );
        }
        super.setBackground( color );
    }//setBackground


    @Override
    public void setForeground( Color color ) {
        if( color instanceof ColorUIResource ) {
            color = new Color( color.getRGB() );
        }
        super.setForeground( color );
    }//setBackground


}//end class
