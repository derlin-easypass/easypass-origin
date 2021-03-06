package ch.derlin.easypass.main;

import ch.derlin.easypass.dialogs.OpenSessionDialog;
import ch.derlin.easypass.gui.PassFrame;
import ch.derlin.easypass.main.thread.PassLock;
import ch.derlin.easypass.manager.PassConfigContainer;
import ch.derlin.easypass.manager.SessionManager;
import ch.derlin.easypass.manager.SessionManager.Session;
import ch.derlin.easypass.models.AbstractConfigContainer;
import ch.derlin.easypass.models.ConfigFileManager;
import ch.derlin.easypass.models.Exceptions;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * User: lucy Date: 02/03/13
 */
public class Main {

    public static final String APPLICATION_NAME = "easypass";
    public static final String CONFIG_PATH = "config.json";
    private AbstractConfigContainer config;
    private SessionManager sessionManager;
    private int runningWindowsCount;
    private boolean running = true;
    private PassLock lock;
    public static boolean debug = true;


    public static void main( String[] args ) {
        new Main();
    } // end ch.derlin.easypass.main


    public Main() {
        // get config
        try {
            System.out.println( CONFIG_PATH );
            initConfig();
        } catch( Exceptions.ConfigFileNotFoundException e ) {
            JOptionPane.showMessageDialog( null, "Default settings not found!",
                    "configuration error", JOptionPane.ERROR_MESSAGE );
        }

        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        } catch( Exception e ) {

        }

        this.sessionManager = new SessionManager( this.config );
        this.runningWindowsCount = 0;
        this.lock = new PassLock();

        while( running ) {
            PassFrame frame = new SessionWindow().launchGUI();
            if( debug ) System.out.printf( "launchGUI returned." );
            try {
                synchronized( lock ) {
                    lock.wait();
                }
            } catch( InterruptedException e ) {
                if( debug ) System.out.println( "ch.derlin.easypass.main thread interrupted" );
                System.out.println( "interrupt" );
            }

            switch( lock.getMessage() ) {
                case DO_CLOSE:
                    this.running = false;

                case DO_OPEN_SESSION:
                    frame.dispose();

            }
        }

    }


    protected synchronized void incrementRunningThreadCount() {
        this.runningWindowsCount++;
    }


    protected synchronized void decrementRunningThreadCount() {
        this.runningWindowsCount--;
    }


    /**
     * *****************************************************************
     * Thread for launching a GUI session window
     * ******************************************************************
     */
    class SessionWindow {

        public PassFrame launchGUI() {

            if( debug ) {
                System.out.println( "\nsessions location : " + config.getProperty( "session path"
                ) );
            }

            Session session = handleSessionDialog();
            PassFrame frame = new PassFrame( session, lock );
            incrementRunningThreadCount();
            if( debug ) System.out.println( "GUI launched" );
            return frame;
        } // end launchGUI
    }


    public Session handleSessionDialog() throws Exceptions.NotInitializedException {

        OpenSessionDialog modal = null;
        Session session = null;

        try {

            modal = new OpenSessionDialog( null, sessionManager );
            modal.addWindowListener( new WindowAdapter() {

                @Override
                public void windowClosing( WindowEvent e ) {
                    System.exit( 0 );
                }
            } );


            // asks for pass with a dialog and loads the serialized datas
            // if the pass is wrong, ask again. If the user cancels, quits the
            // application

            modal.setVisible( true );

            //TODO
            if(modal.isCanceled()) System.exit( 0 );

            // get pass and salt
            String pass = modal.getPass();
            String name = modal.getSession();

            //TODO
            if(modal.isCanceled()){
                System.exit( 0 );
            }

            if( modal.isImported() ) {
                session = sessionManager.importSession( name, pass );

            } else if( !sessionManager.sessionExists( name ) ) {
                session = sessionManager.createSession( name, pass );


            } else {
                // try to open the session and loads the encrypted data
                session = sessionManager.openSession( name, pass );

            }

        } catch( Exceptions.WrongCredentialsException e ) {
            // if the pass was wrong, loops again
            JOptionPane.showMessageDialog( null, e.getMessage(), "open error",
                    JOptionPane.ERROR_MESSAGE );
            // writeLog( "info: " + e.getMessage() );
        } catch( Exceptions.ImportException e ) {
            // if the pass was wrong, loops again
            JOptionPane.showMessageDialog( null, e.getMessage(), "import error",
                    JOptionPane.ERROR_MESSAGE );
            // writeLog( "info: " + e.toString() );
        } catch( Exception e ) {
            // otherwise, writes the exception to the log file and quit
            System.out.println( "unplanned exception" );
            e.printStackTrace();
            // writeLog( "severe: " + e.toString() );
            // TODO
            System.exit( 0 );
        }

        modal.dispose();
        return session;

    }// end handleCredentials


    /* *****************************************************************
     *  thread to launch new Session GUI window
     * ****************************************************************/


    private void initConfig() throws Exceptions.ConfigFileNotFoundException {

        try {
            if( debug ) {
                System.out.println( new File( Main.class.getProtectionDomain().getCodeSource()
                        .getLocation().getPath() ) );
            }
            config = ( PassConfigContainer ) new ConfigFileManager().getJsonFromFile( this.getClass()
                    .getClassLoader().getResourceAsStream( CONFIG_PATH ),
                    new PassConfigContainer() );

            System.out.println("default config loaded");

            //updates the paths
            this.config.updatePaths();
            File defaultSessionPath = new File( ( String ) config.getProperty( "session path" ) );
            if( debug ) System.out.println( config );

            //try to load the usersettings
            PassConfigContainer userconfig;
            userconfig = ( PassConfigContainer ) new ConfigFileManager().getJsonFromFile( new File( ( String ) config.getProperty( "userconfig path" ) ), new PassConfigContainer() );
            if( userconfig != null ) this.config.mergeSettings( userconfig );

            //checks that application dir exists or creates it
            File appfolder = new File( ( String ) config.getProperty( "application path" ) );

            if( !appfolder.exists() && !appfolder.isDirectory() && !appfolder.mkdir() ) {
                throw new Exception( "could not find/create application dir" );
            }//end if

            //checks that session dir exists or creates it
            if( !( new File( ( String ) config.getProperty( "session path" ) ).exists() ) ) {
                //try to use the default setting
                if( !defaultSessionPath.exists() && !defaultSessionPath.mkdir()  ) {
                    throw new Exceptions.ConfigFileWrongSyntaxException( "session folder could " +
                            "not " + "be found" );
                }
                config.setProperty( "session path", defaultSessionPath );
            }//end if

        } catch( Exception e ) {
            if( debug ) e.printStackTrace();
            throw new Exceptions.ConfigFileNotFoundException();
        }// end try

    }// end initConfig


    /*private void checkConfig() {

        try {
            // ---------- checks that the application folder exists, or creates
            // it
            File appfolder = new File( this.config.getProperty( "application path" ) );

            if( ( !appfolder.exists() || !appfolder.isDirectory() ) && !appfolder.mkdir() ) {
                throw new Exception( "application path could not be found" );
            }

            // ----------- checks the session path
            File sessionfolder = new File( this.config.getProperty( "session path" ) );

            if( ( !sessionfolder.exists() || !sessionfolder.isDirectory() ) && !sessionfolder
                    .mkdir() ) {
                throw new Exception( "session path could not be found" );
            }

            // ----------- checks the logfile path
            File logfile = new File( config.getProperty( "log filepath" ) );
            // if file doesn't exists, creates it
            if( ( !logfile.exists() || !logfile.isFile() ) && !logfile.createNewFile() ) {
                throw new Exception( "log filepath could not be found" );
            }

            // ------------checks the window width and height
            try {
                Integer.parseInt( config.getProperty( "window width" ) );
                Integer.parseInt( config.getProperty( "window height" ) );

            } catch( Exception e ) {
                throw new Exception( "error parsing 'window height' and 'window width' " +
                        "properties" );
            }

            // ------------checks and trims the column names
            try {
                String colnamesTrimmed = "";
                String[] columnNames = config.getProperty( "column names" ).split( "," );
                for( int i = 0; i < columnNames.length; i++ ) {

                    colnamesTrimmed += ( i == 0 ? "" : "," ) + columnNames[ i ].trim();
                }// end for
                if( debug ) System.out.println( "col names trimmed : " + colnamesTrimmed );
                config.setProperty( "column names", colnamesTrimmed );
            } catch( Exception e ) {
                throw new Exception( "'column names' property could not be found" );
            }

        } catch( Exception e ) {
            if( debug ) e.printStackTrace();
            JOptionPane.showMessageDialog( null, "Error in configuration file : \n       " + e
                    .getMessage() + "\nplease verify your settings or delete your custom
                    configuration file", "configuration error", JOptionPane.ERROR_MESSAGE );
            System.exit( 1 );
        }// end try
    }// end checkConfig */


} // end class
