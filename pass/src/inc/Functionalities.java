package inc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import models.*;

public class Functionalities {

    /**
     * Saves the iv (a limited array of bytes generated during encryption) into the specified file
     * @param iv
     * @param path
     * @throws IOException
     */
    public static void saveIv(byte[] iv, String path) throws IOException {

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(iv);
        fos.close();

    }// end saveIV

    /**
     * reads and return the iv (a limited array of bytes used for decryption) from the specified file
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] readIv(String path) throws IOException {

        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);

        byte[] iv = new byte[(int) file.length()];

        fis.read(iv);
        fis.close();

        return iv;

    }// end saveIV
}
