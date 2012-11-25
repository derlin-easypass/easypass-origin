package inc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

import models.*;

public class Functionalities {

	/**
	 * Saves the iv (a limited array of bytes generated during encryption) into
	 * the specified file
	 * 
	 * @param iv
	 * @param path
	 * @throws IOException
	 */
	public static void saveIv(byte[] iv, String path) throws IOException {

		FileOutputStream fos = new FileOutputStream(path);
		fos.write(iv);
		fos.close();

	}// end saveIV

	
	public static String[] getAvailableSessions(String pathToFolder, String pattern){

		File folder = new File(pathToFolder);
		
		File[] listOfFiles = folder.listFiles(new Filter(pattern));
		String[] sessions = new String[listOfFiles.length];
		int counter = 0;
		    for (File f : listOfFiles) {
		    	String name =  f.getName();
		        sessions[counter] = name.substring(0, name.lastIndexOf('.'));
		        System.out.println(sessions[counter]);
		        counter++;
		    }
		
		return sessions;
	}//end getAvailableSessions
	
	
	/**
	 * checks if a file corresponding to the given session name exists.
	 * @param pathToFolder
	 * @param name
	 * @return
	 */
	public static boolean sessionExists(String pathToFolder, String name){

		File folder = new File(pathToFolder);
		
		return (folder.listFiles(new Filter(name + "\\.(data|iv)_ser$")).length > 0 ? true : false);

	}//end getAvailableSessions

	
	/**
	 * reads and return the iv (a limited array of bytes used for decryption)
	 * from the specified file
	 * 
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

	/**
	 * appends a message to a [log] file.
	 * @param message  the message to write
	 * @param logFilePath  the path to the file
	 */
	public static void writeLog(String message, String logFilePath) {

		try {

			File file = new File(logFilePath);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

			//adds the date and the message at the end of the file
			writer.newLine();
			writer.write(new Date().toString() + " "
					+ message); // + System.getProperty("line.separator"));
			writer.flush();
			writer.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}//end writeLog
	
	
	/**
	 * private class used to get files in a folder that match a pattern
	 * @author lucy
	 */
	public static class Filter implements FilenameFilter {

		protected String pattern;

		public Filter(String str) {
			pattern = str;
		}

		public boolean accept(File dir, String name) {
			return name.toLowerCase().matches(pattern);
		}
	}
}
