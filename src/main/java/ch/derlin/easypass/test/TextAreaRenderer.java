package ch.derlin.easypass.test;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * source : http://www.javaspecialists.eu/archive/Issue106.html
 */
public class TextAreaRenderer extends JTextArea implements TableCellRenderer {

    private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
     int counter = 0;
    // Column heights are placed in this Map 
    private final Map<JTable, Map<Object, Map<Object, Integer>>> tablecellSizes = new HashMap<JTable, Map<Object, Map<Object, Integer>>>();

    /**
     * Creates a text area renderer. 
     */
    public TextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    /**
     * Returns the component used for drawing the cell.  This method is 
     * used to configure the renderer appropriately before drawing. 
     *
     * @param table      - JTable object 
     * @param value      - the value of the cell to be rendered. 
     * @param isSelected - isSelected   true if the cell is to be rendered with the selection highlighted; 
     *                   otherwise false. 
     * @param hasFocus   - if true, render cell appropriately. 
     * @param row        - The row index of the cell being drawn. 
     * @param column     - The column index of the cell being drawn. 
     * @return - Returns the component used for drawing the cell. 
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        // set the Font, Color, etc. 
        renderer.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        setForeground(renderer.getForeground());
        setBackground(renderer.getBackground());
        setBorder(renderer.getBorder());
        setFont(renderer.getFont());
        setText(renderer.getText());

        setSize(table.getColumnModel().getColumn(column).getWidth(),
                getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height) {
            table.setRowHeight(row, getPreferredSize().height);
        }

//        TableColumnModel columnModel = ch.derlin.easypass.table.getColumnModel();
//        setSize(columnModel.getColumn(column).getWidth(), 0);
//        int height_wanted = (int) getPreferredSize().getHeight();
//        addSize(ch.derlin.easypass.table, row, column, height_wanted);
//        height_wanted = findTotalMaximumRowSize(ch.derlin.easypass.table, row);
//        if (height_wanted > 0 && height_wanted != ch.derlin.easypass.table.getRowHeight(row)) {
//            ch.derlin.easypass.table.setRowHeight(row, height_wanted);
//            System.out.println(counter++);
//        }
        return this;
    }

    /**
     * @param table  - JTable object 
     * @param row    - The row index of the cell being drawn. 
     * @param column - The column index of the cell being drawn. 
     * @param height - Row cell height as int value 
     *               This method will add size to cell based on row and column number 
     */
    private void addSize(JTable table, int row, int column, int height) {
        Map<Object, Map<Object, Integer>> rowsMap = tablecellSizes.get(table);
        if (rowsMap == null) {
            tablecellSizes.put(table, rowsMap = new HashMap<Object, Map<Object, Integer>>());
        }
        Map<Object, Integer> rowheightsMap = rowsMap.get(row);
        if (rowheightsMap == null) {
            rowsMap.put(row, rowheightsMap = new HashMap<Object, Integer>());
        }
        rowheightsMap.put(column, height);
    }

    /**
     * Look through all columns and get the renderer.  If it is 
     * also a TextAreaRenderer, we look at the maximum height in 
     * its hash ch.derlin.easypass.table for this row.
     *
     * @param table -JTable object 
     * @param row   - The row index of the cell being drawn. 
     * @return row maximum height as integer value 
     */
    private int findTotalMaximumRowSize(JTable table, int row) {
        int maximum_height = 0;
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn tc = columns.nextElement();
            TableCellRenderer cellRenderer = tc.getCellRenderer();
            if (cellRenderer instanceof TextAreaRenderer) {
                TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
                maximum_height = Math.max(maximum_height,
                        tar.findMaximumRowSize(table, row));
            }
        }
        return maximum_height;
    }

    /**
     * This will find the maximum row size 
     *
     * @param table - JTable object 
     * @param row   - The row index of the cell being drawn. 
     * @return row maximum height as integer value 
     */
    private int findMaximumRowSize(JTable table, int row) {
        Map<Object, Map<Object, Integer>> rows = tablecellSizes.get(table);
        if (rows == null) return 0;
        Map<Object, Integer> rowheights = rows.get(row);
        if (rowheights == null) return 0;
        int maximum_height = 0;
        for (Map.Entry<Object, Integer> entry : rowheights.entrySet()) {
            int cellHeight = entry.getValue();
            maximum_height = Math.max(maximum_height, cellHeight);
        }
        return maximum_height;
    }
}