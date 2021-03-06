package ch.derlin.easypass.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PasswordCellRenderer extends DefaultTableCellRenderer {
    
    private static final String ASTERISKS = "************************************************";
    
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object data,
            boolean isSelected, boolean hasFocus, int row, int column ) {
        
        super.getTableCellRendererComponent( table, data, isSelected, hasFocus,
                row, column );
        
        int length = 0;
        
        if( data instanceof String ){
            length = ( (String) data ).length();
        }else if( data instanceof char[] ){
            length = ( (char[]) data ).length;
        }
        
        if( isSelected ){
            setBackground( super.getBackground() );
        }
        setText( ASTERISKS.substring( 0, length ) );
        
        return this;
    }
    
}
