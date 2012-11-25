package inc;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import models.Crypto;
import models.Exceptions;
import models.Exceptions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JsonManager {
    
    // public static void main( String[] args ) throws Exception {
    
    // Crypto crypto = new Crypto( "PBKDF2WithHmacSHA1",
    // "AES/CBC/PKCS5Padding", "AES", 65536, 128, "pass", "salt" );
    
    // crypto.initCipherForEncryption();
    // System.out.println(crypto.getCipher().getIV().length);
    // saveIv(crypto.getCipher().getIV());
    // ArrayList<Object[]> data = new ArrayList<Object[]>();
    // Object[] o1 = { "Google", "Smith", "Snowboarding", "dlskafj", "" };
    // Object[] o2 = { "John", "Doe", "Rowing", "pass", "" };
    // Object[] o3 = { "paypal", "winthoutid@hotmail.fr", "", "pass", "" };
    //
    // data.add( o1 );
    // data.add( o2 );
    // data.add( o3 );
    //
    // FileOutputStream fw = new FileOutputStream( "test.gson" );
    // CipherOutputStream cout = new CipherOutputStream( fw,
    // crypto.getCipher() );
    // Gson gson = new GsonBuilder().create();
    // cout.write(gson.toJson( data ).getBytes());
    // cout.flush();
    // cout.close();
    // System.out.println( "ok" );
    // System.out.println(readIv().length);
    
    // }
    
    public byte[] serialize( List<Object[]> data, Crypto crypto, String filepath )
            throws IOException, CryptoException {
        
        CipherOutputStream cout = null;
        
        try{
            FileOutputStream fos = new FileOutputStream( filepath );
            crypto.initCipherForEncryption();
            cout = new CipherOutputStream( fos, crypto.getCipher() );
            Gson gson = new GsonBuilder().create();
            cout.write( gson.toJson( data ).getBytes() );
            cout.flush();
            
            return crypto.getCipher().getIV();
            
        }catch( InvalidKeyException e ){
            e.printStackTrace();
            System.out.println( "problem with cipher" );
            throw new CryptoException( "problem with cipher" );
        }finally{
            if( cout != null )
                cout.close();
        }
        
    }// end serialize
    
    
    public List<Object[]> deserialize( Crypto crypto, byte[] iv, String filepath )
            throws CryptoException, WrongCredentialsException, IOException {
        
        CipherInputStream cin = null;
        
        try{
            crypto.initCipherForDecryption( iv );
            cin = new CipherInputStream( new FileInputStream( filepath ),
                    crypto.getCipher() );
            
            return ( new GsonBuilder().create().fromJson(
                    new InputStreamReader( cin ),
                    new TypeToken<List<Object[]>>() {
                    }.getType() ) );
            
        }catch( JsonSyntaxException | IllegalStateException | InvalidKeyException ike ){
            throw new Exceptions.WrongCredentialsException();
        }catch( InvalidAlgorithmParameterException iae ){
            iae.printStackTrace();
            throw new CryptoException( "problem with cipher" );
        }finally{
            if( cin != null )
                cin.close();
        }// end try
        
    }// end deserialize
    
}// end class
