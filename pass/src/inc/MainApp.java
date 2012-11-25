package inc;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import javax.crypto.BadPaddingException;
import javax.swing.*;
import javax.swing.event.*;

import dialogs.SessionAndPassFrame;
import dialogs.SimpleDialog;

import java.util.ArrayList;
import java.util.Date;

import models.*;


public class MainApp {

    private static Crypto cipher; 				// for encryption/decryption
    private static String pathToClassFolder;
    private static String serializeFile = "datas.data_ser";
    private static String ivFile = "iv.iv_ser";
    private static String logFile = "easypass.log";
    
    private static JFrame window; 				//main frame
    private static int winHeight = 300; 		// dimensions of the main frame
    private static int winWidth = 400;
    
    private static JPanel mainContainer; 		//main container (BorderLayout)

    private static PassTableModel model; 		// containing the datas, the object serialized
    private static MyTable table; 				// the jtable
    private static  JScrollPane scrollPane; 	//scrollPane for the JTable
       			
    
    private static String[] columnNames =  {
    	"account", 
    	"email address", 
    	"password", 
    	"notes"
    }; // the headers for the jtable


	public static void main(String[] args) {

    	
        // initializes the main Frame
        window = new JFrame("accounts and passwords");
        //sets the listener to save data on quit
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setWindowClosingListener(window);
        //sets position, size, etc
        window.setSize(new Dimension(winWidth, winHeight));       
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winY = (screensize.height - winHeight) / 2;
        int winX = (screensize.width - winWidth) / 2;
        window.setLocation(winX, winY);
        

        // get the path to the current .class folder
        pathToClassFolder = (MainApp.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()) + "/sessions";
        
         
//        ArrayList<Object[]> data = new ArrayList<Object[]>();
//        Object[] o1 = {"Google", "Smith", "Snowboarding", "dlskafj", ""};
//        Object[] o2 = {"John", "Doe", "Rowing", "pass", ""};
//        Object[] o3 = {"paypal", "winthoutid@hotmail.fr", "", "pass", ""};
//
//        data.add(o1);
//        data.add(o2);
//        data.add(o3);
//		  model = new PassTableModel(columnNames, data);
        
         
//        handleCredentialsAndLoadSession();
        test();

        // creates the main container
        mainContainer = new JPanel(new BorderLayout());
        mainContainer.add(getUpperMenu(), BorderLayout.NORTH);
        mainContainer.setOpaque(false);


        // creates the jtable
        try {
        	
            table = new MyTable(model);
//        	table = new MyTable(columnNames, datas.dataAsObjectArray());
        } catch (Exception e) {
            System.out.println("problem while filling the table with data");
            e.printStackTrace();
        }//end try
        
        // sets the size of the JTable and hide "id" column
        //table.setPreferredScrollableViewportSize(new Dimension((winWidth - 40), winHeight));
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(20);


        // add scrollpane and JTable to the window
        scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(winWidth, winHeight - 50));
        mainContainer.add(scrollPane, BorderLayout.CENTER);
//        scrollPane.setMaximumSize(new Dimension(200, 400));
//        scrollPane.setMinimumSize(new Dimension(200, 200));
        window.getContentPane().add(mainContainer);
        
        //updates the GUI and show the window
        mainContainer.updateUI();
        window.pack();
        window.setMinimumSize(new Dimension(winWidth, winHeight));
        window.setJMenuBar( getMenu() );
        window.setVisible(true);

    }//end main
    
    
    /**
     * This method is called when the user want to quit the application.
     * It launches a dialog and asks the user if he wants to save the modifications.
     * If yes, the datas are serialized (overriding the previous serialization) before quit.
     * @param window
     */
    public static void setWindowClosingListener(JFrame window){
    	
    	// adds a listener 
        //asks the user if he wants to save data and quit, just quit, or resume 
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				int answer = JOptionPane.showConfirmDialog(null,
						"Would you like to save the modifications?", "save",
						JOptionPane.YES_NO_CANCEL_OPTION);

				if (answer == JOptionPane.YES_OPTION) { // serializes and quits
					try {
						byte[] iv = cipher.serializeObject(pathToClassFolder
								+ "\\" + serializeFile, (ArrayList<Object[]>)model.getData());
						Functionalities.saveIv(iv, pathToClassFolder
								+ "\\" + ivFile);
						System.out.println("datas serialized");
					} catch (Exception e) {
						System.out
								.println("error in serialization. Possible data loss");
						e.printStackTrace();
					}// end try

					System.exit(0);

				} else if (answer == JOptionPane.NO_OPTION) { // just quit
					System.exit(0);
				}// end if
			}
		});
    }//end setWindowClosing

    
    /**
     * asks the user to choose the session to load and get his credentials.
     * The method then creates the cipher, deserializes the data
     * and creates a TableModel.
     * 
     * If an error occurs :
     * - either the problem comes from the credentials, so it prompts the user to enter them again
     * - or the problem is somewhere else and the program exits (after logging the cause of the exception)
     */
	@SuppressWarnings("unchecked")
	public static void handleCredentialsAndLoadSession() {
		
		SessionAndPassFrame modal = null;
		try {
			
			modal = new SessionAndPassFrame(
					window,
					Functionalities.getAvailableSessions(pathToClassFolder, ".*\\.data_ser$")
										
			);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		while (true) {

			// asks for pass with a dialog and loads the serialized datas
			// if the pass is wrong, ask again. If the user cancels, quits the
			// application
			
			
			modal.setVisible(true);

			if (modal.getStatus() == false) { // checks if user clicked cancel
												// or closed
				System.exit(0);
			}
			// get pass and salt
			String pass = modal.getPass(); //"my_pass"; // 
			String salt = modal.getSalt(); //"my_salt"; // 
			String session = modal.getSession();
			serializeFile = session + ".data_ser";
			ivFile = session + ".iv_ser";
			

			try {
				
				if(!Functionalities.sessionExists(pathToClassFolder, session)){
					model = new PassTableModel(columnNames);
					System.out.println("no file");
					cipher = new Crypto("PBKDF2WithHmacSHA1",
							"AES/CBC/PKCS5Padding", "AES", 65536, 128, pass, salt);
					return;
				}


				// creates cipher
				cipher = new Crypto("PBKDF2WithHmacSHA1",
						"AES/CBC/PKCS5Padding", "AES", 65536, 128, pass, salt);

				// loads the data and gives it to a new jtable model instance
				model = new PassTableModel(columnNames,
						(ArrayList<Object[]>) cipher.deserializeObject(
								pathToClassFolder + "\\" + serializeFile,
								Functionalities.readIv(pathToClassFolder + "\\"
										+ ivFile)));

				System.out.println("deserialization ok");
				break;

			} catch (BadPaddingException e) {
				// if the pass was wrong, loops again
				System.out
						.println("wrong parameters : could not retrieve iv and datas");
				Functionalities.writeLog("info: " + e.toString(), pathToClassFolder + "\\" + logFile);
				continue;
			} catch (Exception e) {
				// otherwise, writes the exception to the log file and quit
				System.out.println("unplanned exception");
				e.printStackTrace();
				Functionalities.writeLog("severe: " + e.toString(), pathToClassFolder + "\\"
						+ logFile);
				System.exit(0);
			}
		}//end while
	}//end handleCredentials
    

    /**
     * it was a try, not really conclusive, to restrict the resizing of the main frame
     * @param window
     */
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
    
    
    
    /**
     * creates and return the upper panel, which contains the buttons 
     * "add row" and "delete rows"
     * @return
     */
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
    
    
    
    
    /**
     * asks the user to choose the session to load and get his credentials.
     * The method then creates the cipher, deserializes the data
     * and creates a TableModel.
     * 
     * If an error occurs :
     * - either the problem comes from the credentials, so it prompts the user to enter them again
     * - or the problem is somewhere else and the program exits (after logging the cause of the exception)
     */
    @SuppressWarnings("unchecked")
    public static void test() {
        
        SessionAndPassFrame modal = null;
        SessionManager sm = new SessionManager("C:\\passProtect\\pass");
        try {
            
            modal = new SessionAndPassFrame(
                    window,
                    sm.availableSessions()
                                        
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        while (true) {

            // asks for pass with a dialog and loads the serialized datas
            // if the pass is wrong, ask again. If the user cancels, quits the
            // application
            
            
            modal.setVisible(true);

            if (modal.getStatus() == false) { // checks if user clicked cancel
                                                // or closed
                System.exit(0);
            }
            // get pass and salt
            String pass = modal.getPass(); //"my_pass"; // 
            String salt = modal.getSalt(); //"my_salt"; // 
            String session = modal.getSession();
           

            try {
                
                if(!sm.sessionExists( session )){
                    model = new PassTableModel(columnNames);
                    sm.createSession( session, pass, salt );
                    System.out.println("no file");
                    cipher = new Crypto("PBKDF2WithHmacSHA1",
                            "AES/CBC/PKCS5Padding", "AES", 65536, 128, pass, salt);
                    return;
                }


                // creates cipher
                ArrayList<Object[]> data = (ArrayList<Object[]>)sm.openSession( session, pass, salt );

                // loads the data and gives it to a new jtable model instance
                model = new PassTableModel(columnNames,
                        data);

                System.out.println("deserialization ok");
                break;

            } catch (Exceptions.WrongCredentialsException e) {
                // if the pass was wrong, loops again
                System.out
                        .println("wrong parameters : could not retrieve iv and datas");
                Functionalities.writeLog("info: " + e.toString(), pathToClassFolder + "\\" + logFile);
                continue;
            } catch (Exception e) {
                // otherwise, writes the exception to the log file and quit
                System.out.println("unplanned exception");
                e.printStackTrace();
                Functionalities.writeLog("severe: " + e.toString(), pathToClassFolder + "\\"
                        + logFile);
                System.exit(0);
            }
        }//end while
    }//end handleCredentials
    
    
    public static JMenuBar getMenu() {
        
        // Where the GUI is created:
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
        
        // Create the menu bar.
        menuBar = new JMenuBar();
        
        // Build the first menu.
        menu = new JMenu( "options" );
        menu.setMnemonic( KeyEvent.VK_A );
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items" );
        menuBar.add( menu );
        
        // a group of JMenuItems
        menuItem = new JMenuItem( "save", KeyEvent.VK_T );
        menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
                ActionEvent.CTRL_MASK ) );
        menuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               System.out.println("save");
            }
        } );
        menu.add( menuItem );
        
//        menuItem = new JMenuItem( "Both text and icon", new ImageIcon(
//                "images/middle.gif" ) );
//        menuItem.setMnemonic( KeyEvent.VK_B );
//        menu.add( menuItem );
//        
//        menuItem = new JMenuItem( new ImageIcon( "images/middle.gif" ) );
//        menuItem.setMnemonic( KeyEvent.VK_D );
//        menu.add( menuItem );
        
        // a group of check box menu items
        
        // a submenu
        menu.addSeparator();
        submenu = new JMenu( "A submenu" );
        submenu.setMnemonic( KeyEvent.VK_S );
        
        menuItem = new JMenuItem( "An item in the submenu" );
        menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_2,
                ActionEvent.ALT_MASK ) );
        submenu.add( menuItem );
        
        menuItem = new JMenuItem( "Another item" );
        submenu.add( menuItem );
        menu.add( submenu );
        
        // Build second menu in the menu bar.
        menu = new JMenu( "Another Menu" );
        menu.setMnemonic( KeyEvent.VK_N );
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu does nothing" );
        menuBar.add( menu );
        
        return menuBar;
    }
//  window.addComponentListener(new ComponentAdapter() {
//
//      @Override
//      public void componentResized(ComponentEvent e) {
//      	JFrame win = (JFrame) e.getSource();
//          win.setSize(new Dimension(win.getPreferredSize().width, win.getHeight()));
//          super.componentResized(e);
//      }
//
//  });
  
//  //essai 
//  Dimension dimPreferred = window.getPreferredSize();
//  Dimension dimMinimum = window.getMinimumSize();
//  Dimension dimMaximum = window.getMaximumSize();
//  dimPreferred.width = winWidth;
//  dimMinimum.width = winWidth;
//  dimMaximum.width = winWidth;
//  window.setPreferredSize( dimPreferred );
//  window.setMinimumSize( dimMinimum );
//  window.setMaximumSize( dimMaximum );
    
    
    // adds listener to the jtable
//  table.getModel().addTableModelListener(new TableModelListener() {
//
//      public void tableChanged(TableModelEvent e) {
//
//          try {
//          	System.out.println(table.getModel().getRowCount());
//
//          } catch (Exception e1) {
//              System.out.println("problem while updating field");
//              e1.printStackTrace();
//          }
//          // table.update(colname, data2);
//      }
//
//  });

}// end class
