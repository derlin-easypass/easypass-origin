package models;

public class Exceptions {
    
    public static class VersionNumberNotFoundException extends Exception {
        public VersionNumberNotFoundException() { super(); }
        public VersionNumberNotFoundException(String message) { super( message ); }
    }
    
    public static class SessionFileNotFoundException extends Exception {
        public SessionFileNotFoundException() { super(); }
        public SessionFileNotFoundException(String message) { super( message ); }
        
    }
    
    public static class ConfigFileNotFoundException extends Exception {
        public ConfigFileNotFoundException() { super(); }
        public ConfigFileNotFoundException(String message) { super( message ); }
        
    }
    
    public static class ConfigFileWrongSyntaxException extends Exception {
        public ConfigFileWrongSyntaxException() { super(); }
        public ConfigFileWrongSyntaxException(String message) { super( message ); }
        
    }
    
    public static class WrongCredentialsException extends Exception {
        public WrongCredentialsException() { super(); }
        public WrongCredentialsException(String message) { super( message ); }
    }
    
    public static class RefactorException extends Exception {
        public RefactorException() { super(); }
        public RefactorException(String message) { super( message ); }
    }
    public static class ImportException extends Exception {
        public ImportException() { super(); }
        public ImportException(String message) { super( message ); }
    }
    
    public static class NotInitializedException extends RuntimeException {
        public NotInitializedException() { super(); }
        public NotInitializedException(String message) { super( message ); }
    }
}
