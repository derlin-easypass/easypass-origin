package ch.derlin.easypass.passinterface;

import java.io.FileNotFoundException;

/**
 * User: lucy
 * Date: 05/03/13
 * Version: 0.1
 */
public interface AbstractSessionChecker {

    String[] availableSessions() throws FileNotFoundException;
    boolean sessionExists(String name);
    boolean sessionExists( String path, String name );
    boolean areCredentialsValid(String sessionName, String password);

}//end interface
