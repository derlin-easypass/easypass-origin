package main.thread;

/**
 * User: lucy
 * Date: 03/03/13
 * Version: 0.1
 */
public class PassLock {
    
    public static enum Message {DO_CLOSE, DO_OPEN_SESSION}
    
    protected Message message;

    public synchronized Message getMessage() {
        return this.message;
    }//end getMessage


    public synchronized void setMessage( Message message ) {
        this.message = message;
    }//end setMessage
}//end class
