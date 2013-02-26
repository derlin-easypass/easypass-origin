package inc;

import java.io.File;
import java.util.Map;

import models.ConfigFileManager_try;
import models.Exceptions;

public class PassConfigManager extends ConfigFileManager_try {
    
    private boolean areUserSettings = false;
    private boolean debug = false;
    
    
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
    
    public PassConfigManager() throws Exceptions.ConfigFileNotFoundException {
        
        super( PassConfigManager.class.getClassLoader().getResourceAsStream(
                "settings.json" ) );
        
        updateSettings();
        
        Map<String, String> userSettings = null;
        
        for( String filepath : this.settings.get( "user configfile" ).split(
                "," ) ){
            
            File configFile = new File( filepath );
            if( configFile.exists() && configFile.isFile() ){
                try{
                    // try to read json from file
                    userSettings = getJsonFromFile( configFile );
                    this.areUserSettings = true;
                    break;
                }catch( Exception e ){
                    continue;
                }// end try
            }// end if
            
        }// end for
        
        if( areUserSettings ){
            mergeSettings( userSettings );           
            updateSettings();
        }
        if( debug ){
            for( String key : this.settings.keySet() ){
                System.out.println( key + ": " + settings.get( key ) );
                
            }// end for
        }
    }// end constructor
    
    
    protected void setProperty( String key, String value ) {
        this.settings.put( key, value );
    }// end setProperty
    
    
    private void mergeSettings( Map<String, String> userSettings ) {
        
        for( String key : userSettings.keySet() ){
            String value = userSettings.get( key );
            if( value != null && !value.isEmpty() )
                this.settings.put( key, value );
        }// end for
        
    }// end mergeSettings
    
    
    private void updateSettings() {
        
        //gets the path to the parent dir of the jar file
        String pattern = File.separator + "[^" + File.separator + "]+\\.jar.*";
        String pathToParentOfJar = this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().getPath()
                .replaceAll( pattern, "" );
        
        //replaces keywords with accurate paths
        for( String key : this.settings.keySet() ){
            String value = this.settings.get( key );
            value = value
                    .replace( "%APPDIR%", getApplicationPath() )
                    .replace( "%PARENTOFJAR%", pathToParentOfJar );
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
                    + Easypass.APPLICATION_NAME;
        }else if( os.contains( "Windows" ) ){
            return System.getenv( "APPDATA" ) + File.separator
                    + Easypass.APPLICATION_NAME;
            
        }else{
            System.out.println( "os " + os + " not supported." );
            System.exit( 0 );
            return null;
            // TODO
        }
    }// end getApplicationPath
    
}// end class
