package inc;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
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
import java.util.TimerTask;
import java.util.regex.Pattern;

import models.*;
import models.Exceptions.CryptoException;
import models.Exceptions.WrongCredentialsException;

public class MainApp extends JFrame {
    
    private Crypto cipher; // for encryption/decryption
    private String pathToSessionsFolder;
    private String appName = "easypass";
    private String logFile = appName + ".log";
    
    private int winHeight = 300; // dimensions of the main frame
    private int winWidth = 480;
    
    private JPanel mainContainer; // main container (BorderLayout)
    private JvUndoManager undoManager;
    private TableRowSorter<PassTableModel> sorter;
    private JTextField filterText;
    private JTextField infos;
    private Timer infosTimer;
    
    private PassTableModel model; // containing the datas, the object
                                  // serialized
    private MyTable table; // the jtable
    private JScrollPane scrollPane; // scrollPane for the JTable
    private SessionManager sm;
    
    private String[] columnNames = { "account", "email address", "password",
            "notes" }; // the headers for the jtable
    
    
    public static void main( String[] args ) {
        MainApp ma = new MainApp();
    }
    
    
    public MainApp() {
        // initializes the main Frame
        super( "accounts and passwords" );
        int a = 1;
        a += ++a;
        System.out.println( a );
        // sets the listener to save data on quit
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setWindowClosingListener();
        // sets position, size, etc
        this.setSize( new Dimension( winWidth, winHeight ) );
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winY = ( screensize.height - winHeight ) / 2;
        int winX = ( screensize.width - winWidth ) / 2;
        this.setLocation( winX, winY );
        
        // get the path to the current .class folder
        pathToSessionsFolder = this.getSessionPathAppData();
        
        // ArrayList<Object[]> data = new ArrayList<Object[]>();
        // Object[] o1 = {"Google", "Smith", "Snowboarding", "dlskafj", ""};
        // Object[] o2 = {"John", "Doe", "Rowing", "pass", ""};
        // Object[] o3 = {"paypal", "winthoutid@hotmail.fr", "", "pass", ""};
        //
        // data.add(o1);
        // data.add(o2);
        // data.add(o3);
        // model = new PassTableModel(columnNames, data);
        
        handleCredentialsAndLoadSession();
        
        // debug
        // SessionManager sm = new SessionManager( this.pathToClassFolder );
        // try{
        // ArrayList<Object[]> data = (ArrayList<Object[]>) sm.openSession(
        // "test", "test", "test" );
        // model = new PassTableModel( columnNames, data );
        //
        // }catch( CryptoException e1 ){
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }catch( WrongCredentialsException e1 ){
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        
        // creates the main container
        mainContainer = new JPanel( new BorderLayout() );
        mainContainer.add( this.getRowsManipulationMenu(), BorderLayout.NORTH );
        mainContainer.setOpaque( false );
        
        // creates the jtable
        try{
            
            table = new MyTable( model );
            
        }catch( Exception e ){
            System.out.println( "problem while filling the table with data" );
            e.printStackTrace();
        }// end try
        
        // sets the sizes of the JTable
        table.setAutoCreateRowSorter( true );
        table.setFillsViewportHeight( true );
        table.setRowHeight( 30 );
        table.setStyle();
        
        // sets the undo manager for CTRL Z
        undoManager = new JvUndoManager();
        model.addUndoableEditListener( undoManager );
        
        // adds scrollpane and JTable
        scrollPane = new JScrollPane( table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        scrollPane.setPreferredSize( new Dimension( winWidth, winHeight - 50 ) );
        mainContainer.add( scrollPane, BorderLayout.CENTER );
        this.getContentPane().add( mainContainer );
        
        // adds filter/find menu
        mainContainer.add( this.getDownMenu(), BorderLayout.SOUTH );
        
        // updates the GUI and show the window
        mainContainer.updateUI();
        try{
            UIManager
                    .setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }catch( Exception e ){
            System.out.println( "problem loading default UIManager" );
        }
        
        this.pack();
        this.setMinimumSize( new Dimension( winWidth, winHeight ) );
        this.setJMenuBar( this.getJFrameMenu() );
        this.setKeyboardShortcuts();
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
                if( askSaveData() ){
                    System.exit( 0 );
                }
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
        
        // CTRL+N to add a new line
        KeyStroke NewLineKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_N,
                InputEvent.CTRL_DOWN_MASK );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( NewLineKeyStroke, "NEWLINE" );
        
        this.getRootPane().getActionMap().put( "NEWLINE", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                model.addRow();
            }
        } );
        
        // DEL to delete selected rows
        KeyStroke DelLineKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( NewLineKeyStroke, "DELLINE" );
        
        this.getRootPane().getActionMap().put( "DELLINE", new AbstractAction() {
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
        
    }// end setShortCuts
    
    
    /**
     * gets the path to the sessions folder inside the project. Note : a new
     * folder will be created if it does not already exist
     * 
     * @return
     */
    public String getSessionPath() {
        System.out.println( System.getenv( "APPDATA" ) );
        String path = this.getClass().getResource( "" ).getPath().split( "bin" )[ 0 ]
                + "/sessions";
        
        File dir = new File( path );
        if( !dir.exists() ){
            dir.mkdir();
        }
        
        return path;
    }
    
    
    /**
     * gets the path to the sessions folder inside the project. Note : a new
     * folder will be created if it does not already exist
     * 
     * @return
     */
    public String getSessionPathAppData() {
        String path = System.getenv( "APPDATA" ) + "\\" + appName;
        File appdata = new File( path );
        if( !appdata.exists() || !appdata.isDirectory() ){
            if( appdata.mkdir() ){
                path += "\\sessions";
                if( new File( path ).mkdir() ){
                    return path;
                }
            }
            System.out.println( "error appdata" );
            return "";
        }else{
            return path + "\\sessions";
        }
        
    }
    
    
    // TODO : bug quand effacer des rows avec une cellule sélectionnée
    
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
                if( askSaveData() ){
                    System.exit( 0 );
                }
            }
        } );
    }// end setWindowClosing
    
    
    /**
     * creates a JDialog asking the user if he wants to save before quit. The
     * method will return false only if the user clicked cancel.
     */
    private boolean askSaveData() {
        
        int answer = JOptionPane.showConfirmDialog( null,
                "Would you like to save the modifications?", "save",
                JOptionPane.YES_NO_CANCEL_OPTION );
        
        if( answer == JOptionPane.YES_OPTION ){ // serializes and quits
            try{
                System.out.println( sm.getDataPath() );
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
            
            return true;
            
        }else if( answer == JOptionPane.NO_OPTION ){ // just quit
            return true;
        }// end if
        
        return false;
    }// end askSaveBeforeClose
    
    
    public File fileChooser() {
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter( new TextFilter() );
        chooser.setCurrentDirectory( new java.io.File( "." ) );
        chooser.setDialogTitle( "select folder" );
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        
        if( chooser.showSaveDialog( this ) == JFileChooser.APPROVE_OPTION ){
            return chooser.getSelectedFile();
        }else{
            System.out.println( "No Selection " );
            return null;
        }
        
    }// end filechooser
    
    
    private class TextFilter extends javax.swing.filechooser.FileFilter {
        
        public String getDescription() {
            return "Plain text document (*.txt)";
        }
        
        
        public boolean accept( File file ) {
            if( file.isDirectory() ){
                return true;
            }
            return file.getName().endsWith( ".txt" );
        }
    }
    
    
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
    public void handleCredentialsAndLoadSession() {
        
        SessionAndPassFrame modal = null;
        sm = new SessionManager( this.pathToSessionsFolder );
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
                        pathToSessionsFolder + "\\" + logFile );
                continue;
            }catch( Exception e ){
                // otherwise, writes the exception to the log file and quit
                System.out.println( "unplanned exception" );
                e.printStackTrace();
                Functionalities.writeLog( "severe: " + e.toString(),
                        pathToSessionsFolder + "\\" + logFile );
                System.exit( 0 );
            }
        }// end while
    }// end handleCredentials
    
    
    public JMenuBar getJFrameMenu() {
        
        // Where the GUI is created:
        JMenuBar menuBar;
        JMenu menu, editMenu;
        JMenuItem saveSubMenu, jsonSubMenu, printSubMenu, undoSubMenu, redoSubMenu, newSessionSubMenu;
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
        
        // save option
        saveSubMenu = new JMenuItem( "save", KeyEvent.VK_T );
        saveSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
                ActionEvent.CTRL_MASK ) );
        saveSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try{
                    if( sm.save( (ArrayList<Object[]>) model.getData() ) ){
                        System.out.println( "datas serialized" );
                        showInfos("data saved.");
                    }else{
                        System.out.println( "data not saved" );
                        showInfos("an error occurred! Data not saved...");
                    }
                }catch( Exception ee ){
                    System.out
                            .println( "error in serialization. Possible data loss" );
                    ee.printStackTrace();
                }// end try
            }
        } );
        menu.add( saveSubMenu );
        
        // open a new session menu
        newSessionSubMenu = new JMenuItem( "open new session" );
        
        newSessionSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if( sm.isOpened() )
                    sm.close();
                table.setVisible( false );
                handleCredentialsAndLoadSession();
                table.setModel( model );
                table.updateUI();
                table.setVisible( true );
            }
        } );
        menu.add( newSessionSubMenu );
        
        jsonSubMenu = new JMenuItem( "export as Json", KeyEvent.VK_E );
        jsonSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E,
                ActionEvent.CTRL_MASK ) );
        jsonSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                File file = fileChooser();
                if( file != null ){
                    try{
                        sm.writeAsJson( model.getData(), file );
                        JOptionPane.showMessageDialog( null, "data saved to "
                                + file.getName(), "export complete",
                                JOptionPane.PLAIN_MESSAGE );
                    }catch( IOException ee ){
                        ee.printStackTrace();
                        JOptionPane.showMessageDialog( null,
                                "an error occurred during export",
                                "export error", JOptionPane.ERROR_MESSAGE );
                    }
                }// end if
            }
        } );
        menu.add( jsonSubMenu );
        
        // print subMenu
        menu.addSeparator();
        printSubMenu = new JMenuItem( "print" );
        printSubMenu.setMnemonic( KeyEvent.VK_P );
        printSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P,
                ActionEvent.CTRL_MASK ) );
        
        printSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try{
                    table.print( JTable.PrintMode.NORMAL );
                }catch( PrinterException pe ){
                    System.err.println( "Error printing: " + pe.getMessage() );
                }
            }
        } );
        
        menu.add( printSubMenu );
        
        // Build edit menu in the menu bar.
        editMenu = new JMenu( "edit" );
        // add undo submenu
        undoSubMenu = new JMenuItem( undoManager.getUndoAction() );
        undoSubMenu.setMnemonic( KeyEvent.VK_Z );
        undoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z,
                ActionEvent.CTRL_MASK ) );
        // add redo submenu
        redoSubMenu = new JMenuItem( undoManager.getRedoAction() );
        redoSubMenu.setMnemonic( KeyEvent.VK_Y );
        redoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y,
                ActionEvent.CTRL_MASK ) );
        editMenu.add( undoSubMenu );
        editMenu.add( redoSubMenu );
        
        menuBar.add( editMenu );
        
        return menuBar;
    }
    
    
    public JPanel getDownMenu() {
        
        sorter = new TableRowSorter<PassTableModel>( model );
        table.setRowSorter( sorter );
        // Create a separate form for filterText and statusText
        JPanel form = new JPanel();
        JLabel l1 = new JLabel( "Find :" );
        form.add( l1 );
        filterText = new JTextField( 30 );
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
        
        
        infos = new JTextField(15);
        infos.setEditable( false );
        form.add(infos);
        // form.setSize( new Dimension(50, 100) );
        
        return form;
    }// end getFilterMenu
    
    
    public void showInfos(String info){
        infos.setText( info );
        if(infosTimer != null && infosTimer.isRunning()) infosTimer.stop();
        
        infosTimer = new Timer(10000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                infos.setText( "" );
            }    
        });
        infosTimer.start();
    }
}// end class
