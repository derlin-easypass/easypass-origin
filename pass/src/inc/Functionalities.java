package inc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    
    
    
	public static void writeLog(String message, String logFilePath) {

		try {

			File file = new File(logFilePath);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			FileWriter fileWritter = new FileWriter(file, true);
			BufferedWriter buffer = new BufferedWriter(fileWritter);
			buffer.write(message + System.getProperty("line.separator"));
			buffer.flush();
			buffer.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
