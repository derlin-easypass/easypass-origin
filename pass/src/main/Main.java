package main;

import dialogs.OpenSessionDialog;
import manager.PassConfigManager;
import manager.SessionManager;
import manager.SessionManager.Session;
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
    private PassConfigManager configManager;
    private SessionManager sessionManager;
    private int runningWindowsCount;
    private boolean running = true;
    private Object lock;
    private boolean debug = true;


    public static void main( String[] args ) {
        new Main();
    } // end main


    public Main() {
        // get config
        try {

            this.configManager = initConfigManager();
            assert this.configManager != null;
            checkConfig();
            this.sessionManager = new SessionManager( this.configManager.getMap() );
            this.runningWindowsCount = 0;
            this.lock = new Object();

            while( running ) {
                PassFrame frame = new SessionWindow().launchGUI();
                if( debug ) System.out.printf( "launchGUI returned." );
                    try {
                        synchronized( lock ) {
                            lock.wait();
                        }
                    } catch( InterruptedException e ) {
                        if( debug ) System.out.println( "main thread interrupted" );
                        System.out.println("interrupt");
                    }
                    frame.dispose();
                }
        } catch( Exceptions.ConfigFileNotFoundException e ) {
            if( debug ) {
                System.out.println("config file not found");
                e.printStackTrace();
            }
            // TODO
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
                System.out.println( "\nsessions location : " + configManager.getProperty(
                        "session path" ) );
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
        Session session;

        try {

            modal = new OpenSessionDialog( null, sessionManager.availableSessions() );
            modal.addWindowListener( new WindowAdapter() {

                @Override
                public void windowClosing( WindowEvent e ) {
                    System.exit( 0 );
                }
            } );

        } catch( Exception e ) {
            e.printStackTrace();
            // TODO
            System.exit( 0 );
        }

        while( true ) {

            // asks for pass with a dialog and loads the serialized datas
            // if the pass is wrong, ask again. If the user cancels, quits the
            // application

            modal.setVisible( true );

            if( modal.getStatus() == false ) { // checks if user clicked cancel
                // or closed
                modal.dispose();
                System.exit( 0 );
            }
            // get pass and salt
            String pass = modal.getPass();
            String name = modal.getSession();

            try {

                if( modal.isImported() ) {
                    session = sessionManager.importSession( name, pass );

                } else if( !sessionManager.sessionExists( name ) ) {
                    session = sessionManager.createSession( name, pass );
                    modal.dispose();
                    return session;

                } else {
                    // try to open the session and loads the encrypted data
                    session = sessionManager.openSession( name, pass );
                    modal.dispose();
                    return session;
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
        }// end while

    }// end handleCredentials


    /* *****************************************************************
     *  thread to launch new Session GUI window
     * ****************************************************************/


    private PassConfigManager initConfigManager() throws Exceptions.ConfigFileNotFoundException {

        PassConfigManager confmanager = null;

        try {
            confmanager = new PassConfigManager();
        } catch( Exceptions.ConfigFileNotFoundException e ) {
            JOptionPane.showMessageDialog( null, "Default settings not found. Exiting...",
                    "configuration error", JOptionPane.ERROR_MESSAGE );
            throw e;
        }// end try

        File appfolder = new File( confmanager.getProperty( "application path" ) );

        // if the session folder does not exist, creates it
        if( !appfolder.exists() || !appfolder.isDirectory() ) appfolder.mkdir();

        return confmanager;
    }// end initConfigManager


    private void checkConfig() {

        try {
            // ---------- checks that the application folder exists, or creates
            // it
            File appfolder = new File( this.configManager.getProperty( "application path" ) );

            if( ( !appfolder.exists() || !appfolder.isDirectory() ) && !appfolder.mkdir() ) {
                throw new Exception( "application path could not be found" );
            }

            // ----------- checks the session path
            File sessionfolder = new File( this.configManager.getProperty( "session path" ) );

            if( ( !sessionfolder.exists() || !sessionfolder.isDirectory() ) && !sessionfolder
                    .mkdir() ) {
                throw new Exception( "session path could not be found" );
            }

            // ----------- checks the logfile path
            File logfile = new File( configManager.getProperty( "log filepath" ) );
            // if file doesn't exists, creates it
            if( ( !logfile.exists() || !logfile.isFile() ) && !logfile.createNewFile() ) {
                throw new Exception( "log filepath could not be found" );
            }

            // ------------checks the window width and height
            try {
                Integer.parseInt( configManager.getProperty( "window width" ) );
                Integer.parseInt( configManager.getProperty( "window height" ) );

            } catch( Exception e ) {
                throw new Exception( "error parsing 'window height' and 'window width' " +
                        "properties" );
            }

            // ------------checks and trims the column names
            try {
                String colnamesTrimmed = "";
                String[] columnNames = configManager.getProperty( "column names" ).split( "," );
                for( int i = 0; i < columnNames.length; i++ ) {

                    colnamesTrimmed += ( i == 0 ? "" : "," ) + columnNames[ i ].trim();
                }// end for
                if( debug ) System.out.println( "col names trimmed : " + colnamesTrimmed );
                configManager.setProperty( "column names", colnamesTrimmed );
            } catch( Exception e ) {
                throw new Exception( "'column names' property could not be found" );
            }

        } catch( Exception e ) {
            if( debug ) e.printStackTrace();
            JOptionPane.showMessageDialog( null, "Error in configuration file : \n       " + e
                    .getMessage() + "\nplease verify your settings or delete your custom configuration file", "configuration error", JOptionPane.ERROR_MESSAGE );
            System.exit( 1 );
        }// end try
    }// end checkConfig


} // end class
