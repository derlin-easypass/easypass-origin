package inc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Exceptions;
import models.Exceptions.WrongCredentialsException;
import models.JsonManager;

import org.apache.commons.ssl.OpenSSL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * this class provides utilities in order to encrypt/data data (openssl style,
 * pass and auto-generated salt, no iv) and to serialize/deserialize them (in a
 * json format).
 * 
 * it is also possible to write the content of a list in a cleartext "pretty"
 * valid json format.
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 * 
 */
public class PassJsonManager extends JsonManager {
    
    public static void main( String[] args ) throws Exception {
        
        new PassJsonManager().deserialize( "aes-128-cbc",
                "C:\\Users\\lucy\\AppData\\Roaming\\easypass\\sessions\\prout",
                "salfkj" );
        // ArrayList<Object[]> data = new ArrayList<Object[]>();
        // Object[] o1 = { "Google", "Smith", "Snowboarding", "dlskafj", "" };
        // Object[] o2 = { "John", "Doe", "Rowing", "pass", "" };
        // Object[] o3 = { "paypal", "winthoutid@hotmail.fr", "", "pass", "" };
        //
        // data.add(o1);
        // data.add(o2);
        // data.add(o3);
        //
        // String password = "essai"; // {'c','h','a','n','g','e','i','t'};
        //
        // // Encrypt!
        //
        // Gson gson = new GsonBuilder().create();
        // FileOutputStream fs = new FileOutputStream(new File(
        // "C:\\Users\\lucy\\AppData\\Roaming\\easypass\\sessions\\prout"));
        // fs.write(OpenSSL.encrypt("aes-128-cbc", password.toCharArray(), gson
        // .toJson(data).getBytes()));
        // fs.write("\r\n".getBytes());
        // fs.close();
        //
        // // decrypt
        //
        // FileInputStream fin = null;
        // password = "ess";
        //
        // try {
        //
        // fin = new FileInputStream(
        // "C:\\Users\\lucy\\AppData\\Roaming\\easypass\\sessions\\prout.data_ser");
        // ArrayList<Object[]> data_decrypt = new GsonBuilder()
        // .create()
        // .fromJson(
        // new InputStreamReader(OpenSSL.decrypt(
        // "aes-128-cbc", password.toCharArray(), fin)),
        // new TypeToken<List<Object[]>>() {
        // }.getType());
        //
        // for (Object[] o : data_decrypt) {
        // for (Object s : o) {
        // System.out.println((String) s);
        // }
        // }
        // } catch (JsonSyntaxException | IllegalStateException e) {
        // // throw new Exceptions.WrongCredentialsException();
        // e.printStackTrace();
        //
        // } finally {
        // if (fin != null)
        // fin.close();
        // }// end try
        
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
     *
    public void serialize(List<Object[]> data, String algo, String filepath,
			String password) throws IOException {

    	super.serialize(data, algo, filepath, password);
		FileOutputStream fos = null;

		try {
		    
			Gson gson = new GsonBuilder().create();
			fos = new FileOutputStream(filepath);
			fos.write(OpenSSL.encrypt(algo, password.toCharArray(), gson
					.toJson(data).getBytes("UTF-8")));
			fos.write("\r\n".getBytes());
			fos.write(System.getProperty("line.separator").getBytes());
			fos.flush();

	
		}catch( GeneralSecurityException e ){
            e.printStackTrace();
        } finally {
			if (fos != null)
				fos.close();
		}

	}// end serialize*/
    
    
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
    public List<Object[]> deserialize( String algo, String filepath,
            String password ) throws WrongCredentialsException, IOException {  
    	
    	return (List<Object[]>) super.deserialize(algo, filepath, password, new TypeToken<ArrayList<Object[]>>(){}.getType());
        
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
     * to each value and writes a "pretty" file.
     * 
     * @author me
     * 
     ******************************************************/
    private static class GsonContainer {
        String account;
        String pseudo;
        String emailAddress;
        String password;
        String notes;
        
        
        GsonContainer(Object[] obj) {
            setValues( obj );
        }
        
        
        private void setValues( Object[] obj ) {
            
            account = (String) obj[ 0 ];
            pseudo = (String) obj[ 1 ];
            emailAddress = (String) obj[ 2 ];
            password = (String) obj[ 3 ];
            notes = (String) obj[ 4 ];
        }
    }//end private class
    
}// end class