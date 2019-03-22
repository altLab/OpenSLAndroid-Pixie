package net.jpralves.slandroid;

import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Networking Object.
 *
 * <P>
 * Obtain information about the currently used ipv4 address
 *
 * @author Joao Alves
 * @version 1.0
 */
public class Networking {

	private static final String TAG = Networking.class.getSimpleName();

	public static String getLocalIpAddressString() {
		InetAddress address = getLocalIpAddress();
		String ipv4 = "";
		if (address != null) {
			ipv4 = getLocalIpAddress().getHostAddress();
		}
		return ipv4;
	}

	/**
	 * returns the local ipv4 address other than loopback interface
	 *
	 * @return the local ipv4 address
	 */
	public static InetAddress getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
						if (BuildConfig.DEBUG)
							Log.i(TAG, "Local IP Address="
									+ inetAddress.getHostAddress());
						if (BuildConfig.DEBUG)
							Log.i(TAG, "Private IP Addressing? " + inetAddress.isSiteLocalAddress());
						return inetAddress;
					}
				}
			}
		} catch (SocketException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}

}
