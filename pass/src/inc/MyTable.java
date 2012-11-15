package inc;

import javax.swing.JTable;


public class MyTable extends JTable {
	

	public MyTable(String[] columnNames, Object[][] data){
		super(new MyTableModel(columnNames, data));
	}
	
	
	public void update(String[] colname, Object[][] data){
       this.setModel(new MyTableModel(colname, data));
       this.updateUI();
	}
	
	public void setColSizes(int[] sizes){
		
		
		
	}

}//end MyTable
