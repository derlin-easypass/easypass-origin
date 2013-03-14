package manager;

import main.Main;
import models.ConfigFileManager;
import models.Exceptions;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

public class PassConfigManager extends ConfigFileManager {
    
    private boolean areUserSettings = false;
    private boolean debug = true;
    
    
    /*
     * public static void main(String[] args) throws
     * ConfigFileNotFoundException,
     * 
     * ConfigFileWrongSyntaxException, NoSuchSettingException {
     * 
     * Gson gson = new Gson();
     * 
     * Map<String, String> map = new HashMap<String, String>(); map.put("name",
     * "demo"); map.put("fname",
     * "account, pseudo, email address,password, notes" );
     * 
     * String str = gson.toJson(map); System.out.println(str);
     * 
     * Map<String, String> map2 = gson.fromJson(str, new TypeToken<Map<String,
     * String>>() { }.getType());
     * 
     * Iterator it = map2.entrySet().iterator();
     * 
     * while (it.hasNext()) { Map.Entry pairs = (Map.Entry) it.next();
     * System.out.println(pairs.getKey() + " = " + pairs.getValue()); }
     * 
     * ConfigFileManager_try cm = new ConfigFileManager_try(
     * "/home/lucy/git/test_json"); System.out.println(cm.getProperty("name"));
     * 
     * } //
     */
    
    /**
     * creates a configFileManager which holds a <code>Map<string,string></code>
     * of all the settings for the application.<br>
     * The constructor will first load a default (not customizable)
     * settings.json from the root of the jar. If the latter is not found, an
     * exception is thrown.<br>
     * <br>
     * It will then try to find a user config file at the path(s) indicated in
     * the <code>user configfile</code> entry of the default
     * <code>settings.json</code>. If one is found, the default and custom
     * settings will be merged (custom ones having the priority).<br>
     * <br>
     * Notes :
     * <ul>
     * <li>this class will only check for correct json syntax. The validity of
     * the settings must be checked by the caller.</li>
     * <li>each option is parsed and the following patterns will be replaced :
     * <ul>
     * <li>%APPDIR% => the application directory, ~/.easypass in linux and
     * user.home/appdata/easypass in windows</li>
     * <li>%PARENTOFJAR% => the parent directory of the jar file</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @throws Exceptions.ConfigFileNotFoundException
     */
    public PassConfigManager() throws Exceptions.ConfigFileNotFoundException {
        // loads default settings
        super( PassConfigManager.class.getClassLoader().getResourceAsStream(
                "settings.json" ) );
        
        // replaces patterns by correct paths
        updateSettings();
        
        Map<String, String> userSettings = null;
        // try to find user defined settings
        for( String filepath : this.settings.get( "user configfile" ).split(
                "," ) ){
            
            File configFile = new File( filepath );
            if( configFile.exists() && configFile.isFile() ){
                try{
                    // try to read json from file
                    userSettings = getJsonFromFile( configFile );
                    this.areUserSettings = true;
                    if( debug )
                        System.out.println( "\nuser configfile '" + filepath
                                + "' loaded\n" );
                    break;
                }catch( Exception e ){
                    continue;
                }// end try
            }// end if
            
        }// end for
         // if user settings found, merge + replace patterns by paths
        if( areUserSettings ){
            mergeSettings( userSettings );
            updateSettings();
        }
        // if debug enabled, prints the final settings
        if( debug ){
            System.out.println( "user config file "
                    + ( areUserSettings ? "found." : "not found." ) );
            for( String key : this.settings.keySet() ){
                System.out.println( key + ": " + settings.get( key ) );
                
            }// end for
        }
    }// end constructor
    
    
    public Map<String,String> getMap() {
        return this.settings;
    }//end getMap
    /**
     * changes the specified property stored into the map.<br>
     * Note : this will not update the settings file !!When the program exits,
     * the modification will be lost.
     * 
     * @param key
     *            the key of the property to change
     * @param value
     *            the new value
     */
    public void setProperty( String key, String value ) {
        this.settings.put( key, value );
    }// end setProperty
    
    
    /**
     * merges the default settings with the settings held into the specified
     * map. The latter has the priority.
     * 
     * @param userSettings
     */
    private void mergeSettings( Map<String, String> userSettings ) {
        
        for( String key : userSettings.keySet() ){
            String value = userSettings.get( key );
            if( value != null && !value.isEmpty() )
                this.settings.put( key, value );
        }// end for
        
    }// end mergeSettings
    
    
    /**
     * updates the entries of the map by replacing the following patterns
     * <ul>
     * <li>%APPDIR% => the application directory, ~/.easypass in linux and
     * user.home/appdata/easypass in windows</li>
     * <li>%PARENTOFJAR% => the parent directory of the jar file</li>
     * </ul>
     * 
     * @throws URISyntaxException
     */
    private void updateSettings() {

        String jarDir = new File( PassConfigManager.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath() ).getParentFile()
                .getPath();
        
        if( debug )
            System.out.println( "\njarDir: " + jarDir );
        
        // replaces keywords with accurate paths
        for( String key : this.settings.keySet() ){
            String value = this.settings.get( key );
            value = value.replace( "%APPDIR%", getApplicationPath() )
                    .replace( "%PARENTOFJAR%", jarDir )
                    .replace( "\\", File.separator )
                    .replace( "/", File.separator );
            this.settings.put( key, value );
        }// end for
    }// end updateSettings
    
    
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
        if( os.contains( "Linux" ) || os.contains( "Mac" ) ){
            return System.getProperty( "user.home" ) + File.separator + "."
                    + Main.APPLICATION_NAME;
        }else if( os.contains( "Windows" ) ){
            return System.getenv( "APPDATA" ) + File.separator
                    + Main.APPLICATION_NAME;
            
        }else{
            System.out.println( "os " + os + " not supported." );
            System.exit( 0 );
            return null;
            // TODO
        }
    }// end getApplicationPath
    
}// end class
