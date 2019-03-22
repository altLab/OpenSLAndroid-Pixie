package net.jpralves.slandroid;

import android.annotation.SuppressLint;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Library of methods to be used in other parts of the code
 *
 * @author Joao Alves
 * @version 1.0
 *
 */
public class Utils {

	private static final String TAG = Utils.class.getSimpleName();

	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 *
	 * @return The number of cores, or 1 if failed to get result
	 */
	// http://makingmoneywithandroid.com/forum/showthread.php?tid=298
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			if (BuildConfig.DEBUG)
				Log.d(TAG, "CPU Count: " + files.length);
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			if (BuildConfig.DEBUG)
				Log.d(TAG, "CPU Count: Failed.");
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * gets the android CPU Speed
	 *
	 * @return the speed in hz
	 */
	public static long getCPUSpeed() {
		// http://stackoverflow.com/questions/4940241/what-is-the-most-reliable-way-to-determine-the-cpu-clock-frequency-of-android-ph?rq=1
		String[] args = { "/system/bin/cat",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" };

		ProcessBuilder cmd = new ProcessBuilder(args);

		String result = "";
		Process process;
		try {
			process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
		}
		result = result.trim();
		if (result.equals("")) {
			result = "0";
		}
		return Long.parseLong(result);
	}

	/**
	 * gets the android CU usage
	 *
	 * @return the cpu usage in percentage
	 */
	public static float readUsage() {
		// http://stackoverflow.com/questions/3118234/how-to-get-memory-usage-and-cpu-usage-in-android
		float cpu = -1f;
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();

			String[] toks = load.split(" ");

			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			try {
				Thread.sleep(360);
			} catch (Exception e) {
			}

			reader.seek(0);
			load = reader.readLine();
			reader.close();
			toks = load.split(" ");

			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			if (idle1 >= 0 && cpu1 >= 0 && idle2 >= 0 && cpu2 >= 0) {
				if ((cpu2 + idle2) > (cpu1 + idle1) && cpu2 >= cpu1) {
					cpu = (cpu2 - cpu1) / (float) ((cpu2 + idle2) - (cpu1 + idle1));
					cpu *= 100.0f;
					if (BuildConfig.DEBUG)
						Log.d(TAG, "CPU Usage: " + cpu);
				}
			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
		}
		return cpu;
	}

	/**
	 * Determine the type of Mobile connection
	 *
	 * @param info
	 *            the network info to determine connection type.
	 * @return the type of mobile network we are on - string
	 */
	public static String getMobileType(NetworkInfo info) {
		if (info != null) {
			String type = info.getTypeName();

			if (type.toLowerCase(Locale.US).equals("mobile")) {
				type = info.getSubtypeName();

				if (type.toLowerCase(Locale.US).equals("gsm")
						|| type.toLowerCase(Locale.US).equals("gprs")
						|| type.toLowerCase(Locale.US).equals("edge")) {
					return "2G - " + type;
				} else if (type.toLowerCase(Locale.US).equals("cdma")
						|| type.toLowerCase(Locale.US).equals("umts")) {
					return "3G - " + type;
				} else if (type.toLowerCase(Locale.US).equals("lte")
						|| type.toLowerCase(Locale.US).equals("umb")) {
					return "4G - " + type;
				}
			}
		} else {
			return "";
		}
		return "";
	}

	/**
	 * formats a long value into a scaled value with unit
	 *
	 * @param bytes
	 *            the value to be formated
	 * @param si
	 *            base 10 or binary
	 * @return the string representation of bytes
	 */
	@SuppressLint("DefaultLocale")
	public static String formatMultibyte(long bytes, boolean si) {
		// http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String getDeviceModel() {
		if (android.os.Build.MODEL.contains(android.os.Build.MANUFACTURER + " "))
			return android.os.Build.MODEL;
		else
			return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
	}

	public static void CopyFile(String src, String dst) throws IOException {
		InputStream in = new FileInputStream(src);
		try {
			OutputStream out = new FileOutputStream(dst);
			try {
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
}
