package models;

import com.google.gson.GsonBuilder;
import models.Exceptions.ConfigFileNotFoundException;
import models.Exceptions.ConfigFileWrongSyntaxException;
import models.Exceptions.NoSuchSettingException;
import passinterface.GsonContainable;

import java.io.*;

public class ConfigFileManager {

    boolean debug = true;


    public static void main( String[] args ) throws ConfigFileNotFoundException,
            ConfigFileWrongSyntaxException, NoSuchSettingException {
        //
        //                Gson gson = new Gson();
        //
        //                PassConfigContainer conf = new PassConfigContainer();
        //                conf.window$height = 300;
        //                conf.window$width = 200;
        //                conf.application$path = "%APPDIR%";
        //                conf.session$path = "%PARENTOFJAR%/sessions";
        //                conf.column$dimensions = new int[]{ 250, 250, 150, 100, 450 };
        //
        //                String str = new GsonBuilder().setPrettyPrinting().create().toJson( conf,
        //                        PassConfigContainer.class );
        //
        //                FileOutputStream fos = null;
        //
        //
        //                try {
        //
        //                    fos = new FileOutputStream( "config.json" );
        //
        //
        //                    fos.write( str.getBytes() );
        //
        //
        //                } catch( Exception e ) {
        //                    e.printStackTrace();
        //                }
        //
        //
        //        try {
        //            PassConfigContainer conf = ( PassConfigContainer ) new ConfigFileManager()
        // .getJsonFromFile( new
        //                    File( "config.json" ), new PassConfigContainer() );
        //            System.out.println( conf );
        //        } catch( FileNotFoundException e ) {
        //            e.printStackTrace();
        //        }

    }


    /**
     * reads a json file and stores its content into the fields of
     * the container object. <br />
     * The json entries must match the container fields. If a field of the container does not
     * appear in the json file (or its value is left empty), the field is set to null.
     * non-existent json entries ,
     *
     * @param file the json file to read from
     *
     */
    public GsonContainable getJsonFromFile( File file, GsonContainable container ) {

        FileInputStream fin = null;
        try {
            fin = new FileInputStream( file );
            return getJsonFromFile( fin, container );
        } catch( FileNotFoundException e ) {
            e.printStackTrace();
        }
        return null;
    }// end getJsonFromFile


    /**
     * parses the specified json file and returns a map storing the key/value
     * pairs.
     *
     * @param stream the filestream to a json file
     * @return the map holding the key/value pairs or null
     */
    public GsonContainable getJsonFromFile( InputStream stream, GsonContainable container ) {

        try {
            return new GsonBuilder().create().fromJson( new InputStreamReader( stream ),
                    container.getClass() );

        } catch( Exception e ) {
            System.out.println( e.getMessage() );
        }
        return null;

    }// end getJsonFromFile


    public boolean writeJsonFile( File file, GsonContainable container ) {
        try {
            FileOutputStream fos;
            String str = new GsonBuilder().setPrettyPrinting().create().toJson( container,
                    container.getClass() );

            fos = new FileOutputStream( file );
            fos.write( str.getBytes() );
            fos.close();
            return true;
        } catch( Exception e ) {
            System.out.println( e.getMessage() );
        }

        return false;
    }//end writeJsonFile


    public boolean writeJsonFile( String filepath, GsonContainable container ) {
        return this.writeJsonFile( new File( filepath ), container );
    }//end writeJsonFile

}// end class
