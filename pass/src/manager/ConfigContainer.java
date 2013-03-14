package manager;

import main.Main;
import models.AbstractGsonContainer;
import models.ConfigFileManager2;

import java.io.File;

/**
 * User: lucy
 * Date: 07/03/13
 * Version: 0.1
 */
public class ConfigContainer extends AbstractGsonContainer {

    public String appPath;
    public String sessionPath;
    public String[] colNames;
    public int[] colDimensions;
    public int windowHeight, windowWidth;


    public static void main( String[] args ) {
        try {
            ConfigContainer conf = ( ConfigContainer ) new ConfigFileManager2().getJsonFromFile(
                    new File( "test_config" ), new ConfigContainer() );
            System.out.println( conf );

            ConfigContainer conf2 = ( ConfigContainer ) new ConfigFileManager2().getJsonFromFile(
                    new File( "test_config2" ), new ConfigContainer() );
            System.out.println( conf2 );

            conf.mergeSettings( conf2 );

            System.out.println( conf );

            conf.update( conf.getApplicationPath() );
            System.out.println( conf );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }//end main


    /**
     * gets the path to the application folder, i.e. <user>/AppData/<appliName>
     * under windows and <user.home>/.<appliName> under Linux.
     *
     * @return
     */
    protected String getApplicationPath() {

        String os = System.getProperty( "os.name" );

        // depending on the os system, choose the best location to store session
        // data
        if( os.contains( "Linux" ) || os.contains( "Mac" ) ) {
            return System.getProperty( "user.home" ) + File.separator + "." + Main.APPLICATION_NAME;
        } else if( os.contains( "Windows" ) ) {
            return System.getenv( "APPDATA" ) + File.separator + Main.APPLICATION_NAME;

        } else {
            System.out.println( "os " + os + " not supported." );
            System.exit( 0 );
            return null;
            // TODO
        }
    }// end getApplicationPath


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "apppath: " + this.appPath );
        builder.append( "\n" );
        builder.append( "sessionPath: " + this.sessionPath );
        builder.append( "\n" );
        builder.append( "coldimensions: " );
        if( colDimensions != null ) {
            for( int i : colDimensions ) {
                builder.append( " " + i );
            }//end for
        }
        builder.append( "\n" );
        builder.append( "colnames: " );
        if( colNames != null ) {
            for( String colName : colNames ) {
                builder.append( " " + colName );
            }//end for
        }
        builder.append( "winheight: " + this.windowHeight );
        builder.append( "\n" );
        builder.append( "winhwidth: " + this.windowWidth );
        builder.append( "\n" );
        return builder.toString();
    }

}//end class
