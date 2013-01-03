package inc;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Exceptions;
import models.Exceptions.ConfigFileNotFoundException;
import models.Exceptions.ConfigFileWrongSyntaxException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigFileManager {
    
    private File configFile;
    
    
    public static void main( String[] args ) throws ConfigFileNotFoundException, ConfigFileWrongSyntaxException {
        ConfigFileManager confManager = new ConfigFileManager(
                System.getProperty( "user.home" ) + File.separator
                        + ".easypass/config.xml" );
        System.out.println(confManager.getProperty( "sessionPath" ));
    }
    
    
    public ConfigFileManager(String configFilePath)
            throws Exceptions.ConfigFileNotFoundException {
        
        this.configFile = new File( configFilePath );
        if( !configFile.exists() || !configFile.isFile() ){
            throw new Exceptions.ConfigFileNotFoundException();
        }
    }
    
    
    public String getProperty( String property )
            throws ConfigFileWrongSyntaxException {
        
        // parse using builder to get DOM representation of the XML file

            
        try{
            NodeList list;
                list = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse( this.configFile )
                        .getElementsByTagName( property );
                
                return list.item( 0 ).getFirstChild().getNodeValue();
                
            }catch( SAXException e ){
                throw new Exceptions.ConfigFileWrongSyntaxException( "SAX error: " + e.getMessage() );
            }catch( IOException e ){
                throw new Exceptions.ConfigFileWrongSyntaxException( "IO error: " + e.getMessage() );
            }catch( ParserConfigurationException e ){
                throw new Exceptions.ConfigFileWrongSyntaxException( "Parse error: " + e.getMessage() );
            }
            
            

        
    }
}//end class
