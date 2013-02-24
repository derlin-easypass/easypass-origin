package inc;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import models.Exceptions;
import models.Exceptions.ConfigFileNotFoundException;
import models.Exceptions.NoSuchSettingException;
import dialogs.RefactorSessionDialog;
import dialogs.SessionAndPassFrame2;

public class Easypass extends JFrame {
    
    boolean debug = false;
    protected static final String APPLICATION_NAME = "easypass";
    
    // filename
    
    private JPanel mainContainer; // main container (BorderLayout)
    private JvUndoManager undoManager;
    private PassConfigManager configManager;
    private TableRowSorter<PassTableModel> sorter; // used for the search bar
                                                   // ("find")
    private JTextField filterText; // text entered by the user, used to filter
                                   // the table cells
    private JTextField rowCount;
    private JTextField infos; // informations bar (data have been saved, for
                              // example)
    private Timer infosTimer; // used to hide infos after x seconds
    private final static int INFOS_DISPLAY_TIME = 6000; // in milliseconds,
                                                        // delay before
                                                        // resetting info text
    
    private PassTableModel model; // containing the datas, the object
                                  // serialized
    private PassTable table; // the jtable
    private JScrollPane scrollPane; // scrollPane for the JTable
    private SessionManager sessionManager;
    
    private int winWidth, winHeight;
    private String[] columnNames;
    
    
    public static void main( String[] args ) {
        new Easypass();
    }
    
    
    /********************************************************************
     * constructor, acts as a main method /
     ********************************************************************/
    
    public Easypass() {
        // initializes the main Frame
        super( "accounts and passwords" );
        // sets the listener to save data on quit
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setWindowClosingListener();
        
        // get config
        initConfigManager();
        checkConfig();
        
        // sets position, size, etc
        this.setSize( new Dimension( winWidth, winHeight ) );
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winX = ( screensize.width - winWidth ) / 2;
        int winY = ( screensize.height - winHeight ) / 2;
        this.setLocation( winX, winY );
        
        handleCredentialsAndLoadSession();
        
        // creates the main container
        mainContainer = new JPanel( new BorderLayout() );
        
        // creates the jtable
        try{
            
            table = new PassTable( model );
            
        }catch( Exception e ){
            writeLog( "problem while filling the table with data" );
            e.printStackTrace();
        }// end try
        
        // sets the sizes of the JTable
        table.setAutoCreateRowSorter( true );
        table.setFillsViewportHeight( true );
        table.setRowHeight( 30 );
        table.setStyle();
        settableColSizes();
        
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
        
        // adds mouselistener to make the table loose focus when click occurs
        // outside
        this.setMouseListener();
        
        // adds a listener to update the displayed row count when the table
        // changes
        table.getModel().addTableModelListener( new TableModelListener() {
            
            @Override
            public void tableChanged( TableModelEvent e ) {
                updateDisplayedRowCount();
                
            }
        } );
        
        // updates the GUI and show the window
        mainContainer.updateUI();
        try{
            UIManager
                    .setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }catch( Exception e ){
            writeLog( "problem loading default UIManager" );
        }
        
        // PassExcelAdapter adapter = new PassExcelAdapter( table );
        this.pack();
        this.setMinimumSize( new Dimension( winWidth, winHeight ) );
        this.setJMenuBar( this.getJFrameMenu() );
        this.setKeyboardShortcuts();
        this.setVisible( true );
        
    }// end constructor
    
    
    private void initConfigManager() {
        
        try{
            this.configManager = new PassConfigManager();
        }catch( ConfigFileNotFoundException e ){
            JOptionPane.showMessageDialog( this,
            
            "Default settings not found. Exiting...", "configuration error",
                    JOptionPane.ERROR_MESSAGE );
            System.exit( 1 );
        }// end try
        
        File appfolder = new File(
                this.configManager.getProperty( "application path" ) );
        
        // if the session folder does not exist, creates it
        if( !appfolder.exists() || !appfolder.isDirectory() )
            appfolder.mkdir();
        
    }// end initConfigManager
    
    /********************************************************************
     * handle configuration files
     ********************************************************************/
    
    private void checkConfig() {
        
        try{
            // ---------- checks that the application folder exists, or creates
            // it
            File appfolder = new File(
                    this.configManager.getProperty( "application path" ) );
            
            if( ( !appfolder.exists() || !appfolder.isDirectory() )
                    && !appfolder.mkdir() )
                throw new Exception( "application path could not be found" );
            
            // ----------- checks the session path
            File sessionfolder = new File(
                    this.configManager.getProperty( "session path" ) );
            
            if( ( !sessionfolder.exists() || !sessionfolder.isDirectory() )
                    && !sessionfolder.mkdir() )
                throw new Exception( "session path could not be found" );
            
            // ----------- checks the logfile path
            File logfile = new File( configManager.getProperty( "log filepath" ) );
            // if file doesn't exists, creates it
            if( ( !logfile.exists() || !logfile.isFile() )
                    && !logfile.createNewFile() )
                throw new Exception( "log filepath could not be found" );
            
            // ------------checks the window width and height
            try{
                winWidth = Integer.parseInt( configManager
                        .getProperty( "window width" ) );
                winHeight = Integer.parseInt( configManager
                        .getProperty( "window height" ) );
            }catch( Exception e ){
                throw new Exception(
                        "error parsing 'window height' and 'window width' properties" );
            }
            
            // ------------checks the column names
            try{
                this.columnNames = configManager.getProperty( "column names" )
                        .split( "," );
                for( int i = 0; i < this.columnNames.length; i++ ){
                    this.columnNames[ i ] = this.columnNames[ i ].trim();
                }// end for
            }catch( Exception e ){
                throw new Exception(
                        "'column names' property could not be found" );
            }
            
        }catch( Exception e ){
            JOptionPane
                    .showMessageDialog(
                            this,
                            "Error in configuration file : \n       "
                                    + e.getMessage()
                                    + "\nplease verify your settings or delete your custom configuration file",
                            "configuration error", JOptionPane.ERROR_MESSAGE );
            System.exit( 1 );
        }// end try
    }// end checkConfig
    
    
    private void settableColSizes() {
        
        try{
            String[] dims = configManager.getProperty( "dimensions" ).split(
                    "," );
            int[] dimensions = new int[dims.length];
            
            for( int i = 0; i < dims.length; i++ ){
                dimensions[ i ] = Integer.parseInt( dims[ i ].trim() );
            }// end for
            table.setColSizes( dimensions );
        }catch( Exception e ){
            return;
        }// end try
        
    }// end proper
    
    
    /********************************************************************
     * interaction with the user (save data, load session, show infos)
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
        
        SessionAndPassFrame2 modal = null;
        sessionManager = new SessionManager(
                configManager.getProperty( "session path" ) );
        
        try{
            
            modal = new SessionAndPassFrame2( this,
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
                modal.dispose();
                System.exit( 0 );
            }
            // get pass and salt
            String pass = modal.getPass();
            String session = modal.getSession();
            
            try{
                
                ArrayList<Object[]> data;
                
                if( modal.isImported() ){
                    data = (ArrayList<Object[]>) sessionManager.importSession(
                            session, pass );
                    
                }else if( !sessionManager.sessionExists( session ) ){
                    model = new PassTableModel( columnNames );
                    sessionManager.createSession( session, pass );
                    modal.dispose();
                    return;
                }else{
                    
                    // try to open the session and loads the encrypted data
                    data = (ArrayList<Object[]>) sessionManager.openSession(
                            session, pass );
                }
                
                // loads the data and gives it to a new jtable model instance
                model = new PassTableModel( columnNames, data );
                
                this.setTitle( this.getTitle() + ": "
                        + sessionManager.getSessionName() );
                System.out.println( "deserialization ok" );
                break;
                
            }catch( Exceptions.WrongCredentialsException e ){
                // if the pass was wrong, loops again
                JOptionPane.showMessageDialog( this, e.getMessage(),
                        "open error", JOptionPane.ERROR_MESSAGE );
                writeLog( "info: " + e.getMessage() );
                continue;
            }catch( Exceptions.ImportException e ){
                // if the pass was wrong, loops again
                JOptionPane.showMessageDialog( this, e.getMessage(),
                        "import error", JOptionPane.ERROR_MESSAGE );
                writeLog( "info: " + e.toString() );
                continue;
            }catch( Exception e ){
                // otherwise, writes the exception to the log file and quit
                System.out.println( "unplanned exception" );
                e.printStackTrace();
                writeLog( "severe: " + e.toString() );
                System.exit( 0 );
            }
        }// end while
        
        modal.dispose();
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
                if( sessionManager.save( (ArrayList<Object[]>) model.getData() ) ){
                    System.out.println( "datas serialized" );
                }else{
                    System.out.println( "data not saved" );
                }
            }catch( Exception e ){
                System.out
                        .println( "error in serialization. Possible data loss" );
                writeLog( "ERROR: " + e.getMessage() );
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
        
        if( ms <= 0 ){
            return;
        }
        
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
                
                // if everything is saved, quits
                if( !model.isModified() ){
                    System.exit( 0 );
                }
                
                // if unsaved modifications, asks to save them
                if( askSaveData() ){
                    System.exit( 0 );
                }
            }
        } );
    }// end setWindowClosing
    
    
    /**
     * adds a mouselistener so that the table cells loose focus and stop editing
     * when the user clicks outside of the table
     */
    public void setMouseListener() {
        
        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
                    // TODO @Override
                    public void eventDispatched( AWTEvent evt ) {
                        if( evt.getID() != MouseEvent.MOUSE_CLICKED ){
                            return;
                        }
                        
                        MouseEvent event = (MouseEvent) evt;
                        // if the click was outside the table, clear selection
                        if( !( event.getSource() instanceof JTable )
                                || table.rowAtPoint( event.getPoint() ) == -1 ){
                            if( table.isEditing() )
                                table.getCellEditor().stopCellEditing();
                            table.clearSelection();
                        }
                    }
                    
                }, AWTEvent.MOUSE_EVENT_MASK );
    }// end setMouseListener
    
    
    /**
     * adds the different keyboards shortcuts to the jpanel
     * 
     * Shortcuts : ESC - to close the application; CTRL+F - focus on the search
     * bar; CTRL+N - new row; DEL - delete selected rows;
     */
    @SuppressWarnings("serial")
    public void setKeyboardShortcuts() {
        
        // escape event closes window
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE,
                0, false );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( escapeKeyStroke, "ESCAPE" );
        
        this.getRootPane().getActionMap().put( "ESCAPE", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                
                // stops editing
                if( table.isEditing() ){
                    table.getCellEditor().stopCellEditing();
                }
                
                // if everything was saved, quits
                if( !model.isModified() ){
                    System.exit( 0 );
                }
                
                // if unsaved modifications, asks to save before quitting
                if( askSaveData() ){
                    System.exit( 0 );
                }
            }
        } );
        
        // CTRL+F to put focus on the filter/find textfield
        KeyStroke ctrlFKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
        
        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
                .put( ctrlFKeyStroke, "FIND" );
        
        this.getRootPane().getActionMap().put( "FIND", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                filterText.requestFocusInWindow();
                filterText.selectAll();
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
        
        String path;
        
        // try to retrieve the sessionpath from the config file
        path = this.configManager.getProperty( "session path" );
        
        File sessionfolder = new File( path );
        
        if( sessionfolder.exists() && sessionfolder.isDirectory() )
            return path;
        
        // ------------if does not exists, creates the session folder in default
        // location
        
        sessionfolder = new File(
                this.configManager.getProperty( "application path" )
                        + File.separator + "sessions" );
        
        if( !sessionfolder.exists() || !sessionfolder.isDirectory() )
            sessionfolder.mkdir();
        
        // the session folder exists, return its path
        return sessionfolder.getAbsolutePath();
        
    }// end getSessionPath
    
    
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
        JMenu optionsMenu, editMenu, sessionMenu;
        Insets inset = new Insets( 2, 7, 2, 7 );
        JMenuItem saveSubMenu, jsonSubMenu, printSubMenu;
        JMenuItem newSessionSubMenu, deleteSessionSubMenu, refactorSessionSubMenu;
        JMenuItem undoSubMenu, redoSubMenu, addRowSubMenu, deleteRowSubMenu;
        
        // Create the menu bar.
        menuBar = new JMenuBar();
        
        // --------------------------- Build the option menu.
        optionsMenu = new JMenu( "options" );
        optionsMenu.setMnemonic( KeyEvent.VK_A );
        optionsMenu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items" );
        
        // save option
        saveSubMenu = new JMenuItem( "save", KeyEvent.VK_T );
        saveSubMenu.setMargin( inset );
        saveSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        saveSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                // stops current editing
                if( table.isEditing() ){
                    table.getCellEditor().stopCellEditing();
                }
                
                // if no modification to save, returns
                if( !model.isModified() ){
                    showInfos( "everything up to date.", INFOS_DISPLAY_TIME );
                    return;
                }
                
                try{
                    // saves data
                    if( sessionManager.save( (ArrayList<Object[]>) model
                            .getData() ) ){
                        System.out.println( "datas serialized" );
                        showInfos( "data saved.", INFOS_DISPLAY_TIME );
                        model.resetModified();
                        
                    }else{
                        System.out.println( "data not saved" );
                        showInfos( "an error occurred! Data not saved...",
                                INFOS_DISPLAY_TIME );
                    }
                }catch( Exception ee ){
                    System.out
                            .println( "error in serialization. Possible data loss" );
                    showInfos( "an error occurred! Data not saved...",
                            INFOS_DISPLAY_TIME );
                    ee.printStackTrace();
                }// end try
            }
        } );
        optionsMenu.add( saveSubMenu );
        
        // save as json menu
        jsonSubMenu = new JMenuItem( "export as Json", KeyEvent.VK_E );
        jsonSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        jsonSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                if( table.isEditing() ){
                    table.getCellEditor().stopCellEditing();
                }
                
                if( table.getSelectedRowCount() > 0 ){
                    table.clearSelection();
                }
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
        optionsMenu.add( jsonSubMenu );
        
        // print subMenu
        optionsMenu.addSeparator();
        printSubMenu = new JMenuItem( "print" );
        printSubMenu.setMnemonic( KeyEvent.VK_P );
        printSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        
        printSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try{
                    table.print( JTable.PrintMode.NORMAL );
                }catch( PrinterException pe ){
                    System.err.println( "Error printing: " + pe.getMessage() );
                }
            }
        } );
        
        optionsMenu.add( printSubMenu );
        
        // -------------------------build menu to manage session
        sessionMenu = new JMenu( "session" );
        sessionMenu.setMargin( inset );
        // open a new session menu
        newSessionSubMenu = new JMenuItem( "open new session" );
        
        newSessionSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                if( sessionManager.isOpened() )
                    sessionManager.closeSession();
                
                setVisible( false );
                
                // open a new session
                handleCredentialsAndLoadSession();
                table.setModel( model );
                table.updateUI();
                setVisible( true );
            }
        } );
        sessionMenu.add( newSessionSubMenu );
        
        // refactor current session
        refactorSessionSubMenu = new JMenuItem( "rename session" );
        refactorSessionSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                RefactorSessionDialog dialog = new RefactorSessionDialog( null );
                // if the user closed the dialog or clicked cancel, simply
                // returns
                if( dialog.getStatus() == false ){
                    return;
                }
                
                try{
                    sessionManager.refactorSession( dialog.getSessionName(),
                            dialog.getPass() );
                    showInfos( "refactoring finished successfully.",
                            INFOS_DISPLAY_TIME );
                    
                }catch( Exceptions.RefactorException ex ){
                    showInfos( ex.getMessage(), INFOS_DISPLAY_TIME );
                }
                
            }
        } );
        sessionMenu.add( refactorSessionSubMenu );
        
        // delete session
        // TODO
        deleteSessionSubMenu = new JMenuItem( "delete session" );
        
        deleteSessionSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                if( JOptionPane.showConfirmDialog( null,
                        "are you sure you want to permanently delete session \""
                                + sessionManager.getSessionName() + "\" ?",
                        "delete session", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ){
                    
                    sessionManager.deleteSession();
                    
                    setVisible( false );
                    
                    // open a new session
                    handleCredentialsAndLoadSession();
                    table.setModel( model );
                    table.updateUI();
                    setVisible( true );
                }
            }
        } );
        sessionMenu.add( deleteSessionSubMenu );
        
        // --------------------------- Build edit menu in the menu bar.
        editMenu = new JMenu( "edit" );
        editMenu.setMargin( inset );
        
        // adds the add row subMenu
        addRowSubMenu = new JMenuItem( "add row" );
        addRowSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        addRowSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                model.addRow();
                
                // resets the filters --> shows all rows (global view)
                filterText.setText( "" );
                
                // sets focus on the new row
                int lastRow = model.getRowCount() - 1;
                
                // scrolls to the bottom of the table
                table.getSelectionModel().setSelectionInterval( lastRow,
                        lastRow );
                table.scrollRectToVisible( new Rectangle( table.getCellRect(
                        lastRow, 0, true ) ) );
            }
        } );
        
        editMenu.add( addRowSubMenu );
        
        // adds the delete selected rows subMenu
        deleteRowSubMenu = new JMenuItem( "delete selected rows" );
        deleteRowSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        
        deleteRowSubMenu.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if( table.isEditing() ){
                    table.getCellEditor().stopCellEditing();
                }
                int[] selectedRows = table.getSelectedRows();
                for( int i = 0; i < selectedRows.length; i++ ){
                    // row index minus i since the table size shrinks by 1
                    // everytime. Also converts the row indexes since the table
                    // can be sorted/filtered
                    model.deleteRow( table
                            .convertRowIndexToModel( selectedRows[ i ] - i ) );
                }
            }
        } );
        editMenu.add( deleteRowSubMenu );
        
        // add undo submenu
        undoSubMenu = new JMenuItem( undoManager.getUndoAction() );
        undoSubMenu.setMnemonic( KeyEvent.VK_Z );
        undoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        
        // add redo submenu
        redoSubMenu = new JMenuItem( undoManager.getRedoAction() );
        redoSubMenu.setMnemonic( KeyEvent.VK_Y );
        redoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        editMenu.add( undoSubMenu );
        editMenu.add( redoSubMenu );
        
        // adds the menu to the menubar
        menuBar.add( optionsMenu );
        menuBar.add( sessionMenu );
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
        // Whenever filterText changes, invokes newFilter.
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
        infos.setBorder( null );
        infos.setOpaque( false );
        infos.setEditable( false );
        infos.setFocusable( false );
        form.add( infos );
        
        // to display the row count at the bottom of the frame
        rowCount = new JTextField( 12 );
        rowCount.setHorizontalAlignment( JTextField.RIGHT );
        rowCount.setBorder( null );
        rowCount.setOpaque( true );
        rowCount.setEditable( false );
        rowCount.setFocusable( false );
        updateDisplayedRowCount();
        form.add( rowCount );
        
        return form;
    }// end getFilterMenu
    
    
    /********************************************************************
     * implementation of the "find" filter search bar /
     ********************************************************************/
    
    public void updateDisplayedRowCount() {
        rowCount.setText( "rows: " + sorter.getViewRowCount() + "/"
                + model.getRowCount() + "  " );
    }
    
    
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
        updateDisplayedRowCount();
    }
    
    
    /**
     * 
     * appends a message to a [log] file.
     * 
     * @param message
     *            the message to write
     * 
     * @param logFilePath
     *            the path to the file
     */
    
    public void writeLog( String message ) {
        try{
            
            // true = append to file
            BufferedWriter writer = new BufferedWriter( new FileWriter(
                    configManager.getProperty( "log filepath" ), true ) );
            
            // adds the date and the message at the end of the file
            writer.newLine();
            writer.write( new Date().toString() + " " + message ); // +
                                                                   // System.getProperty("line.separator"));
            writer.flush();
            writer.close();
            System.out.println( "logging Done" );
            
        }catch( IOException e ){
            e.printStackTrace();
        }// end try
    }// end writeLog
    
    
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
