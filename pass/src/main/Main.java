package main;

import dialogs.OpenSessionDialog;
import gui.PassFrame;
import main.thread.PassLock;
import manager.PassConfigContainer;
import manager.SessionManager;
import manager.SessionManager.Session;
import models.AbstractConfigContainer;
import models.ConfigFileManager;
import models.Exceptions;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * User: lucy Date: 02/03/13
 */
public class Main {

    public static final String APPLICATION_NAME = "easypass";
    private static final String configPath = "test_config";
    private AbstractConfigContainer config;
    private SessionManager sessionManager;
    private int runningWindowsCount;
    private boolean running = true;
    private PassLock lock;
    public static boolean debug = true;


    public static void main( String[] args ) {
        new Main();
    } // end main


    public Main() {
        // get config

        try {
            initConfig();
        } catch( Exceptions.ConfigFileNotFoundException e ) {
            JOptionPane.showMessageDialog( null, "Default settings not found. Exiting...",
                    "configuration error", JOptionPane.ERROR_MESSAGE );
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
                if( debug ) System.out.println( "main thread interrupted" );
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

            // get pass and salt
            String pass = modal.getPass();
            String name = modal.getSession();


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
            //gets defaults settings
            config = ( PassConfigContainer ) new ConfigFileManager().getJsonFromFile( new File(
                    configPath ), new PassConfigContainer() );

            //updates the paths
            this.config.updatePaths();
            if( debug ) System.out.println( config );

            File appfolder = new File( ( String ) config.getProperty( "application path" ) );
            // if the session folder does not exist, creates it
            if( !appfolder.exists() || !appfolder.isDirectory() || appfolder.mkdir() ) {
                throw new Exception( "could not find/create session dir" );
            }

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
