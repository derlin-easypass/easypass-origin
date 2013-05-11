package gui;

import main.thread.PassLock;
import manager.SessionManager.Session;
import manager.UndoManager;
import table.PassTable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class PassFrame extends JFrame {

    boolean debug = true;


    // filename
    Session session;
    PassLock lock;

    private JPanel mainContainer; // main container (BorderLayout)
    UndoManager undoManager;

    JTextField filterText; // text entered by the user, used to filter
    // the table cells
    private JLabel rowCount;
    JLabel infos; // informations bar (data have been saved, for
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


        java.net.URL url = ClassLoader.getSystemResource( "resources/easypass_favicon.ico" );
        Image img = Toolkit.getDefaultToolkit().createImage(url);
        setIconImage( img );

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
        this.setMinimumSize( new Dimension( 400, 300 ) );
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
    public void showInfos( String info ) {
        infos.setText( info );

        if( infosTimer != null && infosTimer.isRunning() ) infosTimer.stop();

        infosTimer = new Timer( INFOS_DISPLAY_TIME, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                infos.setText( "" );
            }
        } );

        infosTimer.start();
    }


    public void clearFilterText() {
        filterText.setText( "" );
        updateDisplayedRowCount();
    }//end clearFilterText

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
                    table.stopEditing();
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
                table.stopEditing();

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
     * returns the searchbox to filter the data in the table (implementation
     * logic : see the setTableFilter method) as well as an uneditable
     * jtextfield used to display informations to the user (if data have been
     * saved for example)
     *
     * @return
     */
    public JPanel getDownMenu() {

        // Create a separate form for filterText and statusText
        JPanel panel = new JPanel(new BorderLayout(  ));
        JPanel findPanel = new JPanel(  );
        JLabel l1 = new JLabel( "Find :" );

        filterText = new JTextField( 30 );
        // Whenever filterText changes, invokes newFilter.
        filterText.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate( DocumentEvent e ) {
                table.setTableFilter(filterText.getText());
                updateDisplayedRowCount();
            }


            public void insertUpdate( DocumentEvent e ) {
                table.setTableFilter(filterText.getText());
                updateDisplayedRowCount();
            }


            public void removeUpdate( DocumentEvent e ) {
                table.setTableFilter(filterText.getText());
                updateDisplayedRowCount();
            }
        } );
        l1.setLabelFor( filterText );
        findPanel.add( l1 );
        findPanel.add( filterText );

        panel.add( findPanel, BorderLayout.WEST  );

        infos = new JLabel(  );
        infos.setPreferredSize( new Dimension( 200, infos.getHeight() ) );
        panel.add( infos, BorderLayout.CENTER );

        // to display the row count at the bottom of the frame
        rowCount = new JLabel(  );
        //rowCount.setHorizontalAlignment( JTextField.RIGHT );
        updateDisplayedRowCount();
        panel.add( rowCount, BorderLayout.EAST );

        return panel;
    }// end getFilterMenu


    /**
     * *****************************************************************
     * implementation of the "find" filter search bar /
     * ******************************************************************
     */

    public void updateDisplayedRowCount() {
        rowCount.setText( "rows: " + table.getViewRowCount() + "/" + session.getModel()
                .getRowCount() + "  " );
    }

    /**
     * appends a message to a [log] file.
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
