package gui;

import main.thread.PassLock;
import manager.UndoManager;
import manager.SessionManager.Session;
import table.PassTable;
import table.PassTableModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class PassFrame extends JFrame {

    boolean debug = true;


    // filename
    Session session;
    PassLock lock;

    private JPanel mainContainer; // main container (BorderLayout)
    UndoManager undoManager;

    TableRowSorter<PassTableModel> sorter; // used for the search bar
    // ("find")
    JTextField filterText; // text entered by the user, used to filter
    // the table cells
    private JTextField rowCount;
    JTextField infos; // informations bar (data have been saved, for
    // example)
    private Timer infosTimer; // used to hide infos after x seconds
    final static int INFOS_DISPLAY_TIME = 6000; // in milliseconds,
    // delay before
    // resetting info text

    PassTable table; // the jtable
    private JScrollPane scrollPane; // scrollPane for the JTable


    /**
     * *****************************************************************
     * constructor, acts as a main method /
     * ******************************************************************
     */

    public PassFrame( Session session, PassLock lock ) {
        // initializes the main Frame
        super( "Easypass / " + System.getProperty( "user.name" ) + " / " + session.getName() );
        this.session = session;
        this.lock = lock;

        // sets the listener to save data on quit
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setWindowClosingListener();

        // sets position, size, etc
        int winWidth = ( Integer ) session.getConfigProperty( "window width" );
        int winHeight = ( Integer ) session.getConfigProperty( "window height" );

        this.setSize( new Dimension( winWidth, winHeight ) );
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int winX = ( screensize.width - winWidth ) / 2;
        int winY = ( screensize.height - winHeight ) / 2;
        this.setLocation( winX, winY );

        // creates the main container
        mainContainer = new JPanel( new BorderLayout() );

        // creates the jtable
        try {
            table = new PassTable( session.getModel() );

        } catch( Exception e ) {
            writeLog( "problem while filling the table with data" );
            e.printStackTrace();
        }// end try

        // sets the sizes of the JTable

        //table.setRowHeight( 30 );
        table.setStyle();
        table.setColSizes( ( int[] ) session.getConfigProperty( "column dimensions" ) );
        //settableColSizes();

        // sets the undo manager for CTRL Z
        undoManager = new UndoManager( table );
        this.session.getModel().addUndoableEditListener( undoManager );
        this.session.getModel().addTableModelListener( new TableModelListener() {

            @Override
            public void tableChanged( TableModelEvent e ) {
                updateDisplayedRowCount();
            }
        } );


        // adds scrollpane and JTable
        scrollPane = new JScrollPane( table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
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
        session.getModel().addTableModelListener( new TableModelListener() {

            @Override
            public void tableChanged( TableModelEvent e ) {
                updateDisplayedRowCount();

            }
        } );

        // updates the GUI and show the window
        mainContainer.updateUI();
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch( Exception e ) {
            writeLog( "problem loading default UIManager" );
        }

        // PassExcelAdapter adapter = new PassExcelAdapter( table );
        this.pack();
        this.setMinimumSize( new Dimension( winWidth, winHeight ) );
        //this.setJMenuBar( this.getJFrameMenu() );
        this.setJMenuBar( new PassMenuBar( this ) );
        this.setKeyboardShortcuts();
        this.setVisible( true );

    }// end constructor


   /* private void settableColSizes() {

        try {
            String[] dims = session.getConfigProperty( "dimensions" ).split( "," );
            int[] dimensions = new int[ dims.length ];

            for( int i = 0; i < dims.length; i++ ) {
                dimensions[ i ] = Integer.parseInt( dims[ i ].trim() );
            }// end for
            table.setColSizes( dimensions );
        } catch( Exception e ) {
            return;
        }// end try

    }// end proper */


    /********************************************************************
     * interaction with the user (save data, load session, show infos)
     ********************************************************************/

    /**
     * asks the user to choose the session to load and get his credentials. The
     * method then creates the cipher, deserializes the data and creates a
     * TableModel.
     * <p/>
     * If an error occurs : - either the problem comes from the credentials, so
     * it prompts the user to enter them again - or the problem is somewhere
     * else and the program exits (after logging the cause of the exception)
     */
    @SuppressWarnings("unchecked")
    /**
     * creates a JDialog asking the user if he wants to save before quit. The
     * method will return false only if the user clicked cancel.
     */
    private boolean askSaveData() {

        int answer = JOptionPane.showConfirmDialog( null, "Would you like to save the " +
                "modifications?", "save", JOptionPane.YES_NO_CANCEL_OPTION );

        if( answer == JOptionPane.YES_OPTION ) { // serializes and quits
            try {
                if( session.save() ) {
                    System.out.println( "datas serialized" );
                } else {
                    System.out.println( "data not saved" );
                }
            } catch( Exception e ) {
                System.out.println( "error in serialization. Possible data loss" );
                writeLog( "ERROR: " + e.getMessage() );
            }// end try

            return true;

        } else if( answer == JOptionPane.NO_OPTION ) { // just quit
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

        if( ms <= 0 ) {
            return;
        }

        if( infosTimer != null && infosTimer.isRunning() ) infosTimer.stop();

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
        chooser.setFileFilter( new FileFilter() {

            @Override
            public boolean accept( File file ) {
                return file.isDirectory() || file.getName().endsWith( ".txt" );
            }


            @Override
            public String getDescription() {
                return "Plain text document (*.txt)";
            }
        } );

        chooser.setCurrentDirectory( new File( "." ) );
        chooser.setDialogTitle( "select folder" );
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

        if( chooser.showSaveDialog( this ) == JFileChooser.APPROVE_OPTION ) {
            return chooser.getSelectedFile();
        } else {
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
     */
    public void setWindowClosingListener() {

        // adds a listener
        // asks the user if he wants to save data and quit, just quit, or resume
        this.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent we ) {

                // if something is modified, ask for saving
                if( session.getModel().isModified() && !askSaveData() ) {
                    //the user clicked cancel, so don't close window
                    return;
                }
                synchronized( lock ) {
                    setVisible( false );
                    lock.setMessage( PassLock.Message.DO_CLOSE );
                    lock.notify();
                }
            }
        } );
    }// end setWindowClosing


    /**
     * adds a mouselistener so that the table cells loose focus and stop editing
     * when the user clicks outside of the table
     */
    public void setMouseListener() {

        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener() {
            // TODO @Override
            public void eventDispatched( AWTEvent evt ) {
                if( evt.getID() != MouseEvent.MOUSE_CLICKED ) {
                    return;
                }

                MouseEvent event = ( MouseEvent ) evt;
                // if the click was outside the table, clear selection
                if( !( event.getSource() instanceof JTable ) || table.rowAtPoint( event.getPoint
                        () ) == -1 ) {
                    if( table.isEditing() ) table.getCellEditor().stopCellEditing();
                    table.clearSelection();
                }
            }

        }, AWTEvent.MOUSE_EVENT_MASK );
    }// end setMouseListener


    /**
     * adds the different keyboards shortcuts to the jpanel
     * <p/>
     * Shortcuts : ESC - to close the application; CTRL+F - focus on the search
     * bar; CTRL+N - new row; DEL - delete selected rows;
     */
    @SuppressWarnings("serial")
    public void setKeyboardShortcuts() {

        // escape event closes window
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0, false );

        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( escapeKeyStroke,
                "ESCAPE" );

        this.getRootPane().getActionMap().put( "ESCAPE", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {

                // stops editing
                if( table.isEditing() ) {
                    table.getCellEditor().stopCellEditing();
                }

                // if everything was saved, quits
                if( !session.getModel().isModified() ) {
                    System.exit( 0 );
                }

                // if unsaved modifications, asks to save before quitting
                if( askSaveData() ) {
                    System.exit( 0 );
                }
            }
        } );

        // CTRL+F to put focus on the filter/find textfield
        KeyStroke ctrlFKeyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );

        this.getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( ctrlFKeyStroke,
                "FIND" );

        this.getRootPane().getActionMap().put( "FIND", new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                filterText.requestFocusInWindow();
                filterText.selectAll();
            }
        } );

    }// end setShortCuts


    /********************************************************************
     * getters (menus, session$path) /
     ********************************************************************/


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
                session.getModel().addRow();
            }
        } );

        upperMenu.add( addJB );

        // creates the delete button
        JButton delJB = new JButton( "delete selected rows" );
        delJB.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int[] selectedRows = table.getSelectedRows();
                for( int i = 0; i < selectedRows.length; i++ ) {
                    // row index minus i since the table size shrinks by 1
                    // everytime
                    session.getModel().deleteRow( selectedRows[ i ] - i );
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

        sorter = new TableRowSorter<PassTableModel>( session.getModel() );
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
                updateDisplayedRowCount();
            }


            public void insertUpdate( DocumentEvent e ) {
                setTableFilter();
                updateDisplayedRowCount();
            }


            public void removeUpdate( DocumentEvent e ) {
                setTableFilter();
                updateDisplayedRowCount();
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


    /**
     * *****************************************************************
     * implementation of the "find" filter search bar /
     * ******************************************************************
     */

    public void updateDisplayedRowCount() {
        rowCount.setText( "rows: " + sorter.getViewRowCount() + "/" + session.getModel()
                .getRowCount() + "  " );
    }


    /**
     * Implements the search bar logic :updates the row filter regular
     * expression from the expression in the text box.
     */
    public void setTableFilter() {
        RowFilter<PassTableModel, Object> rf = null;
        ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object, Object>>();

        try {
            String text = filterText.getText();
            String[] textArray = text.split( " " );

            for( String aTextArray : textArray ) {
                rfs.add( RowFilter.regexFilter( "(?i)" + aTextArray, 0, 1, 2, 3, 4 ) );
            }//end for

            rf = RowFilter.andFilter( rfs );

        } catch( java.util.regex.PatternSyntaxException e ) {
            return;
        }

        sorter.setRowFilter( rf );
        updateDisplayedRowCount();
    }


    /**
     * appends a message to a [log] file.
     *
     * @param message the message to write
     */

    public void writeLog( String message ) {
        try {

            // true = append to file
            BufferedWriter writer = new BufferedWriter( new FileWriter( ( String ) session
                    .getConfigProperty( "logfile path" ), true ) );

            // adds the date and the message at the end of the file
            writer.newLine();
            writer.write( new Date().toString() + " " + message ); // +
            // System.getProperty("line.separator"));
            writer.flush();
            writer.close();
            System.out.println( "logging Done" );

        } catch( IOException e ) {
            e.printStackTrace();
        }// end try
    }// end writeLog



}// end class
