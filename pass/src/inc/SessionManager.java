package inc;

import inc.Functionalities.Filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;

import models.Crypto;
import models.Exceptions;
import models.Exceptions.*;

public class SessionManager {
    
    private static int currentVersion = 1;
    private static String keyFactAlgo = "PBKDF2WithHmacSHA1";
    private static String cipherTransfo = "AES/CBC/PKCS5Padding";
    private static String encryptionType = "AES";
    private static int keyIterationCount = 65536;
    private static int keyLength = 128;
    
    private String ivExtension = ".iv_ser";
    private String dataExtension = ".data_ser";
    private String newLine = System.getProperty( "line.separator" );
    private String directoryPath;
    private String session;
    
    private Crypto cipher = null;
    
    
    public static void main( String[] args ) throws IOException,
            VersionNumberNotFoundException, CryptoException,
            IvNotFoundException, WrongCredentialsException, InvalidKeyException, InvalidAlgorithmParameterException {
        JsonManager jm = new JsonManager();
        SessionManager sm = new SessionManager("C:\\passProtect\\pass");
        sm.openSession( "test", "test", "teeet" );
        sm.session = "test";
//         ArrayList<Object[]> data = new ArrayList<Object[]>();
//         Object[] o1 = { "Google", "Smith", "Snowboarding", "dlskafj", "" };
//         Object[] o2 = { "John", "Doe", "Rowing", "pass", "" };
//         Object[] o3 = { "paypal", "winthoutid@hotmail.fr", "", "pass", "" };
//        
//         data.add( o1 );
//         data.add( o2 );
//         data.add( o3 );
//         sm.saveIv( jm.serialize( (List<Object[]>) data, sm.cipher, "test"
//         + sm.dataExtension ) );
        
        byte[] iv = sm.readIv();
        System.out.println( iv );
        System.out.println( "version : " + sm.readVersion() );
        System.out.println( iv.length );
        ArrayList<Object[]> data = (ArrayList<Object[]>) jm.deserialize(
                sm.cipher, iv, sm.directoryPath + "\\" + sm.session
                        + sm.dataExtension );
        System.out.println( data == null );
        for( int i = 0; i < data.size(); i++ ){
            for( int j = 0; j < data.get( i ).length; j++ ){
                System.out.println( data.get( i )[ j ] );
            }
        }
        // sm.readIv();
        
    }
    
    
    /**********************************************************
     * constructors
     **********************************************************/
    public SessionManager() {
    }
    
    
    public SessionManager(String directoryPath) {
        this.directoryPath = directoryPath;
    }
    
    
    /**
     * Saves the iv (a limited array of bytes generated during encryption) into
     * the specified file
     * 
     * @param iv
     * @param path
     * @throws IOException
     */
    public void saveIv( byte[] iv ) throws IOException {
        
        FileOutputStream fos = new FileOutputStream( new File( directoryPath
                + "\\" + session + this.ivExtension ) );
        
        // fos.write( ( "version " + currentVersion + newLine ).getBytes() );
        // fos.write( ("ivlength " + iv.length + newLine).getBytes() );
        // fos.write( ("iv-").getBytes());
        fos.write( currentVersion );
        fos.write( iv );
        fos.flush();
        fos.close();
        
    }// end saveIV
    
    
    /**
     * returns an array of strings with all the existing sessions
     * 
     * @param pathToDir
     *            the directory containing the sessions
     * @param pattern
     *            the regex to recognize the sessions
     * @return
     * @throws FileNotFoundException
     */
    public String[] availableSessions( String pathToDir, String pattern )
            throws FileNotFoundException {
        
        File folder = new File( pathToDir );
        if( !folder.exists() || !folder.isDirectory() ){
            throw new FileNotFoundException( pathToDir
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
     * returns an array of strings with all the existing sessions
     * 
     * @return
     * @throws FileNotFoundException
     */
    public String[] availableSessions() throws FileNotFoundException {
        return this.availableSessions( directoryPath, ".*\\" + dataExtension
                + "$" );
    }
    
    
    /**
     * returns true if a session is currently opened
     * 
     * @return
     */
    public boolean isOpened() {
        return ( this.cipher == null ? false : true );
    }
    
    
    
    public void close(){
        this.cipher = null;
    }
    
    
    /**
     * creates a new session
     * 
     * @param directoryPath
     * @param sessionName
     * @param pass
     * @param salt
     * @throws CryptoException
     */
    public void createSession( String directoryPath, String sessionName,
            String pass, String salt ) throws CryptoException {
        
        this.directoryPath = directoryPath;
        this.session = sessionName;
        try{
            this.cipher = new Crypto( keyFactAlgo, cipherTransfo,
                    encryptionType, keyIterationCount, keyLength, pass, salt );
        }catch( NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeySpecException e ){
            e.printStackTrace();
            throw new Exceptions.CryptoException();
        }
        
    }
    
    
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
    public void createSession( String sessionName, String pass, String salt )
            throws FileNotFoundException, CryptoException {
        
        if( this.directoryPath == null )
            throw new FileNotFoundException( "no directoryPath specified" );
        
        this.session = sessionName;
        try{
            this.cipher = new Crypto( keyFactAlgo, cipherTransfo,
                    encryptionType, keyIterationCount, keyLength, pass, salt );
            
        }catch( NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeySpecException e ){
            e.printStackTrace();
            throw new Exceptions.CryptoException();
        }
        
    }
    
    
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
    
    
    /**
     * open a session and returns the datas of the jtable
     * 
     * @param pass
     * @param salt
     * @return
     * @throws CryptoErrorException
     * @throws WrongCredentialsException
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> openSession( String session, String pass, String salt )
            throws Exceptions.CryptoException,
            Exceptions.WrongCredentialsException {
        
        this.session = session;
        
        try{
            
            this.cipher = new Crypto( keyFactAlgo, cipherTransfo,
                    encryptionType, keyIterationCount, keyLength, pass, salt );
            
//            return (List<Object[]>) cipher.deserializeObject(
//                    this.directoryPath + "\\" + this.session
//                            + this.dataExtension, this.readIv() );
            

            return new JsonManager().deserialize( this.cipher, this.readIv(),
                    this.directoryPath + "\\" + this.session
                            + this.dataExtension );

        }catch( Exceptions.WrongCredentialsException e ){
          // if the pass was wrong, loops again
          throw new Exceptions.WrongCredentialsException( session
                  + " : wrong salt and/or password" );     
        }catch( Exception ee ){
            // if the pass was wrong, loops again
            throw new Exceptions.CryptoException(
                    ee.getMessage() + " An unplanned error occurred during crypto "
                            + ee.toString() );
        }
        
    }
    
    
    /**
     * reads and return the iv (a limited array of bytes used for decryption)
     * from the specified file
     * 
     * @param path
     * @return
     * @throws IOException
     * @throws IvNotFoundException
     */
    private byte[] readIv() throws IOException, IvNotFoundException {
        
        // File file = new File( directoryPath + "\\" + session + ivExtension );
        // FileInputStream fis = new FileInputStream( file );
        // Scanner sc = new Scanner( fis );
        //
        //
        // try{
        // // sc.useDelimiter(newLine);
        // sc.findWithinHorizon( Pattern.compile( "ivlength " ),
        // (int) file.length() );
        // byte[] iv = new byte[sc.nextInt()];
        // int counter = 0;
        // while(!sc.hasNextByte());
        // while( sc.hasNextByte() ){
        // iv[ counter++ ] = sc.nextByte();
        // }
        // return iv;
        // }catch( InputMismatchException e ){
        // throw new Exceptions.IvNotFoundException();
        // }finally{
        // sc.close();
        // }
        
        File file = new File( directoryPath + "\\" + session + ivExtension );
        FileInputStream fin = new FileInputStream( file );
        fin.read();
        byte[] iv = new byte[(int) file.length() - 1];
        fin.read( iv );
        fin.close();
        
        return iv;
        
    }// end readIv
    
    
    public int readVersion() throws VersionNumberNotFoundException, IOException {
        
        FileInputStream fin = null;
        try{
            File file = new File( directoryPath + "\\" + session + ivExtension );
            fin = new FileInputStream( file );
            return fin.read();
        }catch( IOException e ){
            e.printStackTrace();
            throw new Exceptions.VersionNumberNotFoundException();
        }finally{
            if( fin != null )
                fin.close();
        }
    }
    
}