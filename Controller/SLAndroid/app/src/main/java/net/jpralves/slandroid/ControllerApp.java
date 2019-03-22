package net.jpralves.slandroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Application
 * 
 * @author Joao Alves
 * @version 1.0
 * 
 */
public class ControllerApp extends Application {

	private static final String TAG = ControllerApp.class.getSimpleName();

	private static final String CAPTUREFOLDERNAME = "ControllerCapture";

	private SharedPreferences prefs;

	private byte[] lastImageBA = null;
	private HashMap<String, String> hashString = new HashMap<String, String>();
	private HashMap<String, Float> hashFloat = new HashMap<String, Float>();
	private HashMap<String, Integer> hashInt = new HashMap<String, Integer>();
	private HashMap<String, Long> hashLong = new HashMap<String, Long>();

	private static int theBufferSize = 10 * 1024;

	private String imagePath = "";

	public ControllerApp() {
	}

	/**
	 * gets a value from the table
	 * <p>
	 * tries to locate the key in the hashmaps stored
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public String getTableValue(String key) {
		if (hashString.containsKey(key))
			return hashString.get(key);
		else if (hashInt.containsKey(key))
			return "" + hashInt.get(key);
		else if (hashLong.containsKey(key))
			return "" + hashLong.get(key);
		else if (hashFloat.containsKey(key))
			return "" + hashFloat.get(key);
		else
			return null;
	}

	/**
	 * empties a table entry
	 * 
	 * @param key
	 *            the table entry to clear
	 */
	public void clearTableEntry(String key) {
        hashString.remove(key);
        hashFloat.remove(key);
        hashInt.remove(key);
        hashLong.remove(key);
	}

	/**
	 * returns all keys from table strings
	 * 
	 * @return keys
	 */
	public Iterator<String> getTableStringKeys() {
		ArrayList<String> mylist = new ArrayList<String>(hashString.keySet());
		Collections.sort(mylist);
		return mylist.iterator();
	}

	/**
	 * gets a value from the string table
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public String getTableString(String key) {
		if (hashString.containsKey(key))
			return hashString.get(key);
		else
			return null;
	}

	/**
	 * sets a value in the string table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setTableString(String key, String value) {
		if (value != null)
			this.hashString.put(key, value);
	}

	/**
	 * returns all keys from float table
	 * 
	 * @return keys
	 */
	public Iterator<String> getTableFloatKeys() {
		ArrayList<String> mylist = new ArrayList<String>(hashFloat.keySet());
		Collections.sort(mylist);
		return mylist.iterator();
	}

	/**
	 * gets a value from the float table
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public float getTableFloat(String key) {
		if (hashFloat.containsKey(key))
			return hashFloat.get(key);
		else
			return 0.0f;
	}

	/**
	 * sets a value in the float table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setTableFloat(String key, float value) {
		this.hashFloat.put(key, value);
	}

	/**
	 * returns all keys from int table
	 * 
	 * @return keys
	 */
	public Iterator<String> getTableIntKeys() {
		ArrayList<String> mylist = new ArrayList<String>(hashInt.keySet());
		Collections.sort(mylist);
		return mylist.iterator();
	}

	/**
	 * gets a value from the int table
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public int getTableInt(String key) {
		if (hashInt.containsKey(key))
			return hashInt.get(key);
		else
			return 0;
	}

	/**
	 * sets a value in the int table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setTableInt(String key, int value) {
		this.hashInt.put(key, value);
	}

	/**
	 * adds a value in the int table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be added
	 */
	public void addTableInt(String key, int value) {

		this.hashInt.put(key, getTableInt(key) + value);
	}

	/**
	 * returns all keys from long table
	 * 
	 * @return keys
	 */
	public Iterator<String> getTableLongKeys() {
		ArrayList<String> mylist = new ArrayList<String>(hashLong.keySet());
		Collections.sort(mylist);
		return mylist.iterator();
	}

	/**
	 * gets a value from the long table
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public long getTableLong(String key) {
		if (hashLong.containsKey(key))
			return hashLong.get(key);
		else
			return 0;
	}

	/**
	 * sets a value in the long table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setTableLong(String key, long value) {
		this.hashLong.put(key, value);
	}

	/**
	 * adds a value in the long table
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be added
	 */
	public void addTableLong(String key, long value) {

		this.hashLong.put(key, getTableLong(key) + value);
	}

	/**
	 * returns the last captured image
	 * 
	 * @return the lastImage
	 */
	public synchronized ByteArrayInputStream getLastImage() {
		return new ByteArrayInputStream(lastImageBA);
	}

	/**
	 * stores the last captured image
	 * 
	 * @param lastImage
	 *            the lastImage to set
	 */
	public synchronized void setLastImage(ByteArrayOutputStream lastImage) {

		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(lastImage.toByteArray());
		int numberBytes = bais.available();
		lastImageBA = new byte[numberBytes];
		if (numberBytes > 0) {
			bais.read(lastImageBA, 0, numberBytes);
		}

		if (prefs.getBoolean("isVideoCaptureEnabled", true))
			saveImageToFile(new ByteArrayInputStream(lastImageBA));
	}

	@SuppressLint("SimpleDateFormat")
	protected String getImagePathName() {
		String res = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String currentDateandTime = sdf.format(new Date());

		res = getImagePath() + File.separator + currentDateandTime + ".jpg";
		return res;
	}

	@SuppressLint("SimpleDateFormat")
	protected String getImagePath() {
		if (imagePath.isEmpty()) {
			String res = "";

			if (!Environment.getExternalStorageDirectory().canWrite()) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, "Write Denied to "
							+ Environment.getExternalStorageDirectory().getAbsolutePath());
				// set alternative:
				// TODO document better this fix - HTC related
				res = "/mnt/emmc" + File.separator;
			} else {
				res = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
			}
			res += CAPTUREFOLDERNAME;
			SimpleDateFormat sdfpath = new SimpleDateFormat("yyyyMMdd");
			String pathDate = sdfpath.format(new Date());
			res += File.separator + pathDate + "-" + android.os.Process.myPid();
			imagePath = res;
		}
		return imagePath;
	}

	protected void saveImageToFile(InputStream bais) {
		File dir = new File(getImagePath());
		Boolean res = true;
		if (!dir.isDirectory()) {
			res = dir.mkdirs();
		}
		if (res) {
			File file = new File(getImagePathName());

			try {
				file.createNewFile();
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}

			if (file.exists()) {
				OutputStream fo;
				try {
					int pending = bais.available();
					bais.mark(pending);
					if (pending > 0) {
						fo = new FileOutputStream(file);
						byte[] buff = new byte[theBufferSize];
						while (pending > 0) {
							int read = bais.read(buff, 0,
									((pending > theBufferSize) ? theBufferSize : pending));
							if (read <= 0)
								break;
							fo.write(buff, 0, read);
							pending -= read;
						}
						bais.reset();
						fo.flush();
						fo.close();
					} else {
						if (BuildConfig.DEBUG)
							Log.d(TAG, "Zero byte file");
					}
				} catch (IOException e) {
					if (BuildConfig.DEBUG)
						Log.e(TAG, e.getMessage(), e);
				}

			}
		}
	}

}
