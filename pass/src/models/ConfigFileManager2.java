package models;

import com.google.gson.GsonBuilder;
import manager.ConfigContainer;
import models.Exceptions.ConfigFileNotFoundException;
import models.Exceptions.ConfigFileWrongSyntaxException;
import models.Exceptions.NoSuchSettingException;
import passinterface.GsonContainable;

import java.io.*;

public class ConfigFileManager2 {

    boolean debug = true;


    public static void main( String[] args ) throws ConfigFileNotFoundException,

            ConfigFileWrongSyntaxException, NoSuchSettingException {

        //        Gson gson = new Gson();
        //
        //        ConfigContainer conf = new ConfigContainer();
        //        conf.windowHeight = 300;
        //        conf.windowWidth = 200;
        //        conf.appPath = "%APPDIR%";
        //        conf.sessionPath = "%PARENTOFJAR%/sessions";
        //        conf.colDimensions = new int[]{ 250, 250, 150, 100, 450 };
        //
        //        String str = new GsonBuilder().setPrettyPrinting().create().toJson( conf,
        //                ConfigContainer.class );

        //        FileOutputStream fos = null;
        //
        //
        //        try {
        //
        //            fos = new FileOutputStream( "test_config" );
        //
        //
        //            fos.write( str.getBytes() );
        //
        //
        //        } catch( Exception e ) {
        //            e.printStackTrace();
        //        }


        try {
            ConfigContainer conf = ( ConfigContainer ) new ConfigFileManager2().getJsonFromFile( new
                    File( "test_config" ), new ConfigContainer() );
            System.out.println( conf );
        } catch( FileNotFoundException e ) {
            e.printStackTrace();
        }

    }


    /**
     * reads a json file and stores its content into the settings attribute of
     * this object.
     *
     * @param file the json file to read from
     * @throws java.io.FileNotFoundException
     * @throws models.Exceptions.ConfigFileWrongSyntaxException
     *
     */
    public GsonContainable getJsonFromFile( File file, GsonContainable container )
            throws FileNotFoundException, ConfigFileWrongSyntaxException {

        FileInputStream fin = new FileInputStream( file );
        return getJsonFromFile( fin, container );

    }// end getJsonFromFile


    /**
     * parses the specified json file and returns a map storing the key/value
     * pairs.
     *
     * @param stream the filestream to a json file
     * @return the map holding the key/value pairs
     * @throws java.io.FileNotFoundException
     * @throws models.Exceptions.ConfigFileWrongSyntaxException
     *
     */
    public GsonContainable getJsonFromFile( InputStream stream,
                                                     GsonContainable container ) throws
            FileNotFoundException, ConfigFileWrongSyntaxException {

        try {
            return new GsonBuilder().create().fromJson( new InputStreamReader( stream ),
                    container.getClass() );

        } catch( Exception e ) {
            e.printStackTrace();
        }
        return null;

    }// end getJsonFromFile

}// end class
