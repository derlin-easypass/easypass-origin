package inc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;

import models.*;
import inc.*;

public class Main2 {

    private static final long serialVersionUID = 1L;


    private static Crypto cipher; // for encryption/decryption
    private static String pathToClassFolder;
    private static String serializeFile = "datas.ser";
    private static String ivFile = "iv.ser";
    
    private static int winHeight = 400; // dimensions of the main frame
    private static int winWidth = 800;

    private static MyTable table; // the jtable
    private static PassTableModel model; // containing the datas, the object serialized
    
    private static String[] columnNames =  {
    	"account", 
    	"email address", 
    	"password", 
    	"notes"
    }; // the headers for the jtable

    @SuppressWarnings("unchecked")
	public static void main(String[] args) {

        // get the path to the current .class folder
        pathToClassFolder = (Main2.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath());

         
//        ArrayList<Object[]> data = new ArrayList<Object[]>();
//        Object[] o1 = {"Google", "Smith", "Snowboarding", "dlskafj", ""};
//        Object[] o2 = {"John", "Doe", "Rowing", "pass", ""};
//        Object[] o3 = {"paypal", "winthoutid@hotmail.fr", "", "pass", ""};
//
//        data.add(o1);
//        data.add(o2);
//        data.add(o3);
//		  model = new PassTableModel(columnNames, data);
        
        
        // creates a cipher object for cryption/decryption and loads the
        // serialized datas
        try {

            cipher = new Crypto("PBKDF2WithHmacSHA1", "AES/CBC/PKCS5Padding",
                    "AES", 65536, 128, "my_pass", "my_salt");
            
            model = new PassTableModel(columnNames,          		
            		(ArrayList<Object[]>) cipher.deserializeObject(
            				pathToClassFolder + "\\" + serializeFile,
                    Functionalities.readIv(pathToClassFolder + "\\" + ivFile)
                    )           
            );
            
            System.out.println("deserialization ok");

        } catch (Exception e) {
            System.out.println("could not retrieve iv and datas");
            e.printStackTrace();
            System.exit(0);
        }

        // initializes the main Frame
        JFrame window = new JFrame("accounts and passwords");
        //sets the listener to save data on quit
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setWindowClosingListener(window);
        //sets position, size, etc
        window.setSize(winWidth, winHeight);
        window.setPreferredSize(new Dimension(winWidth, winHeight));
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winY = (screensize.height - winHeight) / 2;
        int winX = (screensize.width - winWidth) / 2;
        window.setLocation(winX, winY);
        
//        window.addComponentListener(new ComponentAdapter() {
//
//            @Override
//            public void componentResized(ComponentEvent e) {
//            	JFrame win = (JFrame) e.getSource();
//                win.setSize(new Dimension(win.getPreferredSize().width, win.getHeight()));
//                super.componentResized(e);
//            }
//
//        });
        
        Dimension dimPreferred = window.getPreferredSize();
        Dimension dimMinimum = window.getMinimumSize();
        Dimension dimMaximum = window.getMaximumSize();
        dimPreferred.width = winWidth;
        dimMinimum.width = winWidth;
        dimMaximum.width = winWidth;
        window.setPreferredSize( dimPreferred );
        window.setMinimumSize( dimMinimum );
        window.setMaximumSize( dimMaximum );
        window.setVisible(true);

		
		
        // creates the main container
        JPanel container = new JPanel(new BorderLayout());
        container.add(getUpperMenu(), BorderLayout.NORTH);
        container.setOpaque(false);


        // creates the jtable
        try {
        	
            table = new MyTable(model);
//        	table = new MyTable(columnNames, datas.dataAsObjectArray());
        } catch (Exception e) {
            System.out.println("problem while filling the table with data");
            e.printStackTrace();
        }//end try
        
        // sets the size of the JTable and hide "id" column
        table.setPreferredScrollableViewportSize(new Dimension((winWidth - 40), winHeight));
        table.setAutoCreateRowSorter(true);
        // table.setFillsViewportHeight(true);
        table.setRowHeight(20);

        
        
        // adds listener to the jtable
//        table.getModel().addTableModelListener(new TableModelListener() {
//
//            public void tableChanged(TableModelEvent e) {
//
//                try {
//                	System.out.println(table.getModel().getRowCount());
//
//                } catch (Exception e1) {
//                    System.out.println("problem while updating field");
//                    e1.printStackTrace();
//                }
//                // table.update(colname, data2);
//            }
//
//        });



        // add scrollpane and JTable to the window
        JScrollPane scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setMaximumSize(new Dimension(200, 400));
        scrollPane.setMinimumSize(new Dimension(200, 200));
        window.getContentPane().add(container);
        
        
        container.updateUI();

//        window.addComponentListener(new ComponentAdapter() {
//  	      public void componentResized(ComponentEvent e) {
//  	    	  JFrame win = (JFrame) e.getSource();
//  	    	  System.out.println(win.getWidth());
//  	    	  table.setPreferredScrollableViewportSize(new Dimension((win.getWidth() - 40),
//  	                  win.getHeight()));
//  	    	  
//  	      }
//  	    });
    }//end main
    
    
    
    public static void setWindowClosingListener(JFrame window){
    	
    	// adds a listener 
        //asks the user if he wants to save data and quit, just quit, or resume 
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				int answer = JOptionPane.showConfirmDialog(null,
						"Would you like to save the modifications?", "save",
						JOptionPane.YES_NO_CANCEL_OPTION);

				if (answer == JOptionPane.YES_OPTION) { //serialize and quit

					try {
						byte[] iv = cipher.serializeObject(pathToClassFolder
								+ "\\datas.ser", model.getData());
						
						Functionalities.saveIv(iv, pathToClassFolder + "\\iv.ser");
						
						System.out.println("datas serialized");
						
					} catch (Exception e) {
						System.out.println("error in serialization. Possible data loss");
						e.printStackTrace();
					}//end try

					System.exit(0);

				} else if (answer == JOptionPane.NO_OPTION) { //just quit

					System.exit(0);

				}// end if
			}
		});
    }//end setWindowClosing

    
    
    public static void setResizeManager(JFrame window){
    	
    	window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
            	JFrame win = (JFrame) e.getSource();
            	System.out.println(win.getWidth());
                if (win.getWidth() < 600) {
                	int[] sizes = {200, 200, 200, 200} ;
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    table.setColSizes(sizes);
                } else if (win.getWidth() > 900) {
                	int[] sizes = {400, 400, 400, 400} ;
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    table.setColSizes(sizes);
                }else{
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                }
            }
        });
    }//end setResizeManager
    
    
    
    
    public static JPanel getUpperMenu(){
    	
        JPanel upperMenu = new JPanel(new FlowLayout());
        
        //creates the add row button
        JButton addJB = new JButton("add row");
        addJB.addActionListener(
        	new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.addRow();
                }
            }
        );
        
        upperMenu.add(addJB);
        
        //creates the delete button
        JButton delJB = new JButton("delete selected rows");
        delJB.addActionListener(
            	new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       int[] selectedRows = table.getSelectedRows();
                       System.out.println("\ndeleteing rows:");
                       for(int i = 0; i < selectedRows.length; i++){
                    	 //row index minus i since the table size shrinks by 1 everytime
                    	   model.deleteRow(selectedRows[i] - i); 
                       }
                    }
                }
            );
        upperMenu.add(delJB);
        return upperMenu;
    }//end getUpperMenu
    

}// end class
