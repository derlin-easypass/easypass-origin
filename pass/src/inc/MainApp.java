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
    
    private String pathToSessionsFolder; // depends on the implementation
                                         // (APPDATA or user.home)
    private String appName = "easypass";
    private String logFile = appName + ".log";
    
    private int winHeight = 300; // dimensions of the main frame
    private int winWidth = 480;
    
    private JPanel mainContainer; // main container (BorderLayout)
    private JvUndoManager undoManager;
    private TableRowSorter<PassTableModel> sorter; // used for the search bar
                                                   // ("find")
    private JTextField filterText; // text entered by the user, used to filter
                                   // the table cells
    private JTextField infos; // informations bar (data have been saved, for
                              // example)
    private Timer infosTimer; // used to hide infos after x seconds
    
    private PassTableModel model; // containing the datas, the object
                                  // serialized
    private PassTable table; // the jtable
    private JScrollPane scrollPane; // scrollPane for the JTable
    private SessionManager sessionManager;
    
    private String[] columnNames = { "account", "email address", "password",
            "notes" }; // the headers for the jtable
    
    
    public static void main( String[] args ) {
        MainApp ma = new MainApp();
    }
    
    
    /********************************************************************
     * constructor, acts as a main method /
     ********************************************************************/
    
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
        pathToSessionsFolder = this.getSessionPath();
        
        handleCredentialsAndLoadSession();
        
        // creates the main container
        mainContainer = new JPanel( new BorderLayout() );
        mainContainer.add( this.getRowsManipulationMenu(), BorderLayout.NORTH );
        mainContainer.setOpaque( false );
        
        // creates the jtable
        try{
            
            table = new PassTable( model );
            
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
        
        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
                    @Override
                    public void eventDispatched( AWTEvent evt ) {
                        if( evt.getID() != MouseEvent.MOUSE_CLICKED ){
                            return;
                        }
                        
                        MouseEvent event = (MouseEvent) evt;
                        int row = table.rowAtPoint( event.getPoint() );
                        if( row == -1
                                || !( event.getSource() instanceof PassTable ) ){
                            if( table.isEditing() )
                                table.getCellEditor().stopCellEditing();
                            table.clearSelection();
                            // }else if(! (event.ge) ){
                            // TODO
                        }
                    }
                    
                }, AWTEvent.MOUSE_EVENT_MASK );
        
        this.pack();
        this.setMinimumSize( new Dimension( winWidth, winHeight ) );
        this.setJMenuBar( this.getJFrameMenu() );
        this.setKeyboardShortcuts();
        this.setVisible( true );
        
    }// end constructor
    
    
    /********************************************************************
     * interaction with the user (save data, load session, show infos) /
     ********************************************************************/
    
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
        sessionManager = new SessionManager( this.pathToSessionsFolder );
        
        try{
            
            modal = new SessionAndPassFrame( this,
                    sessionManager.availableSessions() );
            
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
            String session = modal.getSession();
            
            try{
                
                if( !sessionManager.sessionExists( session ) ){
                    model = new PassTableModel( columnNames );
                    sessionManager.createSession( session, pass );
                    System.out.println( "no file" );
                    return;
                }
                
                // try to open the session and loads the encrypted data
                ArrayList<Object[]> data = (ArrayList<Object[]>) sessionManager
                        .openSession( session, pass );
                
                // loads the data and gives it to a new jtable model instance
                model = new PassTableModel( columnNames, data );
                
                System.out.println( "deserialization ok" );
                break;
                
            }catch( Exceptions.WrongCredentialsException e ){
                // if the pass was wrong, loops again
                System.out
                        .println( "wrong parameters : could not retrieve iv and datas" );
                // Functionalities.writeLog( "info: " + e.toString(),
                // pathToSessionsFolder + "\\" + logFile );
                continue;
            }catch( Exception e ){
                // otherwise, writes the exception to the log file and quit
                System.out.println( "unplanned exception" );
                e.printStackTrace();
                // Functionalities.writeLog( "severe: " + e.toString(),
                // pathToSessionsFolder + "\\" + logFile );
                System.exit( 0 );
            }
        }// end while
    }// end handleCredentials
    
    
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
                System.out.println( sessionManager.getDataPath() );
                if( sessionManager.save( (ArrayList<Object[]>) model.getData() ) ){
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
    
    
    /**
     * displays informations in the uneditable jtextfield of the downMenu. The
     * informations will be displayed for x milliseconds before disappearing
     * 
     * @param info
     */
    public void showInfos( String info, int ms ) {
        infos.setText( info );
        if( infosTimer != null && infosTimer.isRunning() )
            infosTimer.stop();
        
        infosTimer = new Timer( ms, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                infos.setText( "" );
            }
        } );
        infosTimer.start();
    }
    
    
    /**
     * opens a filechooser window to let the user choose a txt file. Used mainly
     * for saving the table data in clear json format ( see writeIlFile method
     * from JsonManager and the "save as json" option in the upper menu )
     * 
     * @return
     */
    public File showTxtFileChooser() {
        
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
        
    }// end showTxtfilechooser
    
    
    /********************************************************************
     * sets the closing operation and the keyboard shortcuts /
     ********************************************************************/
    
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
     * adds the different keyboards shortcuts to the jpanel
     * 
     * Shortcuts : ESC - to close the application; CTRL+F - focus on the search
     * bar; CTRL+N - new row; DEL - delete selected rows;
     */
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
                table.getCellEditor().stopCellEditing();
                model.addRow();
            }
        } );
        
        // CTRL+D to delete selected rows
        KeyStroke DelLineKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( DelLineKeyStroke, "DELLINE" );
        
        this.getRootPane().getActionMap().put( "DELLINE", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                
                table.getCellEditor().stopCellEditing();
                int[] selectedRows = table.getSelectedRows();
                System.out.println( "\ndeleting rows:" );
                for( int i = 0; i < selectedRows.length; i++ ){
                    // row index minus i since the table size shrinks by 1
                    // at each iteration
                    model.deleteRow( selectedRows[ i ] - i );
                }
            }
        } );
        
    }// end setShortCuts
    
    
    /********************************************************************
     * getters (menus, sessionPath) /
     ********************************************************************/
    
    /**
     * gets the path to the sessions folder stored in
     * <user>/AppData/<appliName>/sessions under windows and
     * <user.home>/.<appliName>/sessions under Linux. Note : a new folder will
     * be created if it does not already exist
     * 
     * @return
     */
    public String getSessionPath() {
        
        System.out.println();
        String os = System.getProperty( "os.name" );
        String path = "";
        
        // depending on the os system, choose the best location to store session
        // data
        if( os.contains( "Linux" ) || os.contains( "Mac" ) ){
            path = System.getProperty( "user.home" ) + File.separator + "."
                    + appName;
        }else if( os.contains( "Windows" ) ){
            path = System.getenv( "APPDATA" ) + File.separator + appName;
            
        }else{
            System.out.println( "os " + os + " not supported." );
            System.exit( 0 );
        }
        
        System.out.println( path );
        File sessionDir = new File( path );
        
        // if the session folder does not exist, creates it
        if( !sessionDir.exists() || !sessionDir.isDirectory() ){
            if( sessionDir.mkdir() ){
                path += File.separator + "sessions";
                if( new File( path ).mkdir() ){
                    return path;
                }
            }
            // an error occurred
            System.out.println( "error appdata" );
            return "";
            
        }else{ // the session folder exists, return its path
            return path + File.separator + "sessions";
        }
    }
    
    
    /**
     * returns the window menu. Menus :
     * 
     * options : save, save as json, print, open a new session
     * 
     * edit : undo, redo
     * 
     * @return
     */
    public JMenuBar getJFrameMenu() {
        
        // Where the GUI is created:
        JMenuBar menuBar;
        JMenu menu, editMenu;
        JMenuItem saveSubMenu, jsonSubMenu, printSubMenu, undoSubMenu, redoSubMenu, newSessionSubMenu;
        
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
                table.getCellEditor().stopCellEditing();
                
                try{
                    if( sessionManager.save( (ArrayList<Object[]>) model
                            .getData() ) ){
                        System.out.println( "datas serialized" );
                        showInfos( "data saved.", 10000 );
                    }else{
                        System.out.println( "data not saved" );
                        showInfos( "an error occurred! Data not saved...",
                                10000 );
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
                if( sessionManager.isOpened() )
                    sessionManager.close();
                table.setVisible( false );
                handleCredentialsAndLoadSession();
                table.setModel( model );
                table.updateUI();
                table.setVisible( true );
            }
        } );
        menu.add( newSessionSubMenu );
        
        // save as json menu
        jsonSubMenu = new JMenuItem( "export as Json", KeyEvent.VK_E );
        jsonSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E,
                ActionEvent.CTRL_MASK ) );
        jsonSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                File file = showTxtFileChooser();
                if( file != null ){
                    try{
                        sessionManager.writeAsJson( model.getData(), file );
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
        
    }// end getJFrameMenu
    
    
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
                System.out.println( "\ndeleting rows:" );
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
     * returns the searchbox to filter the data in the table (implementation
     * logic : see the setTableFilter method) as well as an uneditable
     * jtextfield used to display informations to the user (if data have been
     * saved for example)
     * 
     * @return
     */
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
        
        infos = new JTextField( 15 );
        infos.setEditable( false );
        form.add( infos );
        // form.setSize( new Dimension(50, 100) );
        
        return form;
    }// end getFilterMenu
    
    
    /********************************************************************
     * implementation of the "find" filter search bar /
     ********************************************************************/
    
    /**
     * Implements the search bar logic :updates the row filter regular
     * expression from the expression in the text box.
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
     * class used to filter the files selectables in the filechooser window.
     * 
     * @author lucy
     * 
     */
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
    }// end private class
    
}// end class
