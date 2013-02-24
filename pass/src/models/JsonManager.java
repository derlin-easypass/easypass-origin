package models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Exceptions;
import models.Exceptions.WrongCredentialsException;

import org.apache.commons.ssl.OpenSSL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


/**
 * this class provides utilities in order to encrypt/data data (openssl style,
 * pass and auto-generated salt, no iv) and to serialize/deserialize them (in a
 * json format).
 * 
 * it is also possible to write the content of a list in a cleartext "pretty"
 * valid json format.
 * 
 * @author Lucy Linder
 * @date Dec 21, 2012
 * 
 */
public class JsonManager {


	/**
	 * encrypts the arraylist of objects with the cipher given in parameter and
	 * serializes it in json format. Careful : the cipher must be correctly
	 * initialized for encryption
	 * 
	 * @param data
	 * @param cipher
	 *            the cipher previously initialized for encryption
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public void serialize(List<?> data, String algo, String filepath,
			String password) throws IOException {

		FileOutputStream fos = null;

		try {

			Gson gson = new GsonBuilder().create();
			fos = new FileOutputStream(filepath);
			fos.write(OpenSSL.encrypt(algo, password.toCharArray(), gson
					.toJson(data).getBytes("UTF-8")));
			fos.write("\r\n".getBytes());
			fos.write(System.getProperty("line.separator").getBytes());
			fos.flush();

		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				fos.close();
		}

	}// end serialize

	/**
	 * deserializes and returns the arrayList<Object[]> contained in the
	 * specified file. the decryption of the data is performed with the cipher
	 * given in parameter.
	 * 
	 * @param cipher
	 *            initialized for decryption and with the correct key
	 * @param filepath
	 * @return
	 * @throws CryptoException
	 *             If the cipher is not correctly initilialized (wrong algorithm
	 *             for example)
	 * @throws WrongCredentialsException
	 *             If the key used by the cipher is not the correct one
	 * @throws IOException
	 *             If a problem occurs while opening/reading the file
	 */
	public List<?> deserialize(String algo, String filepath,
			String password, Type type) throws WrongCredentialsException,
			IOException {

		FileInputStream fin = null;

		try {

			fin = new FileInputStream(filepath);
			List<?> data = (new GsonBuilder().create().fromJson(
					new InputStreamReader(OpenSSL.decrypt(algo,
							password.toCharArray(), fin), "UTF-8"), type));
			if (data == null) {
				throw new Exceptions.WrongCredentialsException();
			} else {
				return data;
			}

		} catch (IOException e) {
			throw new Exceptions.WrongCredentialsException(e.getMessage());
		} catch (JsonIOException e) {
			throw new Exceptions.WrongCredentialsException(e.getMessage());
		} catch (JsonSyntaxException e) {
			throw new Exceptions.WrongCredentialsException(e.getMessage());
		} catch (GeneralSecurityException e) {
			throw new Exceptions.WrongCredentialsException(e.getMessage());
		} finally {
			if (fin != null)
				fin.close();
		}// end try

	}// end deserialize


}// end class
