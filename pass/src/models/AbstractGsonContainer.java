package models;

import main.Main;
import manager.ConfigContainer;
import passinterface.GsonContainable;

import java.io.File;
import java.lang.reflect.Field;

/**
 * User: lucy
 * Date: 07/03/13
 * Version: 0.1
 */
abstract public class AbstractGsonContainer implements GsonContainable {

    public void mergeSettings( ConfigContainer overridingSettings ) {

        long time = System.currentTimeMillis();

        if( !( overridingSettings.getClass().equals( this.getClass() ) ) ) {
            throw new UnsupportedOperationException( "the two instances are not from the same " +
                    "class" );
        }//end if

        for( Field field : this.getClass().getFields() )
            try {
                field.setAccessible( true );
                Object val1 = field.get( this ), val2 = field.get( overridingSettings );

                if( val2 != null && !val2.equals( val1 ) ) field.set( this, val2 );
                //                if( !field.get( this ).equals( field.get( overridingSettings )
                // ) ) {
                //                    field.set( this, field.get( overridingSettings ) );
                //                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        //end for
        System.out.println( "acces time : " + ( System.currentTimeMillis() - time ) );
    }//end mergeSettings


    public void update( String applicationPath ) {

        String jarDir = new File( Main.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath() ).getParentFile().getPath();

        if( Main.debug ) System.out.println( "\njarDir: " + jarDir );

        // replaces keywords with accurate paths
        for( Field field : this.getClass().getFields() ) {
            String value = null;
            try {
                System.out.println( field.getType() );
                if( !( field.get( this ) instanceof String ) ) continue;
                value = ( String ) field.get( this );

                value = value.replace( "%APPDIR%", applicationPath ).replace( "%PARENTOFJAR%",
                        jarDir ).replace( "\\", File.separator ).replace( "/", File.separator );
                field.set( this, value );
            } catch( IllegalAccessException e ) {
                e.printStackTrace();
            }
        }// end for
    }// end updateSettings

}//end interface
