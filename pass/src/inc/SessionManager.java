package inc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import models.Exceptions;
import models.Exceptions.CryptoException;
import models.Exceptions.WrongCredentialsException;

/**
 * This class manages the manipulation of session files. It returns the name of
 * the existing sessions, opens and close them, serializes the data when needed.
 * 
 * Note that in order to use it, you must specify a valid folder path containing
 * valid easypass sessions files.
 * 
 * The data are encrypted with the aes-128 cbc algorithm and stored in a json
 * format. It is possible to decrypt them by simply using openssl, with the
 * following options :
 * 
 * openssl enc -d -aes-128-cbc -a -salt -pass pass:yourpass -in <encrypted
 * file> -out <destination file>
 * 
 * 
 * Notes : - no utility to delete a session yet - the password is stored in RAM
 * until the session is closed (unavoidable??)
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 * 
 */
public class SessionManager {
    
    private static int currentVersion = 1;
    private static String cryptoAlgorithm = "aes-128-cbc";
    
    private String dataExtension = ".data_ser";
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
    
    
    /**********************************************************
     * constructor
     **********************************************************/
    
    public SessionManager(String directoryPath) {
        File file = new File( directoryPath );
        
        if( file.exists() && file.isDirectory() ){
            this.directoryPath = directoryPath;
        }else{
            throw new Exceptions.NotInitializedException( directoryPath
                    + "is not a valid directory" );
        }
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
        return this.availableSessions( ".*\\" + dataExtension + "$" );
    }// end available sessions
    
    
    /**
     * returns an array of strings with all the existing sessions
     * 
     * @param pattern
     *            the regex to recognize the sessions
     * @return
     * @throws FileNotFoundException
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
            return ( folder.listFiles( new Filter( name + "\\" + dataExtension
                    + "$" ) ).length > 0 ? true : false );
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
     * opens or creates sessions
     ************************************************************/
    
    /**
     * creates a new session. If the directoryPath (where the sessions files are
     * stored) for this object is not set, an exception of type
     * FileNotFoundException is fired.
     * 
     * @param sessionName
     * @param pass
     * @param salt
     * @throws FileNotFoundException
     * @throws CryptoException
     */
    public void createSession( String sessionName, String password ) {
        
        this.session = sessionName;
        this.password = password;
        
    }// end createSession
    
    
    /**
     * open an already existing session and returns the datas of the jtable
     * 
     * @param pass
     * @param salt
     * @return
     * @throws CryptoErrorException
     * @throws WrongCredentialsException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> openSession( String session, String password )
            throws Exceptions.WrongCredentialsException, IOException {
        
        try{
            
            this.session = session;
            this.password = password;
            return new JsonManager().deserialize( cryptoAlgorithm,
                    this.getDataPath(), this.password );
            
        }catch( Exceptions.WrongCredentialsException e ){
            // if the pass was wrong, loops again
            throw new Exceptions.WrongCredentialsException( session
                    + " : wrong salt and/or password" );
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
        return ( this.session == null ? false : true );
    }
    
    
    public boolean save( List<Object[]> data ) {
        
        if( !this.isOpened() ){
            throw new Exceptions.NotInitializedException( "no session opened" );
        }
        
        try{
            
            new JsonManager().serialize( data, cryptoAlgorithm,
                    this.getDataPath(), this.password );
            return true;
            
        }catch( IOException e ){
            e.printStackTrace();
            return false;
        }
    }// end save
    
    
    /**
     * close the current session
     */
    public void close() {
        this.password = null;
        this.session = null;
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
        new JsonManager().writeToFile( data, file );
    }
    
    
    /**************************************************************
     * utilities
     ************************************************************/
    
    public String getDataPath() {
        return this.directoryPath + File.separator + this.session
                + this.dataExtension;
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
        
        
        public boolean accept( File dir, String name ) {
            return name.toLowerCase().matches( pattern );
        }
    }// end class filter
    
}// end class