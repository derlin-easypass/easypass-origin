package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import models.Exceptions.WrongCredentialsException;
import org.apache.commons.ssl.OpenSSL;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.List;


/**
 * this class provides utilities in order to encrypt/data data (openssl style,
 * pass and auto-generated salt, no iv) and to serialize/deserialize them (in a
 * json format).
 * <p/>
 * it is also possible to write the content of a list in a cleartext "pretty"
 * valid json format.
 *
 * @author Lucy Linder
 * @date Dec 21, 2012
 */
public class JsonManager {

    protected byte[] magicNumber;


    public JsonManager() {

    }//end JsonManager


    public JsonManager( byte[] magicNumber ) {
        this.magicNumber = magicNumber;
    }//end JsonManager


    /**
     * encrypts the arraylist of objects with the cipher given in parameter and
     * serializes it in json format.
     * @param data the data
     * @param algo the algorithm (aes-128-cbc for example, see the openssl conventions)
     * @param filepath the output filepath
     * @param password the password
     * @throws IOException
     */
    public void serialize( List<?> data, String algo, String filepath,
                           String password ) throws IOException {

        FileOutputStream fos = null;

        try {

            Gson gson = new GsonBuilder().create();
            fos = new FileOutputStream( filepath );

            if( magicNumber != null ) {
                fos.write( magicNumber );
            }

            fos.write( OpenSSL.encrypt( algo, password.toCharArray(),
                    gson.toJson( data ).getBytes( "UTF-8" ) ) );
            fos.write( "\r\n".getBytes() );
            fos.write( System.getProperty( "line.separator" ).getBytes() );
            fos.flush();

        } catch( GeneralSecurityException e ) {
            e.printStackTrace();
        } finally {
            if( fos != null ) fos.close();
        }

    }// end serialize


     /**
     * deserializes and returns the object of type "Type" contained in the
     * specified file. the decryption of the data is performed with the cipher
     * given in parameter.<br />
     * The object in the file must have been encrypted after a json serialisation.
     *
     * @param algo the algorithm (aes-128-cbc for example, see the openssl conventions)
     * @param filepath the filepath
     * @param password the password
     * @param type the type of the data serialized
     * @return the decrypted data ( a list of ? )
     * @throws WrongCredentialsException if the password or the magic number is incorrect
     * @throws IOException
     */
    public List<?> deserialize( String algo, String filepath, String password,
                                Type type ) throws WrongCredentialsException, IOException {

        FileInputStream fin = null;

        try {

            fin = new FileInputStream( filepath );

            if( magicNumber != null ) {
                byte[] b = new byte[ magicNumber.length ];
                fin.read( b );
            }
            List<?> data = ( new GsonBuilder().create().fromJson( new InputStreamReader( OpenSSL
                    .decrypt( algo, password.toCharArray(), fin ), "UTF-8" ), type ) );
            if( data == null ) {
                throw new Exceptions.WrongCredentialsException();
            } else {
                return data;
            }

        } catch( IOException e ) {
            throw new Exceptions.WrongCredentialsException( e.getMessage() );
        } catch( JsonIOException e ) {
            throw new Exceptions.WrongCredentialsException( e.getMessage() );
        } catch( JsonSyntaxException e ) {
            throw new Exceptions.WrongCredentialsException( e.getMessage() );
        } catch( GeneralSecurityException e ) {
            throw new Exceptions.WrongCredentialsException( e.getMessage() );
        } finally {
            if( fin != null ) fin.close();
        }// end try

    }// end deserialize


}// end class
