package net.jpralves.slandroid;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * SensorService Object.
 *
 * <P>
 * Service responsible by getting values from all sensors.
 *
 * @author Joao Alves
 * @version 1.0
 */
public class SensorService extends Service {

	private static final String TAG = SensorService.class.getSimpleName();

	private BroadcastReceiver broadcastReceiver;
	private SensorManager mSensorManager;
	private TelephonyManager telephonyManager;
	private PhoneStateListener cellLocationListener;

	private ControllerApp app;

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Service Created.");
		app = (ControllerApp) getApplication();

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

				float levelcalc = (float) (level * 100 / scale);

				app.setTableInt("android.battery", Math.round(levelcalc));

				ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				Boolean isWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting();

				if (isWifi) {
					WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					app.setTableString("android.wifi.bssid", wifiInfo.getBSSID());
					app.setTableInt("android.wifi.signal.rssi", wifiInfo.getRssi());
					app.setTableInt("android.wifi.signal.percent",
							normalizeSignalRssi(wifiInfo.getRssi()));
					app.setTableInt("android.wifi.linkspeed", wifiInfo.getLinkSpeed());
				} else {
					app.clearTableEntry("android.wifi.bssid");
					app.clearTableEntry("android.wifi.signal");
					app.clearTableEntry("android.wifi.linkspeed");
				}
			}
		};

		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		cellLocationListener = new PhoneStateListener() {
			public void onCellLocationChanged(CellLocation location) {

				// NXTControllerApp app = (NXTControllerApp) getApplication();
				app.setTableString("android.phone.celllocation", location.toString());
				super.onCellLocationChanged(location);
			};

			public void onSignalStrengthsChanged(final SignalStrength signal) {

				// NXTControllerApp app = (NXTControllerApp) getApplication();
				int asu = signal.getGsmSignalStrength();
				if (asu == 99)
					asu = 0;
				int dBm = -113 + 2 * asu;
				app.setTableInt("android.phone.signal.asu", asu);
				app.setTableInt("android.phone.signal.dbm", dBm);
				app.setTableInt("android.phone.signal.percent", normalizeSignaldBm(dBm));

				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				app.setTableString("android.phone.nettype",
						Utils.getMobileType(cm.getActiveNetworkInfo()));

				super.onSignalStrengthsChanged(signal);
			};

			public void onServiceStateChanged(ServiceState serviceState) {
				if (serviceState.getState() == ServiceState.STATE_POWER_OFF) {
					ControllerApp app = (ControllerApp) getApplication();
					app.clearTableEntry("android.phone.signal.asu");
					app.clearTableEntry("android.phone.signal.dbm");
					app.clearTableEntry("android.phone.signal.percent");
					app.clearTableEntry("android.phone.celllocation");
					app.clearTableEntry("android.phone.nettype");
				}
			}
		};

		telephonyManager.listen(cellLocationListener, PhoneStateListener.LISTEN_CELL_LOCATION
				| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				| PhoneStateListener.LISTEN_SERVICE_STATE);
		// LISTEN_CELL_LOCATION
		// onCellLocationChanged (CellLocation location)
		// LISTEN_SIGNAL_STRENGTHS
		// onSignalStrengthsChanged

		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.getApplicationContext().registerReceiver(broadcastReceiver, filter);

		mSensorManager = (SensorManager) getApplicationContext().getSystemService(
				Context.SENSOR_SERVICE);
		List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for (Sensor s : sensorList) {
			mSensorManager.registerListener(mSensor, s, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	public void onDestroy() {
		this.getApplicationContext().unregisterReceiver(broadcastReceiver);
		mSensorManager.unregisterListener(mSensor);
		telephonyManager.listen(cellLocationListener, 0);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Service Destroyed.");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Received start id = " + startId + ": " + intent);
		// super.onStart(intent, startId);
		if (intent != null) {
			intent.setAction("Started");
			Toast.makeText(this, getString(R.string.sensorservice_started), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return START_STICKY;
	}

	/**
	 * Normalizes the rssi signal to a percentage value
	 *
	 * @param rssi
	 * @return percentage value
	 */
	private int normalizeSignalRssi(int rssi) {
		int MAX_RSSI = -55;
		int MIN_RSSI = -100;
		int numLevels = 100;

		if (rssi < MIN_RSSI)
			rssi = MIN_RSSI;
		if (rssi > MAX_RSSI)
			rssi = MAX_RSSI;

		float level = ((float) (rssi - MIN_RSSI)) / (MAX_RSSI - MIN_RSSI);
		return (int) (level * numLevels);
	}

	private int normalizeSignaldBm(int dbm) {
		int MAX_DBM = -53;
		int MIN_DBM = -113;
		int numLevels = 100;

		if (dbm < MIN_DBM)
			dbm = MIN_DBM;
		if (dbm > MAX_DBM)
			dbm = MAX_DBM;

		float level = ((float) (dbm - MIN_DBM)) / (MAX_DBM - MIN_DBM);
		return (int) (level * numLevels);
	}

	private final SensorEventListener mSensor = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@SuppressWarnings("deprecation")
		public void onSensorChanged(SensorEvent event) {

			Sensor source = event.sensor;
			// NXTControllerApp app = (NXTControllerApp) getApplication();

			/*
			 * Log.d(TAG, "Sensor: " + source.getName() + " - v0=" +
			 * event.values[0] + ", v1=" + event.values[1] + ", v2=" +
			 * event.values[2]);
			 */
			switch (source.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				app.setTableFloat("android.sensor.accel.x", event.values[0]);
				app.setTableFloat("android.sensor.accel.y", event.values[1]);
				app.setTableFloat("android.sensor.accel.z", event.values[2]);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				app.setTableFloat("android.sensor.magnet.x", event.values[0]);
				app.setTableFloat("android.sensor.magnet.y", event.values[1]);
				app.setTableFloat("android.sensor.magnet.z", event.values[2]);
				break;
			case Sensor.TYPE_GYROSCOPE:
				app.setTableFloat("android.sensor.gyro.x", event.values[0]);
				app.setTableFloat("android.sensor.gyro.y", event.values[1]);
				app.setTableFloat("android.sensor.gyro.z", event.values[2]);
				break;
			case Sensor.TYPE_GRAVITY:
				app.setTableFloat("android.sensor.grav.x", event.values[0]);
				app.setTableFloat("android.sensor.grav.y", event.values[1]);
				app.setTableFloat("android.sensor.grav.z", event.values[2]);
				break;
			case Sensor.TYPE_LIGHT:
				app.setTableFloat("android.sensor.light", event.values[0]);
				break;
			case Sensor.TYPE_TEMPERATURE:
				app.setTableFloat("android.sensor.temp", event.values[0]);
				break;
			case Sensor.TYPE_ORIENTATION:
				app.setTableFloat("android.sensor.orient.azim", event.values[0]);
				app.setTableFloat("android.sensor.orient.pitch", event.values[1]);
				app.setTableFloat("android.sensor.orient.roll", event.values[2]);
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				app.setTableFloat("android.sensor.laccel.x", event.values[0]);
				app.setTableFloat("android.sensor.laccel.y", event.values[1]);
				app.setTableFloat("android.sensor.laccel.z", event.values[2]);
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				app.setTableFloat("android.sensor.rotation.x", event.values[0]);
				app.setTableFloat("android.sensor.rotation.y", event.values[1]);
				app.setTableFloat("android.sensor.rotation.z", event.values[2]);
				break;
			case Sensor.TYPE_PROXIMITY:
				app.setTableFloat("android.sensor.proxymity", event.values[0]);
				break;
			default:
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Sensor not registered - " + source.getName());
			}
		}
	};

}
