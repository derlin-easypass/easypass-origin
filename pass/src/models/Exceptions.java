package models;

public class Exceptions {
    
    public static class VersionNumberNotFoundException extends Exception {
        public VersionNumberNotFoundException() { super(); }
        public VersionNumberNotFoundException(String message) { super( message ); }
    }
    
    public static class IvNotFoundException extends Exception {
        public IvNotFoundException() { super(); }
        public IvNotFoundException(String message) { super( message ); }
        
    }
    
    public static class WrongCredentialsException extends Exception {
        public WrongCredentialsException() { super(); }
        public WrongCredentialsException(String message) { super( message ); }
    }
    
    public static class CryptoException extends Exception {
        public CryptoException() { super(); }
        public CryptoException(String message) { super( message ); }
    }
    
    public static class NotInitializedException extends RuntimeException {
        public NotInitializedException() { super(); }
        public NotInitializedException(String message) { super( message ); }
    }
}
