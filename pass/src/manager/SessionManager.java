package manager;

import models.Exceptions;
import models.Exceptions.ImportException;
import models.Exceptions.RefactorException;
import models.Exceptions.SessionFileNotFoundException;
import models.Exceptions.WrongCredentialsException;

import javax.swing.*;

import table.PassTableModel;

import java.io.*;
import java.util.List;
import java.util.Map;

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
public class SessionManager {
    
    private static int CURRENT_VERSION = 1; // TODO
    public static String CRYPTO_ALGORITHM = "aes-128-cbc";
    public static String DATA_EXTENSION = ".data_ser";
    
    private Map<String, String> config;
    private String directoryPath;
    private int nbrOfActiveSessions = 0;
    
    
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
    
    public SessionManager(Map<String, String> config) {
        
        this.config = config;
        File file = new File( config.get( "session path" ) );
        if( !file.exists() || !file.isDirectory() ){
            throw new Exceptions.NotInitializedException( file.getPath()
                    + "is not a valid directory" );
        }
        
        this.directoryPath = config.get( "session path" );
        
    }// end constructor
    
    
    /**************************************************************
     * get available sessions, check if a sessions exists
     ************************************************************/
    
    /**
     * returns an array of strings with all the existing sessions
     * 
     * @return
     * @throws java.io.FileNotFoundException
     */
    public String[] availableSessions() throws FileNotFoundException {
        return this.availableSessions( ".*\\" + DATA_EXTENSION + "$" );
    }// end available sessions
    
    
    /**
     * returns an array of strings with all the existing sessions
     * 
     * @param pattern
     *            the regex to recognize the sessions
     * @return
     * @throws java.io.FileNotFoundException
     */
    public String[] availableSessions( String pattern )
            throws FileNotFoundException {
        
        File folder = new File( this.directoryPath );
        if( !folder.exists() || !folder.isDirectory() ){
            throw new FileNotFoundException( this.directoryPath
                    + " is not a valid directory" );
        }
        
        File[] listOfFiles = folder.listFiles( new Filter( pattern ) );
        String[] sessions = new String[listOfFiles.length];
        
        int counter = 0;
        for( File f : listOfFiles ){
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
        if( folder.exists() && folder.isDirectory() ){
            return ( folder.listFiles( new Filter( name + "\\.*"
                    + DATA_EXTENSION + "$" ) ).length > 0 );
        }
        return false;
        
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
    
    
    /**************************************************************
     * opens, creates or imports sessions
     ************************************************************/
    
    /**
     * creates a new session. If the directoryPath (where the sessions files are
     * stored) for this object is not set, an exception of type
     * FileNotFoundException is fired.
     * 
     * @param sessionName
     *            the session name
     * @param password
     *            the password
     */
    public Session createSession( String sessionName, String password ) {
        Session session = new Session( this.getSessionFilePath( sessionName ), password );
        session.createModel();
        return session;
    }// end createSession
    
    
    /**
     * open an already existing session and returns the datas of the jtable
     * 
     * @param name
     *            the session name
     * @param password
     *            the password
     * @return
     * @throws models.Exceptions.WrongCredentialsException
     * 
     * @throws java.io.IOException
     */
    public Session openSession( String name, String password )
            throws Exceptions.WrongCredentialsException, IOException {
        
        try{
            
            Session s = new Session( this.getSessionFilePath( name ), password );
            s.loadModel();
            return s;
            
        }catch( Exceptions.WrongCredentialsException e ){
            throw new Exceptions.WrongCredentialsException( "session \"" + name
                    + "\" : wrong credentials." );
        }
        
    }// end openSession
    
    
    /**
     * imports a session by copying the ginven file to the sessions folder
     * 
     * @param sessionPath
     * @param password
     * @return
     * @throws models.Exceptions.WrongCredentialsException
     * 
     * @throws java.io.IOException
     * @throws models.Exceptions.SessionFileNotFoundException
     * 
     * @throws models.Exceptions.ImportException
     * 
     */
    public Session importSession( String sessionPath, String password )
            throws Exceptions.WrongCredentialsException, IOException,
            SessionFileNotFoundException, ImportException {
        
        String name;
        
        File fileIn = new File( sessionPath );
        
        if( fileIn.getName().isEmpty()
                || !fileIn.getName().endsWith( DATA_EXTENSION ) ){
            throw new ImportException(
                    "Session "
                            + sessionPath
                            + " not found. The file does not exist or does not end with "
                            + DATA_EXTENSION + "." );
        }
        
        name = fileIn.getName().substring( 0,
                fileIn.getName().lastIndexOf( '.' ) );
        
        // if the session already exist, asks if the user wants to override it
        if( this.sessionExists( name ) ){
            if( JOptionPane
                    .showConfirmDialog(
                            null,
                            "A session by this name already exists. Do you want to replace it ?",
                            "import session", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION ){
                throw new ImportException( "Importation canceled." );
            }
        }
        
        try{
            
            copyFile( sessionPath, this.getSessionFilePath( name ) );
            return new Session( sessionPath, password );
            
        }catch( Exception e ){
            new File( this.getSessionFilePath( name ) ).delete();
            throw new Exceptions.WrongCredentialsException( name
                    + " : file couldn't be copied " );
        }
        
    }// end importSession
    
    
    /**
     * *********************************************************** getters
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
    
    
    /**
     * returns the complete path to the current session file
     * 
     * @return
     */
    public String getSessionFilePath( String name ) {
        return this.directoryPath + File.separator + name + DATA_EXTENSION;
    }
    
    
    /**************************************************************
     * utilities
     ************************************************************/
    
    /**
     * copies a file.
     * 
     * @param srcFilepath
     * @param destFilepath
     * @throws java.io.IOException
     */
    public static void copyFile( String srcFilepath, String destFilepath )
            throws IOException {
        
        FileInputStream fin = new FileInputStream( srcFilepath );
        FileOutputStream fos = new FileOutputStream( destFilepath );
        
        byte[] buf = new byte[1024];
        int len;
        while( ( len = fin.read( buf ) ) > 0 ){
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
        
        
        public Filter(String str) {
            pattern = str;
        }
        
        
        public String getDescription() {
            return "Easypass session file (*." + SessionManager.DATA_EXTENSION
                    + ")";
        }
        
        
        public boolean accept( File dir, String name ) {
            return name.toLowerCase().matches( pattern );
        }
    }// end class filter
    
    /********************************************************************
     * 
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
        
        
        /**
         * ******************************************************* constructor
         * ********************************************************
         */
        
        public Session(String path, String password) {
            this.path = path;
            this.name = path.substring( path.lastIndexOf( File.separator ) + 1,
                    path.indexOf( SessionManager_old.DATA_EXTENSION ) );
            this.password = password;
            this.jsonManager = new PassJsonManager();
            
        }// end constructor
        
        
        /**************************************************************
         * manages sessions
         ************************************************************/
        /**
         * returns true if a name is currently opened
         * 
         * @return
         */
        public boolean isActive() {
            return isActive;
        }
        
        
        public void loadModel() throws WrongCredentialsException {
            try{
                this.model = new PassTableModel( config.get( "column names" )
                        .split( "," ), this.jsonManager.deserialize(
                        SessionManager_old.CRYPTO_ALGORITHM, path, password ) );
                this.isActive = true;
            }catch( Exception e ){
                throw new Exceptions.WrongCredentialsException( "session \""
                        + path + "\" : wrong credentials." );
                
            }// end try
            
        }// end loadModel
        
        
        public void createModel() {
            this.model = new PassTableModel( config.get( "column names" )
                    .split( "," ) );
            this.isActive = true;
        }// end createModel
        
        
        public boolean save() {
            
            if( !this.isActive() ){
                throw new Exceptions.NotInitializedException( "no name opened" );
            }
            
            try{
                
                this.jsonManager.serialize( this.model.getData(),
                        SessionManager_old.CRYPTO_ALGORITHM, this.path,
                        this.password );
                return true;
                
            }catch( IOException e ){
                e.printStackTrace();
                return false;
            }
        }// end save
        
        
        /**
         * closes the current name
         */
        public void closeSession() {
            this.password = null;
            this.name = null;
            this.jsonManager = null;
            this.isActive = false;
        }
        
        
        public void delete() {
            if( !this.isActive() ){
                return;
            }
            
            File file = new File( this.path );
            file.delete();
            this.closeSession();
        }
        
        
        /**
         * changes the session name and the password of the currently opened
         * session
         * 
         * @param newName
         * @param newPass
         * @throws models.Exceptions.RefactorException
         * 
         */
        public void refactor( String newName, String newPass )
                throws Exceptions.RefactorException {
            
            String oldSessionName = this.name;
            String oldPath = this.path;
            String oldPass = this.password;
            
            if( !this.isActive ){
                return;
            }
            
            this.name = newName;
            this.password = newPass;
            this.path = getSessionFilePath( newName );
            
            try{
                this.jsonManager.serialize( this.jsonManager.deserialize(
                        SessionManager_old.CRYPTO_ALGORITHM, oldPath, oldPass ),
                        SessionManager_old.CRYPTO_ALGORITHM, this.path,
                        this.password );

                new File( oldPath ).delete();
                
            }catch( Exception e ){
                
                this.name = oldSessionName;
                this.password = oldPass;
                this.path = oldPath;
                
                throw new Exceptions.RefactorException( this.name
                        + " : file couldn't be refactored " );
            }
            
        }
        
        
        /**
         * writes the content of the list as proper json in the specified file
         * 
         * @param data
         * @param file
         * @throws java.io.IOException
         */
        public void writeAsJson( File file )
                throws IOException {
            this.jsonManager.writeToFile( this.model.getData(), file );
        }
        
        
        /**
         * returns the path to the sessions folder
         * 
         * @return
         */
        public String getPath() {
            return path;
        }
        
        
        public PassTableModel getModel() {
            return this.model;
        }
        
        
        public String getConfigProperty( String key ) {
            return config.get( key );
        }
        
        
        public String getSessionName() {
            return name;
        }
        
    }// end class
}// end class