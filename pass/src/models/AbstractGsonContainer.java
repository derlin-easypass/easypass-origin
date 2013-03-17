package models;

import main.Main;
import manager.ConfigContainer;
import manager.PassConfigManager;
import passinterface.GsonContainable;
import passinterface.PathToRefactor;

import java.io.File;
import java.lang.reflect.Field;

/**
 * User: lucy
 * Date: 07/03/13
 * Version: 0.1
 */
abstract public class AbstractGsonContainer implements GsonContainable {

    protected static boolean debug = false;

    public Object getProperty( String key ) {
        try {
            return this.getClass().getField( key ).get( this );
        } catch( Exception e ) {
            throw new Exceptions.NoSuchSettingException();
        }
    }//end getProperty

    /**
     * merges the settings given in parameter with the settings of the current object. <br />
     * In case of conflict, the settings of the current object will be overridden !
     *
     * @param overridingSettings the container of the settings to merge. Note : its declared
     *                           class must be the same as the class of the current object !
     */
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


    /**
     * updates the settings marked with the {@link passinterface.PathToRefactor} annotation by
     * replacing the
     * following patterns :
     * <ul>
     * <li>%APPDIR% => the application directory, ~/.easypass in linux and
     * user.home/appdata/easypass in windows</li>
     * <li>%PARENTOFJAR% => the parent directory of the jar file</li>
     * </ul>   <br />
     * Note : the fields marked with this annotation must be of type string. If not,
     * they will be skipped.
     */
    protected void updatePaths() {

        String jarDir = new File( PassConfigManager.class.getProtectionDomain().getCodeSource()
                .getLocation().getPath() ).getParentFile().getPath();


        if( debug ) System.out.println( "\njarDir: " + jarDir );

        // replaces keywords with accurate paths
        for( Field field : this.getClass().getFields() ) {
            if( field.isAnnotationPresent( PathToRefactor.class ) ) {
                String value = null;
                try {
                    value = ( ( String ) field.get( this ) ).replace( "%APPDIR%",
                            getApplicationPath() ).replace( "%PARENTOFJAR%",
                            jarDir ).replace( "\\", File.separator ).replace( "/", File.separator );
                    field.set( this, value );
                } catch( IllegalAccessException e ) {
                    if( debug ) {
                        e.printStackTrace();
                    }//end if
                }//end try
            }//end if
        }//end for
    }//end updatePaths


    /* *****************************************************************
     * static utilities
     * ****************************************************************/


    /**
     * returns the path to the application folder, i.e. <user>/AppData/<appliName>
     * under windows and <user.home>/.<appliName> under Linux.
     *
     * @return the application path, i.e. the path (depending on the OS) where sessions and logs
     *         are stored by default
     */
    public static String getApplicationPath() {

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


   /* public void update( String applicationPath ) {

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
    }// end updateSettings   */

}//end interface
