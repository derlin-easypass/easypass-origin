package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.Exceptions.WrongCredentialsException;
import models.JsonManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
public class PassJsonManager extends JsonManager {


    public PassJsonManager() {

    }//end PassJsonManager


    public PassJsonManager( byte[] magicNumber ) {
        super( magicNumber );
    }//end PassJsonManager


    public static void main( String[] args ) throws Exception {

        new PassJsonManager().deserialize( "aes-128-cbc", "/home/lucy/empty", "salfkj" );
        System.out.println( "done" );
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
        //        try {
        //
        //        fin = new FileInputStream(
        //        "C:\\Users\\lucy\\AppData\\Roaming\\easypass\\sessions\\prout.data_ser");
        //        ArrayList<Object[]> data_decrypt = new GsonBuilder()
        //        .create()
        //        .fromJson(
        //        new InputStreamReader(OpenSSL.decrypt(
        //        "aes-128-cbc", password.toCharArray(), fin)),
        //        new TypeToken<List<Object[]>>() {
        //        }.getType());
        //
        //        for (Object[] o : data_decrypt) {
        //        for (Object s : o) {
        //        System.out.println((String) s);
        //        }
        //        }
        //        } catch (JsonSyntaxException | IllegalStateException e) {
        //        // throw new Exceptions.WrongCredentialsException();
        //        e.printStackTrace();
        //
        //        } finally {
        //        if (fin != null)
        //        fin.close();
        //        }// end try

    }


    public List<Object[]> deserialize( String algo, String filepath,
                                       String password ) throws WrongCredentialsException,
            IOException {

        return ( List<Object[]> ) super.deserialize( algo, filepath, password,
                new TypeToken<ArrayList<Object[]>>() {
        }.getType() );

    }// end deserialize


    /**
     * writes the data in proper json in the specified file
     *
     * @param data
     * @param file
     * @throws IOException
     */
    public void writeToFile( List<Object[]> data, File file ) throws IOException {

        BufferedWriter bf = new BufferedWriter( new FileWriter( file ) );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        GsonContainer[] container = new GsonContainer[ data.size() ];

        for( int i = 0; i < data.size(); i++ ) {
            container[ i ] = new GsonContainer( data.get( i ) );
        }

        bf.write( "// This json file was produced by EasyPass version 1.0 [@author Lucy Linder]. " +
                "" );
        bf.newLine();
        bf.write( "// @date " + new Date().toString() );
        bf.newLine();
        bf.newLine();
        bf.write( gson.toJson( container ) );
        bf.close();
        System.out.println( "data written to file " + file.getAbsolutePath() );
    }


    /**
     * *****************************************************
     * container used for clean printing in json, i.e. adds the correct "labels"
     * to each value and writes a "pretty" file.
     *
     * @author me
     *         <p/>
     *         ****************************************************
     */
    private static class GsonContainer {
        String account;
        String pseudo;
        String emailAddress;
        String password;
        String notes;


        GsonContainer( Object[] obj ) {
            setValues( obj );
        }


        private void setValues( Object[] obj ) {

            account = ( String ) obj[ 0 ];
            pseudo = ( String ) obj[ 1 ];
            emailAddress = ( String ) obj[ 2 ];
            password = ( String ) obj[ 3 ];
            notes = ( String ) obj[ 4 ];
        }
    }//end private class

}// end class
