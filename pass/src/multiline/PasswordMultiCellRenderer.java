package multiline;

import javax.swing.*;
import java.awt.*;

public class PasswordMultiCellRenderer extends MultiLineCellRenderer {

    private static final String ASTERISKS = "************************************************";


    @Override
    public Component getTableCellRendererComponent( JTable table, Object data,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column ) {

        //        super.getTableCellRendererComponent( table, data, isSelected, hasFocus,
        //                row, column );

        //        int length = 0;
        //
        //        if( data instanceof String ) {
        //            length = ( ( String ) data ).length();
        //        } else if( data instanceof char[] ) {
        //            length = ( ( char[] ) data ).length;
        //        }

        if( isSelected ) {
            System.out.println( super.getBackground() );
        }
        //setText( ASTERISKS.substring( 0, length ) );
        setText( data.toString().length() > 0 ? "***" : "" );
        return this;
    }


}//end class
