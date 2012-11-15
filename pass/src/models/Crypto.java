package models;
import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;


public class Crypto {

    private String keyFactAlgo; //the algorithm to use, for example PBKDF2WithHmacSHA1
    private String cipherTransfo; // the transformation to do, for example AES/CBC/PKCS5Padding
    private String encryptionType; // the type of the encryptionk, for example AES
    private int keyIterationCount; // the number of iterations during the encryption, for example 65536
    private int keyLength; //the length of the key to generate, for example 64 or 128
    private char[] pass; // the password for the key
    private byte[] salt; // the salt for the key
    private Cipher cipher; //the cipher object
    private SecretKey secret; // the generated key using the pass, salt, keylength and iterationCount parameters
    
    
    public static void main(String[]args){
        
        try {
            Crypto c = new Crypto(
                    "PBKDF2WithHmacSHA1",
                    "AES/CBC/PKCS5Padding",
                    "AES",
                     65536, 128,
                    "my_pass",
                    "my_salt"
                    );
            
            byte[][] cryptAndIv = c.encrypt("mon essai trop cool @@");
            byte[] encrypted = cryptAndIv[0];
            byte[] iv = cryptAndIv[1];
            
            System.out.println(c.decrypt(encrypted, iv));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }//end main
    
    public Crypto(
            String keyFactAlgo,
            String cipherTransfo, 
            String encryptionType, 
            int keyIterationCount,
            int keyLength,
            String pass,
            String salt
            ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        
        this.keyFactAlgo = keyFactAlgo;
        this.cipherTransfo = cipherTransfo;
        this.encryptionType = encryptionType;
        this.keyIterationCount = keyIterationCount;
        this.keyLength = keyLength;
        
        this.cipher = Cipher.getInstance(this.cipherTransfo);
        
        //creates an array of chars from the string password given as a parameter
        this.pass = new char[pass.length()];
        pass.getChars(0, pass.length(), this.pass, 0);
        
        //gets an array of bytes from the given string salt
        this.salt = salt.getBytes();
        
        //creates the key
        SecretKeyFactory factory = SecretKeyFactory.getInstance(keyFactAlgo);          
        this.secret = new SecretKeySpec(
                
                ( factory.generateSecret( 
                        new PBEKeySpec(this.pass, this.salt, this.keyIterationCount, this.keyLength) 
                    ) 
                ).getEncoded(), 
                encryptionType
        );   
    }//end constructor
    
    
    /**
     * this method encrypts a String. 
     * @param toEncrypt
     * @return a 2-dimensionnel array of bytes. the first dimension is the encrypted array of bytes, the second
     * the iv used for encryption (you need it in order to decipher the String later)
     */
    public byte[][] encrypt(String toEncrypt){
        
        byte[][] cipherAndIv = new byte[2][];
        
        try {
            
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
            cipherAndIv[0] = this.cipher.doFinal(toEncrypt.getBytes("UTF-8"));
            cipherAndIv[1] = this.cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
        
        return cipherAndIv;
        
    }//end encrypt
    
    /**
     * this method decrypts an array of bytes encrypted with the encrypt method.
     *  
     * @param todecipher the array of bytes to decipher
     * @param iv   the iv used to encipher the datas
     * @return
     */
    public String decrypt(byte[] todecipher, byte[] iv){
        
        try{           
            this.cipher.init(Cipher.DECRYPT_MODE, this.secret, new IvParameterSpec(iv));
            
            byte[] decrypted = cipher.doFinal(todecipher);
            return (new String(decrypted, "UTF-8"));
            
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }//end decrypt
    
    /**
     * This method encrypts and then serialize an object using the the parameters used to create the 
     * crypto instance.
     * 
     * @param filepath the path to the file storing the serialized object
     * @param obj   the object to serialize
     * @return the iv used to encryp datas
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws IOException
     * @throws InvalidParameterSpecException
     */
    public byte[] serializeObject(String filepath, Serializable obj) 
            throws InvalidKeyException, IllegalBlockSizeException, IOException, InvalidParameterSpecException{
        
        this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);           

        //created an encrypted SealedObject
        SealedObject so = new SealedObject(obj, this.cipher);
        
        // open streams
        FileOutputStream fileOut = new FileOutputStream(filepath);       
        ObjectOutputStream oos = new ObjectOutputStream(fileOut);
        
        //serialize the sealedObject
        oos.writeObject(so);
        oos.flush();
        
        //close streams
        oos.close();
        fileOut.close();
        
        return this.cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        
    }//end serializeObject

    /**
     * This method decrypts and deserialize an object serialized using the serializeObject method.
     * To get back the original object, you need the correct key (passed in the constructor) and 
     * the iv that the serializeObject returned.
     * @param filepath  the file storing the serialized object
     * @param iv  the iv used for the encryption
     * @return  the deserialized object
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws ClassNotFoundException
     */
    public Object deserializeObject(String filepath, byte[] iv) 
            throws IOException, IllegalBlockSizeException, 
            BadPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, ClassNotFoundException{
        
        Object obj = null;
        
        //initialize the cypher to decrypt mode with the correct iv
        this.cipher.init(Cipher.DECRYPT_MODE, this.secret, new IvParameterSpec(iv));        
           
        // open streams to read from the file 
        FileInputStream fileIn = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fileIn);

        //deserialize the sealedobject
        SealedObject sealedObj = (SealedObject) ois.readObject();
        ois.close();
        
        //get the original object by decrypting the sealed one
        obj = sealedObj.getObject(this.cipher);

        return obj;
        
    }//end deserializeObject
    
    

    public String getKeyFactAlgo() {
        return keyFactAlgo;
    }

    public void setKeyFactAlgo(String keyFactAlgo) {
        this.keyFactAlgo = keyFactAlgo;
    }

    public String getCipherTransfo() {
        return cipherTransfo;
    }

    public void setCipherTransfo(String cipherTransfo) {
        this.cipherTransfo = cipherTransfo;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public int getKeyIterationCount() {
        return keyIterationCount;
    }

    public void setKeyIterationCount(int keyIterationCount) {
        this.keyIterationCount = keyIterationCount;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void initCipherForEncryption() throws InvalidKeyException {
        
           this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
    }  
    
    
    public void initCipherForDecryption(byte[] iv) throws InvalidAlgorithmParameterException, InvalidKeyException { 
            this.cipher.init(Cipher.DECRYPT_MODE, this.secret, new IvParameterSpec(iv));
    }
    
    public void initCipherForDecryption() throws InvalidAlgorithmParameterException, InvalidKeyException { 
        this.cipher.init(Cipher.DECRYPT_MODE, this.secret);
    }
    
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }
    
}//end class





/* public static void main(String[]args){
    
    char[] pass = {'m', 'y', 'p', 'a', 's', 's'};
    String salty = "mysalt";
    String essai ="riporiop!.";
    

    try {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(pass, salty.getBytes(), 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);

        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(essai.getBytes("UTF-8"));
        System.out.println(ciphertext.toString() + "\n" + new String(iv, "UTF8"));
//        // reinit cypher using param spec
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
  
        decrypt(ciphertext, iv);
//        byte[] decrypted = cipher.doFinal(ciphertext);
//        System.out.println(new String(decrypted, "UTF8"));
        
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
    }

}*/


