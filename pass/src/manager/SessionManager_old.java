package manager;

import models.Exceptions;
import models.Exceptions.ImportException;
import models.Exceptions.RefactorException;
import models.Exceptions.SessionFileNotFoundException;
import passinterface.AbstractSessionChecker;

import javax.swing.*;
import java.io.*;
import java.util.List;

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
public class SessionManager_old implements AbstractSessionChecker {

    private static int CURRENT_VERSION = 1; // TODO
    public static String CRYPTO_ALGORITHM = "aes-128-cbc";
    public static String DATA_EXTENSION = ".data_ser";

    private PassJsonManager jsonManager;
    private String directoryPath;
    private String session;
    private String password;


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
     * *******************************************************
     * constructor
     * ********************************************************
     */

    public SessionManager_old( String directoryPath ) {
        File file = new File( directoryPath );

        if( file.exists() && file.isDirectory() ) {
            this.directoryPath = directoryPath;
        } else {
            throw new Exceptions.NotInitializedException( directoryPath
                    + "is not a valid directory" );
        }

        this.jsonManager = new PassJsonManager();
    }// end constructor


    /**************************************************************
     * get available sessions, check if a sessions exists
     ************************************************************/

    /**
     * returns an array of strings with all the existing sessions
     *
     * @return
     * @throws FileNotFoundException
     */
    public String[] availableSessions() throws FileNotFoundException {
        return this.availableSessions( ".*\\" + DATA_EXTENSION + "$" );
    }// end available sessions


    /**
     * returns an array of strings with all the existing sessions
     *
     * @param pattern the regex to recognize the sessions
     * @return
     * @throws FileNotFoundException
     */
    public String[] availableSessions( String pattern )
            throws FileNotFoundException {

        File folder = new File( this.directoryPath );
        if( !folder.exists() || !folder.isDirectory() ) {
            throw new FileNotFoundException( this.directoryPath
                    + " is not a valid directory" );
        }

        File[] listOfFiles = folder.listFiles( new Filter( pattern ) );
        String[] sessions = new String[listOfFiles.length];

        int counter = 0;
        for( File f : listOfFiles ) {
            String name = f.getName();
            sessions[counter] = name.substring( 0, name.lastIndexOf( '.' ) );
            System.out.println( sessions[counter] );
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
        if( folder.exists() && folder.isDirectory() ) {
            return (folder.listFiles( new Filter( name + "\\.*"
                    + DATA_EXTENSION + "$" ) ).length > 0 );
        }

        return false;

    }// end sessionExists


    @Override
    public boolean areCredentialsValid( String sessionName, String password ) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * return true if a session is currently opened
     *
     * @param name
     * @return
     */
    public boolean sessionExists( String name ) {
        return this.sessionExists( directoryPath, name );
    }// end sessionExists


    /**************************************************************
     * opens, creates or imports sessions
     ************************************************************/

    /**
     * creates a new session. If the directoryPath (where the sessions files are
     * stored) for this object is not set, an exception of type
     * FileNotFoundException is fired.
     *
     * @param sessionName the session name
     * @param password    the password
     */
    public void createSession( String sessionName, String password ) {

        this.session = sessionName;
        this.password = password;

    }// end createSession


    /**
     * open an already existing session and returns the datas of the jtable
     *
     * @param session  the session name
     * @param password the password
     * @return
     * @throws Exceptions.WrongCredentialsException
     *
     * @throws IOException
     */
    public List<Object[]> openSession( String session, String password )
            throws Exceptions.WrongCredentialsException, IOException {

        try {

            this.session = session;
            this.password = password;
            return this.jsonManager.deserialize( CRYPTO_ALGORITHM,
                    this.getDataPath(), this.password );

        } catch( Exceptions.WrongCredentialsException e ) {
            throw new Exceptions.WrongCredentialsException( "session \""
                    + session + "\" : wrong credentials." );
        }

    }// end openSession


    /**
     * imports a session by copying the ginven file to the sessions folder
     *
     * @param sessionPath
     * @param password
     * @return
     * @throws Exceptions.WrongCredentialsException
     *
     * @throws IOException
     * @throws SessionFileNotFoundException
     * @throws ImportException
     */
    public List<Object[]> importSession( String sessionPath, String password )
            throws Exceptions.WrongCredentialsException, IOException,
            SessionFileNotFoundException, ImportException {

        File fileIn = new File( sessionPath );

        if( fileIn.getName() == ""
                || !fileIn.getName().endsWith( DATA_EXTENSION ) ) {
            throw new Exceptions.ImportException(
                    "Session "
                            + sessionPath
                            + " not found. The file does not exist or does not end with "
                            + DATA_EXTENSION + "." );
        }

        this.session = fileIn.getName().substring( 0,
                fileIn.getName().lastIndexOf( '.' ) );

        //if the session already exist, asks if the user wants to override it
        if( this.sessionExists( this.session ) ) {
            if( JOptionPane
                    .showConfirmDialog(
                            null,
                            "A session by this name already exists. Do you want to replace it ?",
                            "import session",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION ) {
                throw new ImportException( "Importation canceled." );
            }
        }

        try {

            copyFile( sessionPath, this.getDataPath() );
            return this.openSession( this.session, password );

        } catch( Exception e ) {
            new File( this.getDataPath() ).delete();
            this.closeSession();
            // if the pass was wrong, loops again
            throw new Exceptions.WrongCredentialsException( session
                    + " : file couldn't be copied " );
        }

    }// end openSession


    /**************************************************************
     * manages sessions
     ************************************************************/
    /**
     * returns true if a session is currently opened
     *
     * @return
     */
    public boolean isOpened() {
        return (this.session == null ? false : true);
    }


    public boolean save( List<Object[]> data ) {

        if( !this.isOpened() ) {
            throw new Exceptions.NotInitializedException( "no session opened" );
        }

        try {

            this.jsonManager.serialize( data, CRYPTO_ALGORITHM,
                    this.getDataPath(), this.password );
            return true;

        } catch( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }// end save


    /**
     * closes the current session
     */
    public void closeSession() {
        this.password = null;
        this.session = null;
    }


    public void deleteSession() {
        if( !this.isOpened() ) {
            return;
        }

        File file = new File( this.getDataPath() );
        file.delete();
        this.closeSession();
    }


    /**
     * writes the content of the list as proper json in the specified file
     *
     * @param data
     * @param file
     * @throws IOException
     */
    public void writeAsJson( List<Object[]> data, File file )
            throws IOException {
        new PassJsonManager().writeToFile( data, file );
    }


    /**
     * changes the session name and the password of the currently opened session
     *
     * @param newName
     * @param newPass
     * @throws RefactorException
     */
    public void refactorSession( String newName, String newPass )
            throws RefactorException {

        String oldSessionName = this.session;
        String oldPass = this.password;

        if( this.sessionExists( newName ) ) {
            throw new Exceptions.RefactorException( "duplicated session name" );

        } else if( this.isOpened() == false ) {
            return;
        }

        String srcFile = this.getDataPath();
        this.session = newName;
        this.password = newPass;

        try {

            this.save( this.jsonManager.deserialize( CRYPTO_ALGORITHM,
                    srcFile, oldPass ) );
            new File( srcFile ).delete();

        } catch( Exception e ) {

            this.session = oldSessionName;
            this.password = oldPass;
            throw new Exceptions.RefactorException( session
                    + " : file couldn't be refactored " );
        }

    }


    /**
     * ***********************************************************
     * getters
     * **********************************************************
     */


    /**
     * returns the path to the sessions folder
     *
     * @return
     */
    public String getDirectoryPath() {
        return directoryPath;
    }


    public String getSessionName() {
        return session;
    }


    /**
     * returns the complete path to the current session file
     *
     * @return
     */
    public String getDataPath() {
        return this.directoryPath + File.separator + this.session
                + DATA_EXTENSION;
    }


    /**************************************************************
     * utilities
     *
     * @throws IOException
     ************************************************************/

    /**
     * copies a file.
     *
     * @param srcFilepath
     * @param destFilepath
     * @throws IOException
     */
    public static void copyFile( String srcFilepath, String destFilepath )
            throws IOException {

        FileInputStream fin = new FileInputStream( srcFilepath );
        FileOutputStream fos = new FileOutputStream( destFilepath );

        byte[] buf = new byte[1024];
        int len;
        while( (len = fin.read( buf )) > 0 ) {
            fos.write( buf, 0, len );
        }

        fin.close();
        fos.close();
        System.out.println( "File copied." );
    }


    /**
     * private class used to get files in a folder that match a pattern
     *
     * @author lucy
     */
    public static class Filter implements FilenameFilter {

        protected String pattern;


        public Filter( String str ) {
            pattern = str;
        }


        public String getDescription() {
            return "Easypass session file (*." + SessionManager_old.DATA_EXTENSION
                    + ")";
        }


        public boolean accept( File dir, String name ) {
            return name.toLowerCase().matches( pattern );
        }
    }// end class filter

}// end class