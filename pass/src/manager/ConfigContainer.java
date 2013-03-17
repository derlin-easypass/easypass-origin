package manager;

import models.AbstractGsonContainer;
import models.ConfigFileManager2;
import passinterface.PathToRefactor;

import java.io.File;

/**
 * User: lucy
 * Date: 07/03/13
 * Version: 0.1
 */
public class ConfigContainer extends AbstractGsonContainer {

    @PathToRefactor
    public String application$path;
    @PathToRefactor
    public String session$path;
    public String[] column$names;
    public int[] column$dimensions;
    public int window$height, window$width;
    private static boolean debug = true;


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

            conf.updatePaths();
            System.out.println( conf );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }//end main


    @Override
    public Object getProperty( String key ) {
        return super.getProperty( key.trim().replace( ' ', '$' ) );
    }//end getProperty


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "apppath: ").append( this.application$path ).append( "\n" );
        builder.append( "sessionpath: ").append( this.session$path ).append( "\n" );

        builder.append( "coldimensions: " );
        if( column$dimensions != null ) {
            for( int i : column$dimensions ) {
                builder.append( " ").append(  i );
            }//end for
        }
        builder.append( "\n" );

        builder.append( "colnames: " );
        if( column$names != null ) {
            for( String colName : column$names ) {
                builder.append( " ").append( colName );
            }//end for
        }
        builder.append( "\n" );

        builder.append( "winheight: ").append( this.window$height ).append( "\n" );
        builder.append( "winhwidth: ").append( this.window$width ).append( "\n" );
        return builder.toString();
    }

}//end class
