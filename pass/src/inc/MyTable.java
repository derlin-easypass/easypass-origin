package inc;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class MyTable extends JTable {

	public MyTable(String[] columnNames, Object[][] data) {
		super(new MyTableModel(columnNames, data));
	}

	public MyTable(MyTableModel model) {
		super(model);
	}

	public MyTable(PassTableModel model) {
		super(model);
	}

	public void update(String[] colname, Object[][] data) {
		this.setModel(new MyTableModel(colname, data));
		this.updateUI();
	}

	//TODO
	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int column) {
		JComponent c = (JComponent)super.prepareRenderer(renderer, row, column);

		if (!isRowSelected(row)){
			c.setBackground(row % 2 == 0 ? getBackground() : new Color(230, 230, 230));
			
		}else{
			
			c.setFont(getFont().deriveFont(Font.BOLD));
			if(getSelectedRowCount() == 1 && getSelectedColumnCount() == 1 
				&& isRowSelected(row) && isColumnSelected(column) ){
				
				c.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
			}else if( column == getEditingColumn() && row == getEditingRow()){
				System.out.println("true");
				c.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
				c.setForeground(Color.BLACK);
				c.setBackground(new Color(0, 0, 200));
			}else{

			}
		}

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

//		this.setSelectionBackground(new Color(115, 164, 209));
		JTableHeader header = this.getTableHeader();

		header.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		
		

	}

	public void setColSizes(int[] sizes) {

		TableColumn col;
		int colCount = this.getColumnModel().getColumnCount();

		if (colCount > sizes.length)
			colCount = sizes.length;

		for (int i = 0; i < colCount; i++) {
			this.getColumnModel().getColumn(i).setPreferredWidth(sizes[i]);
		}
		System.out.println("col sizes set");
	}

}// end MyTable
