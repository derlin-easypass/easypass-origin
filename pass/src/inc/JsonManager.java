package inc;

import java.io.*;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import models.Crypto;
import models.Exceptions;
import models.Exceptions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JsonManager {
    
    public static void main( String[] args ) throws Exception {
        
        // Crypto crypto = new Crypto( "PBKDF2WithHmacSHA1",
        // "AES/CBC/PKCS5Padding", "AES", 65536, 128, "pass", "salt" );
        
        // crypto.initCipherForEncryption();
        // System.out.println(crypto.getCipher().getIV().length);
        // saveIv(crypto.getCipher().getIV());
        ArrayList<Object[]> data = new ArrayList<Object[]>();
        Object[] o1 = { "Google", "Smith", "Snowboarding", "dlskafj", "" };
        Object[] o2 = { "John", "Doe", "Rowing", "pass", "" };
        Object[] o3 = { "paypal", "winthoutid@hotmail.fr", "", "pass", "" };
        
        data.add( o1 );
        data.add( o2 );
        data.add( o3 );
        
        // CipherOutputStream cout = new CipherOutputStream( fw,
        // crypto.getCipher() );
        // Gson gson = new GsonBuilder().create();
        // cout.write(gson.toJson( data ).getBytes());
        // cout.flush();
        // cout.close();
        // System.out.println( "ok" );
        // System.out.println(readIv().length);
        
    }
    
    
    /**
     * encrypts the arraylist of objects with the cipher given in parameter and
     * serializes it in json format. Careful : the cipher must be correctly
     * initialized for encryption
     * 
     * @param data
     * @param cipher
     *            the cipher previously initialized for encryption
     * @param filepath
     * @return
     * @throws IOException
     */
    public byte[] serialize( List<Object[]> data, Cipher cipher, String filepath )
            throws IOException {
        
        CipherOutputStream cout = null;
        
        try{
            FileOutputStream fos = new FileOutputStream( filepath );
            cout = new CipherOutputStream( fos, cipher );
            Gson gson = new GsonBuilder().create();
            cout.write( gson.toJson( data ).getBytes() );
            cout.flush();
            
            return cipher.getIV();
            
        }finally{
            if( cout != null )
                cout.close();
        }
        
    }// end serialize
    
    
    /**
     * deserializes and returns the arrayList<Object[]> contained in the
     * specified file. the decryption of the data is performed with the cipher
     * given in parameter.
     * 
     * @param cipher
     *            initialized for decryption and with the correct key
     * @param filepath
     * @return
     * @throws CryptoException
     *             If the cipher is not correctly initilialized (wrong algorithm
     *             for example)
     * @throws WrongCredentialsException
     *             If the key used by the cipher is not the correct one
     * @throws IOException
     *             If a problem occurs while opening/reading the file
     */
    public List<Object[]> deserialize( Cipher cipher, String filepath )
            throws CryptoException, WrongCredentialsException, IOException {
        
        CipherInputStream cin = null;
        
        try{
            
            cin = new CipherInputStream( new FileInputStream( filepath ),
                    cipher );
            
            return ( new GsonBuilder().create().fromJson(
                    new InputStreamReader( cin ),
                    new TypeToken<List<Object[]>>() {
                    }.getType() ) );
            
        }catch( JsonSyntaxException | IllegalStateException e ){
            throw new Exceptions.WrongCredentialsException();
            
        }finally{
            if( cin != null )
                cin.close();
        }// end try
        
    }// end deserialize
    
    
    /**
     * writes the data in proper json in the specified file
     * 
     * @param data
     * @param file
     * @throws IOException
     */
    public void writeToFile( List<Object[]> data, File file )
            throws IOException {
        
        BufferedWriter bf = new BufferedWriter( new FileWriter( file ) );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        GsonContainer[] container = new GsonContainer[data.size()];
        
        for( int i = 0; i < data.size(); i++ ){
            container[ i ] = new GsonContainer( data.get( i ) );
        }
        
        bf.write( "// This json file was produced by EasyPass version 1.0 [@author Lucy Linder]. " );
        bf.newLine();
        bf.write( "// @date " + new Date().toString() );
        bf.newLine();
        bf.newLine();
        bf.write( gson.toJson( container ) );
        bf.close();
        System.out.println( "data written to file " + file.getAbsolutePath() );
    }
    
    
    /********************************************************
     * container used for clean printing in json, i.e. adds the correct "labels"
     * to each value.
     * 
     * @author me
     * 
     ******************************************************/
    private static class GsonContainer {
        String account;
        String emailAddress;
        String password;
        String notes;
        
        
        GsonContainer(Object[] obj) {
            setValues( obj );
        }
        
        
        private void setValues( Object[] obj ) {
            
            account = (String) obj[ 0 ];
            emailAddress = (String) obj[ 1 ];
            password = (String) obj[ 2 ];
            notes = (String) obj[ 3 ];
        }
    }
    
}// end class
