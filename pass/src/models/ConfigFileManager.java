package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Exceptions;
import models.Exceptions.ConfigFileNotFoundException;
import models.Exceptions.ConfigFileWrongSyntaxException;
import models.Exceptions.NoSuchSettingException;

import org.apache.commons.ssl.OpenSSL;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ConfigFileManager {
    
    protected Map<String, String> settings;
    
    boolean debug = true;
    
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
    
    protected ConfigFileManager() {
    }
    
    
    /**
     * tries to read json from the specified files and stores the key-values
     * pairs into a <code>Map<String, String></code>. The key/values are the
     * naccessible by calling {@link getProperty(String) getProperty}. <br>
     * Note : only the first valid json file found is processed
     * 
     * @param configFilePaths
     *            the paths to potential json files
     * @throws Exceptions.ConfigFileNotFoundException
     */
    public ConfigFileManager(String... configFilePaths)
            throws Exceptions.ConfigFileNotFoundException {
        
        for( String filepath : configFilePaths ){
            File configFile = new File( filepath );
            if( configFile.exists() && configFile.isFile() ){
                try{
                    // try to read json from file
                    this.settings = getJsonFromFile( configFile );
                    return;
                }catch( Exception e ){
                    continue;
                }// end try
            }// end if
            
        }// end for
        
        throw new Exceptions.ConfigFileNotFoundException();
        
    }// end constructor
    
    
    /**
     * tries to read json from the specified filestream and stores the
     * key-values pairs into a <code>Map<String, String></code>. The key/values
     * are the naccessible by calling {@link getProperty(String) getProperty}. <br>
     * Note : only the first valid json file found is processed
     * 
     * @param stream
     * @throws Exceptions.ConfigFileNotFoundException
     */
    public ConfigFileManager(InputStream stream)
            throws Exceptions.ConfigFileNotFoundException {
        try{
            this.settings = getJsonFromFile( stream );
        }catch( Exception e ){
            throw new Exceptions.ConfigFileNotFoundException();
        }// end try
        
    }// end constructor
    
    
    /**
     * reads a json file and stores its content into the settings attribute of
     * this object.
     * 
     * @param file
     *            the json file to read from
     * @throws FileNotFoundException
     * @throws ConfigFileWrongSyntaxException
     */
    protected Map<String, String> getJsonFromFile( File file )
            throws FileNotFoundException, ConfigFileWrongSyntaxException {
        
        FileInputStream fin = new FileInputStream( file );
        return this.getJsonFromFile( fin );
        
    }// end getJsonFromFile
    
    
    /**
     * parses the specified json file and returns a map storing the key/value
     * pairs.
     * 
     * @param stream
     *            the filestream to a json file
     * @return the map holding the key/value pairs
     * @throws FileNotFoundException
     * @throws ConfigFileWrongSyntaxException
     */
    protected Map<String, String> getJsonFromFile( InputStream stream )
            throws FileNotFoundException, ConfigFileWrongSyntaxException {
        
        Map<String, String> map = new GsonBuilder().create().fromJson(
                new InputStreamReader( stream ),
                new TypeToken<Map<String, String>>() {
                }.getType() );
        
        if( map == null )
            throw new Exceptions.ConfigFileWrongSyntaxException();
        
        return map;
    }// end getJsonFromFile
    
    
    /**
     * returns the value of the given property, found in the configfile
     * specified in the constructor.
     * 
     * @param property
     *            the name of the property
     * @return the value associated with this property
     * @throws NoSuchSettingException
     */
    public String getProperty( String property ) throws NoSuchSettingException {
        
        String value = this.settings.get( property );
        
        if( value == null || value.isEmpty() ){
            throw new Exceptions.NoSuchSettingException( property
                    + " could not be found" );
        }// end if
        
        return value;
    }// end getProperty

    
}// end class
