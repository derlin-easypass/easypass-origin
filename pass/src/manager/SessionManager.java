package manager;

import gui.PassFileFilter;
import models.AbstractConfigContainer;
import models.Exceptions;
import models.Exceptions.ImportException;
import models.Exceptions.WrongCredentialsException;
import passinterface.AbstractSessionChecker;
import table.PassTableModel;

import javax.swing.*;
import java.io.*;

/**
 * This class manages the manipulation of session files. It returns the name of
 * the existing sessions, opens and close them, serializes the data when needed.
 * <p/>
 * Note that in order to use it, you must specify a valid folder path containing
 * valid easypass sessions files.
 * <p/>
 * The data are encrypted with the aes-128 cbc algorithm and stored in a json
 * format. It is possible to decrypt them by simply using openssl, with the
 * following options :
 * <p/>
 * openssl enc -d -aes-128-cbc -a -salt -pass pass:yourpass -in <encrypted file>
 * -out <destination file>
 * <p/>
 * <p/>
 * Notes : - no utility to delete a session yet - the password is stored in RAM
 * until the session is closed (unavoidable??)
 *
 * @author Lucy Linder
 * @date Dec 21, 2012
 */
public class SessionManager implements AbstractSessionChecker {

    private static int CURRENT_VERSION = 1; // TODO
    public static String CRYPTO_ALGORITHM = "aes-128-cbc";
    public static String DATA_EXTENSION = ".data_ser";

    private AbstractConfigContainer config;
    private String directoryPath;


    public static void main( String[] args ) {

        // ArrayList<Object[]> data = new ArrayList<Object[]>();
        // Object[] o1 = { "Google", "Smith", "Snowboarding", "dlskafj", "" };
        // Object[] o2 = { "John", "Doe", "Rowing", "pass", "" };
        // Object[] o3 = { "paypal", "winthoutid@hotmail.fr", "", "pass", "" };
        //
        // data.add( o1 );
        // data.add( o2 );
        // data.add( o3 );

    }


    /**
     * ******************************************************* constructor
     * ********************************************************
     */

    public SessionManager( AbstractConfigContainer config ) {

        this.config = config;
        File file = new File( ( String ) config.getProperty( "session path" ) );
        if( !file.exists() || !file.isDirectory() ) {
            throw new Exceptions.NotInitializedException( file.getPath() + "is not a valid " +
                    "directory" );
        }

        this.directoryPath = ( String ) config.getProperty( "session path" );

    }// end constructor


    /**************************************************************
     * get available sessions, check if a sessions exists
     ************************************************************/


    /**
     * returns an array of strings with all the existing sessions
     *
     * @return   the existing sessions
     * @throws java.io.FileNotFoundException
     */
    public String[] availableSessions() throws FileNotFoundException {

        File folder = new File( this.directoryPath );
        if( !folder.exists() || !folder.isDirectory() ) {
            throw new FileNotFoundException( this.directoryPath + " is not a valid directory" );
        }

        File[] listOfFiles = folder.listFiles( new PassFileFilter( DATA_EXTENSION ) );
        String[] sessions = new String[ listOfFiles.length ];

        int counter = 0;
        for( File f : listOfFiles ) {
            String name = f.getName();
            sessions[ counter ] = name.substring( 0, name.lastIndexOf( '.' ) );
            System.out.println( sessions[ counter ] );
            counter++;
        }

        return sessions;
    }// end getAvailableSessions


    /**
     * checks if a file corresponding to the given session name exists.
     *
     * @param pathToFolder
     * @param name
     * @return
     */
    public boolean sessionExists( String pathToFolder, String name ) {

        File folder = new File( pathToFolder );
        // only if folder exists and is actually a directory
        return folder.exists() && folder.isDirectory() && folder.listFiles( new PassFileFilter(
                DATA_EXTENSION ) ).length > 0;

    }// end sessionExists


    /**
     * return true if a session is currently opened
     *
     * @param name
     * @return
     */
    public boolean sessionExists( String name ) {
        return this.sessionExists( directoryPath, name );
    }// end sessionExists


    /**
     * ***********************************************************
     * opens, creates or imports sessions
     * **********************************************************
     */

    public boolean areCredentialsValid( String sessionName, String password ) {
        try {
            new Session( this.getSessionFilePath( sessionName ), password ).loadModel();
            return true;
        } catch( WrongCredentialsException e ) {
            return false;
        }
    }//end areCredentialsValid


    /**
     * creates a new session. If the directoryPath (where the sessions files are
     * stored) for this object is not set, an exception of type
     * FileNotFoundException is fired.
     *
     * @param sessionName the session name
     * @param password    the password
     */
    public Session createSession( String sessionName, String password ) {
        Session session = new Session( this.getSessionFilePath( sessionName ), password );
        session.createModel();
        return session;
    }// end createSession


    /**
     * open an already existing session and returns the datas of the jtable
     *
     * @param name     the session name
     * @param password the password
     * @return
     * @throws models.Exceptions.WrongCredentialsException
     *
     * @throws java.io.IOException
     */
    public Session openSession( String name, String password ) throws Exceptions
            .WrongCredentialsException, IOException {

        try {

            Session s = new Session( this.getSessionFilePath( name ), password );
            s.loadModel();
            return s;

        } catch( Exceptions.WrongCredentialsException e ) {
            throw new Exceptions.WrongCredentialsException( "session \"" + name + "\" : wrong " +
                    "credentials." );
        }

    }// end openSession


    /**
     * imports a session by copying the given file to the sessions folder
     *
     * @param sessionPath the path to the file to import
     * @param password    the password
     * @return a session object
     * @throws models.Exceptions.WrongCredentialsException
     *
     * @throws models.Exceptions.ImportException
     *
     */
    public Session importSession( String sessionPath, String password ) throws Exceptions
            .WrongCredentialsException, ImportException {

        String name;
        File fileIn = new File( sessionPath );

        //checks that the file to import exists and has the correct extension
        if( fileIn.getName().isEmpty() || !fileIn.getName().endsWith( DATA_EXTENSION ) ) {
            throw new ImportException( "Session " + sessionPath + " not found. The file does not " +
                    "exist or does not have the correct extension (" + DATA_EXTENSION + ")." );
        }

        name = fileIn.getName().substring( 0, fileIn.getName().lastIndexOf( '.' ) );

        // if the session already exist, asks if the user wants to override it
        if( this.sessionExists( name ) ) {
            if( JOptionPane.showConfirmDialog( null, "A session by this name already exists. Do "
                    + "you want to replace it ?", "import session", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION ) {
                throw new ImportException( "Importation canceled." );
            }
        }

        //copies the file to the session directory and creates a new session object to return
        try {

            copyFile( sessionPath, this.getSessionFilePath( name ) );
            Session session = new Session( sessionPath, password );
            session.loadModel();
            return session;

        } catch( Exception e ) {
            //deletes the copied file
            new File( this.getSessionFilePath( name ) ).delete();
            throw new Exceptions.WrongCredentialsException( name + " : file couldn't be copied " );
        }

    }// end importSession


    /**
     * *********************************************************** getters
     * **********************************************************
     */

    /**
     * returns the complete path to the current session file
     *
     * @return the path to the given session  file
     */
    public String getSessionFilePath( String name ) {
        return this.directoryPath + File.separator + name + DATA_EXTENSION;
    }


    /**************************************************************
     * utilities
     ************************************************************/

    /**
     * copies a file. If a file with the same name already exists, it will be replaced !
     *
     * @param srcFilepath  the file to copy
     * @param destFilepath the target of the copy
     * @throws java.io.IOException
     */
    public static void copyFile( String srcFilepath, String destFilepath ) throws IOException {

        FileInputStream fin = new FileInputStream( srcFilepath );
        FileOutputStream fos = new FileOutputStream( destFilepath );

        byte[] buf = new byte[ 1024 ];
        int len;
        while( ( len = fin.read( buf ) ) > 0 ) {
            fos.write( buf, 0, len );
        }

        fin.close();
        fos.close();
        System.out.println( "File copied." );
    }


    /********************************************************************
     *  the session class
     ********************************************************************/

    /**
     * This class manages the manipulation of name files. It returns the name of
     * the existing sessions, opens and close them, serializes the data when
     * needed.
     * <p/>
     * Note that in order to use it, you must specify a valid folder path
     * containing valid easypass sessions files.
     * <p/>
     * The data are encrypted with the aes-128 cbc algorithm and stored in a
     * json format. It is possible to decrypt them by simply using openssl, with
     * the following options :
     * <p/>
     * openssl enc -d -aes-128-cbc -a -salt -pass pass:yourpass -in <encrypted
     * file> -out <destination file>
     * <p/>
     * <p/>
     * Notes : - no utility to delete a name yet - the password is stored in RAM
     * until the name is closed (unavoidable??)
     *
     * @author Lucy Linder
     * @date Dec 21, 2012
     */
    public class Session {

        private PassJsonManager jsonManager;
        private String path;
        private String name;
        private PassTableModel model;
        private String password;
        private boolean isActive = false;


        /* *****************************************************************
         * constructor
         * ****************************************************************/


        /**
         * creates a session object, but does not initialize the model !
         *
         * @param path     the path to the session file
         * @param password the password
         */
        private Session( String path, String password ) {
            this.path = path;
            this.name = path.substring( path.lastIndexOf( File.separator ) + 1,
                    path.indexOf( DATA_EXTENSION ) );
            this.password = password;
            this.jsonManager = new PassJsonManager();

        }// end constructor


        /* *****************************************************************
         * session management
         * ****************************************************************/


        /**
         * returns true if a name is currently opened
         *
         * @return
         */
        public boolean isActive() {
            return isActive;
        }//end isActive


        /**
         * decrypts the data stored into the session file and loads them into the {@link
         * PassTableModel} attached to the session object
         * object.
         *
         * @throws WrongCredentialsException
         */
        private void loadModel() throws WrongCredentialsException {
            try {
                this.model = new PassTableModel( ( String[] ) config.getProperty( "column names"
                ), this.jsonManager.deserialize( CRYPTO_ALGORITHM, path, password ) );
                this.isActive = true;
            } catch( Exception e ) {
                throw new Exceptions.WrongCredentialsException( "session \"" + path + "\" : wrong" +
                        " credentials." );

            }// end try

        }// end loadModel


        /**
         * creates an empty {@link PassTableModel} for the session object
         */
        private void createModel() {
            this.model = new PassTableModel( ( String[] ) config.getProperty( "column names" ) );
            this.isActive = true;
        }// end createModel


        /**
         * saves the data of the tableModel into the session file, using AES
         *
         * @return true if the saving went well, false if a problem occurred
         */
        public boolean save() {

            if( !this.isActive() ) {
                throw new Exceptions.NotInitializedException( "no name opened" );
            }//end if

            try {

                this.jsonManager.serialize( this.model.getData(), CRYPTO_ALGORITHM, this.path,
                        this.password );
                return true;

            } catch( IOException e ) {
                e.printStackTrace();
                return false;
            } //end try
        }// end save


        /**
         * closes the current nsession object
         */
        public void close() {
            this.password = null;
            this.name = null;
            this.jsonManager = null;
            this.isActive = false;
        }//end close


        /**
         * deletes the current session object, i.e. the file used to serialize its data
         */
        public void delete() {
            if( !this.isActive() ) {
                return;
            }

            File file = new File( this.path );
            file.delete();
            this.close();
        }//end delete


        /**
         * changes the session name and the password of the currently opened
         * session
         *
         * @param newName the new name
         * @param newPass the new password
         * @throws models.Exceptions.RefactorException
         *
         */
        public void refactor( String newName, String newPass ) throws Exceptions.RefactorException {

            String oldSessionName = this.name;
            String oldPath = this.path;
            String oldPass = this.password;

            if( !this.isActive ) {
                return;
            }//end if

            //updates the session attributes
            this.name = newName;
            this.password = newPass;
            this.path = getSessionFilePath( newName );

            try {
                //deserializes the data using the old credentials and serializes them with the
                // new ones
                this.jsonManager.serialize( this.jsonManager.deserialize( CRYPTO_ALGORITHM,
                        oldPath, oldPass ), CRYPTO_ALGORITHM, this.path, this.password );

                //if the filename has changed, deletes the old file
                if( !this.path.equals( oldPath ) ) new File( oldPath ).delete();

            } catch( Exception e ) {
                //in case an error occurs, sets the session attributes back to the pre-refactor
                // state
                this.name = oldSessionName;
                this.password = oldPass;
                this.path = oldPath;

                throw new Exceptions.RefactorException( this.name + " : file couldn't be " +
                        "refactored " );
            } //end try

        }//end refactor


        /**
         * writes the content of the list as proper json in the specified file
         *
         * @param file the file in which to serialize the data
         * @throws java.io.IOException
         */
        public void writeAsJson( File file ) throws IOException {
            this.jsonManager.writeToFile( this.model.getData(), file );
        }


        /**
         * returns the {@link PassTableModel} attached to this session object
         *
         * @return the tablemodel
         */
        public PassTableModel getModel() {
            return this.model;
        }//end getModel


        /**
         * returns the given configuration property
         *
         * @param key the name of the property to return
         * @return the property
         */
        public Object getConfigProperty( String key ) {
            return config.getProperty( key );
        }//end getConfigProperty


        /**
         * gets the name of the current session
         *
         * @return
         */
        public String getName() {
            return name;
        }//end getName

    }// end class
}// end class