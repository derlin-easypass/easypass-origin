package inc;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import javax.crypto.BadPaddingException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableRowSorter;

import dialogs.SessionAndPassFrame;
import dialogs.SimpleDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import models.*;
import models.Exceptions.CryptoException;
import models.Exceptions.WrongCredentialsException;

public class MainApp extends JFrame {
    
    private static Crypto cipher; // for encryption/decryption
    private static String pathToClassFolder;
    private static String serializeFile = "datas.data_ser";
    private static String ivFile = "iv.iv_ser";
    private static String logFile = "easypass.log";
    
    private static int winHeight = 300; // dimensions of the main frame
    private static int winWidth = 400;
    
    private static JPanel mainContainer; // main container (BorderLayout)
    private static TableRowSorter<PassTableModel> sorter;
    private static JTextField filterText;
    
    private static PassTableModel model; // containing the datas, the object
                                         // serialized
    private static MyTable table; // the jtable
    private static JScrollPane scrollPane; // scrollPane for the JTable
    private static SessionManager sm;
    
    private static String[] columnNames = { "account", "email address",
            "password", "notes" }; // the headers for the jtable
    
    
    public static void main( String[] args ) {
        MainApp ma = new MainApp();
    }
    
    
    public MainApp() {
        
        // initializes the main Frame
        super( "accounts and passwords" );
        // sets the listener to save data on quit
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setWindowClosingListener();
        this.setKeyboardShortcuts();
        // sets position, size, etc
        this.setSize( new Dimension( winWidth, winHeight ) );
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winY = ( screensize.height - winHeight ) / 2;
        int winX = ( screensize.width - winWidth ) / 2;
        this.setLocation( winX, winY );
        
        // get the path to the current .class folder
        pathToClassFolder = this.getSessionPath();
        
        System.out.println();
        
        // ArrayList<Object[]> data = new ArrayList<Object[]>();
        // Object[] o1 = {"Google", "Smith", "Snowboarding", "dlskafj", ""};
        // Object[] o2 = {"John", "Doe", "Rowing", "pass", ""};
        // Object[] o3 = {"paypal", "winthoutid@hotmail.fr", "", "pass", ""};
        //
        // data.add(o1);
        // data.add(o2);
        // data.add(o3);
        // model = new PassTableModel(columnNames, data);
        
        // handleCredentialsAndLoadSession();
        // test();
        
        // debug
        SessionManager sm = new SessionManager( this.pathToClassFolder );
        try{
            ArrayList<Object[]> data = (ArrayList<Object[]>) sm.openSession(
                    "test", "test", "test" );
            model = new PassTableModel( columnNames, data );
            
        }catch( CryptoException e1 ){
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }catch( WrongCredentialsException e1 ){
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // creates the main container
        mainContainer = new JPanel( new BorderLayout() );
        mainContainer.add( this.getRowsManipulationMenu(), BorderLayout.NORTH );
        mainContainer.setOpaque( false );
        
        // creates the jtable
        try{
            
            table = new MyTable( model );
            // table = new MyTable(columnNames, datas.dataAsObjectArray());
        }catch( Exception e ){
            System.out.println( "problem while filling the table with data" );
            e.printStackTrace();
        }// end try
        
        // sets the sizes of the JTable
        table.setAutoCreateRowSorter( true );
        table.setFillsViewportHeight( true );
        table.setRowHeight( 20 );
        
        // adds scrollpane and JTable
        scrollPane = new JScrollPane( table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        scrollPane.setPreferredSize( new Dimension( winWidth, winHeight - 50 ) );
        mainContainer.add( scrollPane, BorderLayout.CENTER );
        this.getContentPane().add( mainContainer );
        
        // adds filter/find menu
        mainContainer.add( this.getFilterMenu(), BorderLayout.SOUTH );
        
        // updates the GUI and show the window
        mainContainer.updateUI();
        this.pack();
        this.setMinimumSize( new Dimension( winWidth, winHeight ) );
        this.setJMenuBar( this.getJFrameMenu() );
        this.setVisible( true );
        
    }// end constructor
    
    
    public void setKeyboardShortcuts() {
        
        // escape event closes window
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE,
                0, false );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( escapeKeyStroke, "ESCAPE" );
        
        this.getRootPane().getActionMap().put( "ESCAPE", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                askSaveBeforeClose();
            }
        } );
        
        // CTRL+F to put focus on the filter/find textfield
        KeyStroke ctrlFKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_F,
                InputEvent.CTRL_DOWN_MASK );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( ctrlFKeyStroke, "FIND" );
        
        this.getRootPane().getActionMap().put( "FIND", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                filterText.requestFocusInWindow();
            }
        } );
    }
    
    
    /**
     * gets the path to the sessions folder inside the project. Note : a new
     * folder will be created if it does not already exist
     * 
     * @return
     */
    public String getSessionPath() {
        String path = this.getClass().getResource( "" ).getPath().split( "bin" )[ 0 ]
                + "/sessions";
        
        File dir = new File( path );
        if( !dir.exists() ){
            dir.mkdir();
        }
        
        return path;
    }
    
    
    /**
     * Update the row filter regular expression from the expression in the text
     * box.
     */
    public void setTableFilter() {
        RowFilter<PassTableModel, Object> rf = null;
        ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object, Object>>();
        
        try{
            String text = filterText.getText();
            String[] textArray = text.split( " " );
            
            for( int i = 0; i < textArray.length; i++ ){
                rfs.add( RowFilter.regexFilter( "(?i)" + textArray[ i ], 0, 1,
                        2, 3, 4 ) );
            }
            
            rf = RowFilter.andFilter( rfs );
            
        }catch( java.util.regex.PatternSyntaxException e ){
            return;
        }
        
        sorter.setRowFilter( rf );
    }
    
    
    /**
     * This method is called when the user wants to quit the application. It
     * launches a dialog and asks the user if he wants to save the
     * modifications. If yes, the datas are serialized (overriding the previous
     * serialization) before quit.
     * 
     * @param window
     */
    public void setWindowClosingListener() {
        
        // adds a listener
        // asks the user if he wants to save data and quit, just quit, or resume
        this.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent we ) {               
                askSaveBeforeClose();              
            }
        } );
    }// end setWindowClosing
    
    
    /**
     * creates a JDialog asking the user if he wants to save before quit.
     * If the user clicks cancel, the method will return without ending the application
     */
    private void askSaveBeforeClose() {
        
        int answer = JOptionPane.showConfirmDialog( null,
                "Would you like to save the modifications?", "save",
                JOptionPane.YES_NO_CANCEL_OPTION );
        
        if( answer == JOptionPane.YES_OPTION ){ // serializes and quits
            try{
                if( sm.save( (ArrayList<Object[]>) model.getData() ) ){
                    System.out.println( "datas serialized" );
                }else{
                    System.out.println( "data not saved" );
                }
            }catch( Exception e ){
                System.out
                        .println( "error in serialization. Possible data loss" );
                e.printStackTrace();
            }// end try
            
            System.exit( 0 );
            
        }else if( answer == JOptionPane.NO_OPTION ){ // just quit
            System.exit( 0 );
        }// end if
        
    }// end askSaveBeforeClose
    
    
    /**
     * asks the user to choose the session to load and get his credentials. The
     * method then creates the cipher, deserializes the data and creates a
     * TableModel.
     * 
     * If an error occurs : - either the problem comes from the credentials, so
     * it prompts the user to enter them again - or the problem is somewhere
     * else and the program exits (after logging the cause of the exception)
     */
    @SuppressWarnings("unchecked")
    public void handleCredentialsAndLoadSession() {
        
        SessionAndPassFrame modal = null;
        try{
            
            modal = new SessionAndPassFrame( this,
                    Functionalities.getAvailableSessions( pathToClassFolder,
                            ".*\\.data_ser$" )
            
            );
            
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( 0 );
        }
        
        while( true ){
            
            // asks for pass with a dialog and loads the serialized datas
            // if the pass is wrong, ask again. If the user cancels, quits the
            // application
            
            modal.setVisible( true );
            
            if( modal.getStatus() == false ){ // checks if user clicked cancel
                                              // or closed
                System.exit( 0 );
            }
            // get pass and salt
            // String pass = modal.getPass();
            // String salt = modal.getSalt();
            // String session = modal.getSession();
            
            String pass = "test";
            String salt = "test";
            String session = "test";
            
            try{
                
                if( !Functionalities.sessionExists( pathToClassFolder, session ) ){
                    model = new PassTableModel( columnNames );
                    System.out.println( "no file" );
                    cipher = new Crypto( "PBKDF2WithHmacSHA1",
                            "AES/CBC/PKCS5Padding", "AES", 65536, 128, pass,
                            salt );
                    return;
                }
                
                // creates cipher
                cipher = new Crypto( "PBKDF2WithHmacSHA1",
                        "AES/CBC/PKCS5Padding", "AES", 65536, 128, pass, salt );
                
                // loads the data and gives it to a new jtable model instance
                model = new PassTableModel( columnNames,
                        (ArrayList<Object[]>) cipher.deserializeObject(
                                pathToClassFolder + "\\" + serializeFile,
                                Functionalities.readIv( pathToClassFolder
                                        + "\\" + ivFile ) ) );
                
                System.out.println( "deserialization ok" );
                break;
                
            }catch( BadPaddingException e ){
                // if the pass was wrong, loops again
                System.out
                        .println( "wrong parameters : could not retrieve iv and datas" );
                Functionalities.writeLog( "info: " + e.toString(),
                        pathToClassFolder + "\\" + logFile );
                continue;
            }catch( Exception e ){
                // otherwise, writes the exception to the log file and quit
                System.out.println( "unplanned exception" );
                e.printStackTrace();
                Functionalities.writeLog( "severe: " + e.toString(),
                        pathToClassFolder + "\\" + logFile );
                System.exit( 0 );
            }
        }// end while
    }// end handleCredentials
    
    
    /**
     * it was a try, not really conclusive, to restrict the resizing of the main
     * frame
     * 
     * @param window
     */
    public void setResizeManager( JFrame window ) {
        
        this.addComponentListener( new ComponentAdapter() {
            @Override
            public void componentResized( final ComponentEvent e ) {
                JFrame win = (JFrame) e.getSource();
                System.out.println( win.getWidth() );
                if( win.getWidth() < 600 ){
                    int[] sizes = { 200, 200, 200, 200 };
                    table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
                    table.setColSizes( sizes );
                }else if( win.getWidth() > 900 ){
                    int[] sizes = { 400, 400, 400, 400 };
                    table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
                    table.setColSizes( sizes );
                }else{
                    table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
                }
            }
        } );
    }// end setResizeManager
    
    
    /**
     * creates and return the upper panel, which contains the buttons "add row"
     * and "delete rows"
     * 
     * @return
     */
    public JPanel getRowsManipulationMenu() {
        
        JPanel upperMenu = new JPanel( new FlowLayout() );
        
        // creates the add row button
        JButton addJB = new JButton( "add row" );
        addJB.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                model.addRow();
            }
        } );
        
        upperMenu.add( addJB );
        
        // creates the delete button
        JButton delJB = new JButton( "delete selected rows" );
        delJB.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int[] selectedRows = table.getSelectedRows();
                System.out.println( "\ndeleteing rows:" );
                for( int i = 0; i < selectedRows.length; i++ ){
                    // row index minus i since the table size shrinks by 1
                    // everytime
                    model.deleteRow( selectedRows[ i ] - i );
                }
            }
        } );
        upperMenu.add( delJB );
        return upperMenu;
    }// end getUpperMenu
    
    
    /**
     * asks the user to choose the session to load and get his credentials. The
     * method then creates the cipher, deserializes the data and creates a
     * TableModel.
     * 
     * If an error occurs : - either the problem comes from the credentials, so
     * it prompts the user to enter them again - or the problem is somewhere
     * else and the program exits (after logging the cause of the exception)
     */
    @SuppressWarnings("unchecked")
    public void test() {
        
        SessionAndPassFrame modal = null;
        sm = new SessionManager( this.pathToClassFolder );
        try{
            
            modal = new SessionAndPassFrame( this, sm.availableSessions()
            
            );
            
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( 0 );
        }
        
        while( true ){
            
            // asks for pass with a dialog and loads the serialized datas
            // if the pass is wrong, ask again. If the user cancels, quits the
            // application
            
            modal.setVisible( true );
            
            if( modal.getStatus() == false ){ // checks if user clicked cancel
                                              // or closed
                System.exit( 0 );
            }
            // get pass and salt
            String pass = modal.getPass();
            String salt = modal.getSalt();
            String session = modal.getSession();
            
            try{
                
                if( !sm.sessionExists( session ) ){
                    model = new PassTableModel( columnNames );
                    sm.createSession( session, pass, salt );
                    System.out.println( "no file" );
                    cipher = new Crypto( "PBKDF2WithHmacSHA1",
                            "AES/CBC/PKCS5Padding", "AES", 65536, 128, pass,
                            salt );
                    return;
                }
                
                // creates cipher
                ArrayList<Object[]> data = (ArrayList<Object[]>) sm
                        .openSession( session, pass, salt );
                
                // loads the data and gives it to a new jtable model instance
                model = new PassTableModel( columnNames, data );
                
                System.out.println( "deserialization ok" );
                break;
                
            }catch( Exceptions.WrongCredentialsException e ){
                // if the pass was wrong, loops again
                System.out
                        .println( "wrong parameters : could not retrieve iv and datas" );
                Functionalities.writeLog( "info: " + e.toString(),
                        pathToClassFolder + "\\" + logFile );
                continue;
            }catch( Exception e ){
                // otherwise, writes the exception to the log file and quit
                System.out.println( "unplanned exception" );
                e.printStackTrace();
                Functionalities.writeLog( "severe: " + e.toString(),
                        pathToClassFolder + "\\" + logFile );
                System.exit( 0 );
            }
        }// end while
    }// end handleCredentials
    
    
    public JMenuBar getJFrameMenu() {
        
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
                try{
                    if( sm.save( (ArrayList<Object[]>) model.getData() ) ){
                        System.out.println( "datas serialized" );
                    }else{
                        System.out.println( "data not saved" );
                    }
                }catch( Exception ee ){
                    System.out
                            .println( "error in serialization. Possible data loss" );
                    ee.printStackTrace();
                }// end try
            }
        } );
        menu.add( menuItem );
        
        // menuItem = new JMenuItem( "Both text and icon", new ImageIcon(
        // "images/middle.gif" ) );
        // menuItem.setMnemonic( KeyEvent.VK_B );
        // menu.add( menuItem );
        //
        // menuItem = new JMenuItem( new ImageIcon( "images/middle.gif" ) );
        // menuItem.setMnemonic( KeyEvent.VK_D );
        // menu.add( menuItem );
        
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
    
    
    public JPanel getFilterMenu() {
        
        sorter = new TableRowSorter<PassTableModel>( model );
        table.setRowSorter( sorter );
        // Create a separate form for filterText and statusText
        JPanel form = new JPanel();
        JLabel l1 = new JLabel( "Find :" );
        form.add( l1 );
        filterText = new JTextField( 50 );
        // Whenever filterText changes, invoke newFilter.
        filterText.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate( DocumentEvent e ) {
                setTableFilter();
            }
            
            
            public void insertUpdate( DocumentEvent e ) {
                setTableFilter();
            }
            
            
            public void removeUpdate( DocumentEvent e ) {
                setTableFilter();
            }
        } );
        l1.setLabelFor( filterText );
        form.add( l1 );
        form.add( filterText );
        // form.setSize( new Dimension(50, 100) );
        
        return form;
    }// end getFilterMenu
    
}// end class
