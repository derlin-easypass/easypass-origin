package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * <p/>
 * To deserialise data from the terminal, type:
 * <code>
 * openssl enc -d -aes-128-cbc -a -in [name].data_ser -k [pass]
 * </code>
 *
 * @author Lucy Linder
 * @date Dec 21, 2012
 */
public class SSLSerializer<T>{

    protected byte[] magicNumber;
    private Type type;
    private String algo;

    public SSLSerializer( byte[] magicNumber ){
        this.magicNumber = magicNumber;
    }//end JsonManager

    public SSLSerializer(Type type, String algo){
        this.type = type;
        this.algo = algo;
    }//end constructor


    public T deserialize( String path, String password ) throws Exception{
        return (T) deserialize( algo, path, password, type );
    }//end

    public void serialize( T data, String path, String password ) throws Exception{
        serialize( data, algo, path, password );
    }//end


    /**
     * see {@link #serialize(Object, String, byte[], String, String)}
     */
    public static void serialize( Object data, String algo, String filepath, String password ) throws IOException{
       serialize( data, algo, null, filepath, password );
    }

    /**
     * encrypts the object with the cipher given in parameter and
     * serializes it in json format.
     *
     * @param data     the data
     * @param algo     the algorithm (aes-128-cbc for example, see the openssl conventions)
     * @param magicNumber if not null, will be written as it is at the top of the file
     * @param filepath the output filepath
     * @param password the password
     * @throws java.io.IOException
     */
    public static void serialize( Object data, String algo, byte[] magicNumber, String filepath, String password ) throws IOException{

        FileOutputStream fos = null;

        try{

            Gson gson = new GsonBuilder().create();
            fos = new FileOutputStream( filepath );

            if( magicNumber != null ){
                fos.write( magicNumber );
            }

            fos.write( OpenSSL.encrypt( algo, password.toCharArray(), gson.toJson( data ).getBytes( "UTF-8" ) ) );
            fos.write( "\r\n".getBytes() );
            fos.write( System.getProperty( "line.separator" ).getBytes() );
            fos.flush();

        }catch( GeneralSecurityException e ){
            e.printStackTrace();
        }finally{
            if( fos != null ) fos.close();
        }

    }// end serialize


    /**
     * see {@link #deserialize(String, byte[], String, String, java.lang.reflect.Type)}
     */
    public static Object deserialize( String algo, String filepath, String password, Type type ) throws Exception{
        return deserialize( algo, null, filepath, password, type );
    }


    /**
     * deserializes and returns the object of type "Type" contained in the
     * specified file. the decryption of the data is performed with the cipher
     * given in parameter.<br />
     * The object in the file must have been encrypted after a json serialisation.
     *
     * @param algo        the algorithm (aes-128-cbc for example, see the openssl conventions)
     * @param magicNumber if not null, will be read from the top of the file
     * @param filepath    the filepath
     * @param password    the password
     * @param type        the type of the data serialized
     * @return the decrypted data ( a list of ? )
     * @throws java.io.IOException
     */
    public static Object deserialize( String algo, byte[] magicNumber, String filepath, String password,
                                      Type type ) throws Exception{

        FileInputStream fin = null;

        try{

            fin = new FileInputStream( filepath );

            if( magicNumber != null ){
                byte[] b = new byte[ magicNumber.length ];
                fin.read( b );
            }
            List<?> data = ( new GsonBuilder().create().fromJson( new InputStreamReader( OpenSSL.decrypt( algo,
                    password.toCharArray(), fin ), "UTF-8" ), type ) );
            if( data == null ){
                throw new Exception( "data null" );
            }else{
                return data;
            }

        }finally{
            if( fin != null ) fin.close();
        }// end try

    }// end deserialize


}// end class
