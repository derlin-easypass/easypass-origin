package inc;

import javax.swing.JTable;
import javax.swing.table.TableColumn;


public class MyTable extends JTable {
	

	public MyTable(String[] columnNames, Object[][] data){
		super(new MyTableModel(columnNames, data));
	}
	
	public MyTable(MyTableModel model){
		super(model);
	}
	
	public MyTable(PassTableModel model){
		super(model);
	}
	
	public void update(String[] colname, Object[][] data){
       this.setModel(new MyTableModel(colname, data));
       this.updateUI();
	}
	
	public void setColSizes(int[] sizes){
		
		TableColumn col;
		int colCount = this.getColumnModel().getColumnCount();
		
		if(colCount > sizes.length)
			colCount = sizes.length;
		
		for (int i = 0; i < colCount ; i++) {
			 this.getColumnModel().getColumn(i).setPreferredWidth(sizes[i]);			 
		}
		System.out.println("col sizes set");
	}

}//end MyTable
