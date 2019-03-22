package net.jpralves.slandroid;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//import lejos.pc.comm.NXTCommLogListener;
//import lejos.pc.comm.NXTConnector;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

/**
 * NXTControl Object.
 * 
 * <P>
 * Controls actions of the NXT.
 * 
 * @author Joao Alves
 * @version 1.0
 */
public class Control {

	private static final String TAG = Control.class.getSimpleName();

	private int speed = 100;

//	private ProcessBTInput bti;
//	private ProcessBTOutput bto;

	private int motorLPos = 0;
	private int motorRPos = 0;
	private int motorFlags = 0;
	private int initSpeed = 0;
	private int minDistance = 0;
	private int touchPos = 0;
	private int colorPos = 0;
	private int sonicleftPos = 0;
	private int sonicrightPos = 0;
	private int sonicfrontPos = 0;

//	private Connector nxtConn = null;

	private String nxtName = "NXT";

	private int motorLeft = 0;
	private int motorRight = 1;

	private ControllerApp app;

	/**
	 * @return true if connected
	 */
	public boolean isConnected() {
		return false; //nxtConn != null;
	}

	/**
	 * Sets the environment of the NXT
	 * 
	 * @param nxtName
	 *            NXT Name
	 * @param motorLPos
	 *            Port of Left Motor
	 * @param motorRPos
	 *            Port of Right Motor
	 * @param motorFlags
	 *            Motor Flags
	 * @param initSpeed
	 *            Initial Speed
	 * @param minDistance
	 *            minimum reaction distance
	 * @param touchPos
	 *            Port of touch sensor
	 * @param colorPos
	 *            Port of color sensor
	 * @param sonicleftPos
	 *            Port of sonic-left sensor
	 * @param sonicrightPos
	 *            Port of sonic-right sensor
	 * @param sonicrightPos
	 *            Port of sonic-front sensor
	 * @param app
	 *            reference to NXTControllerApp
	 */
	public Control(String nxtName, int motorLPos, int motorRPos, int motorFlags, int initSpeed,
			int minDistance, int touchPos, int colorPos, int sonicleftPos, int sonicrightPos,
			int sonicfrontPos, ControllerApp app) {
		this.nxtName = nxtName;
		this.app = app;
		this.motorLPos = motorLPos;
		this.motorRPos = motorRPos;
		this.motorFlags = motorFlags;
		this.initSpeed = initSpeed;
		this.minDistance = minDistance;
		this.touchPos = touchPos;
		this.colorPos = colorPos;
		this.sonicleftPos = sonicleftPos;
		this.sonicrightPos = sonicrightPos;
		this.sonicfrontPos = sonicfrontPos;
	}

	/**
	 * Closes connection with NXT
	 */
	public void close() {
		if (isConnected()) {
/*			try {
				nxtConn.close();
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}*/
		}
	}


	public int getMotorLeft() {
		return motorLeft;
	}

	public int getMotorRight() {
		return motorRight;
	}

	public String getNXTName() {
		return nxtName;
	}

	/**
	 * Connects the android to NXT
	 * 
	 * @return the NXTConnector Object
	 */
	private SimpleBluetoothDeviceInterface connect() {

		Log.d(TAG, "about to add LEJOS listener");
//		DataOutputStream dos = new DataOutputStream();
//		DataInputStream dis = new DataInputStream();
//		SimpleBluetoothDeviceInterface conn = new SimpleBluetoothDeviceInterface(new BluetoothSerialDevice("", dos, dis)));
/*		Connector conn = new Connector();
		if (conn != null) {
			conn.setDebug(true);
			conn.addLogListener(new NXTCommLogListener() {
				public void logEvent(String arg0) {
					if (BuildConfig.DEBUG)
						Log.e(TAG, "NXJ log: " + arg0);
				}

				public void logEvent(Throwable arg0) {
					if (BuildConfig.DEBUG)
						Log.e(TAG, "NXJ log:" + arg0.getMessage(), arg0);
				}
			});
			if (!conn.connectTo("btspp://")) {
				return null;
			}
		}
		*/
		return null;
	}

	/**
	 * rotates NXT to left
	 */
	public void moveLeft() {
		if (isConnected()) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "moveLeft");
			// bto.primitiveStartLeft();
			// sendMessage(1,0);
		}
	}

	/**
	 * rotates NXT to right
	 */
	public void moveRight() {
		if (isConnected()) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "moveRight");
			//bto.primitiveStartRight();
			// sendMessage(0,1);
		}
	}

	/**
	 * stops both motors
	 */
	public void stopMotors() {
		if (isConnected()) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "stop");
			//bto.primitiveStop();
			// sendMessage(0,0);
		}
	}

	/**
	 * Moves both motors forward
	 */
	public void moveForward() {
		if (isConnected()) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "moveForward");

			//bto.primitiveForward();
			// sendMessage(1,1);
		}
	}

	/**
	 * changes speed of NXT
	 * 
	 * @param speed
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	private void moveNXT(int xaxis, int yaxis) {
		int cx = xaxis / 2;
		int cy = yaxis / 2;

		int vl, vr;

		if (cx > 0) {
			if (cy > 0) {
				vl = speed;
				vr = (int) (speed * Math.sin(Math.atan2(cy, cx)));
			} else if (cy == 0) {
				vl = speed;
				vr = 0;
			} else {
				vl = -speed;
				vr = (int) (speed * Math.sin(Math.atan2(cy, cx)));
			}

		} else if (cx == 0) {
			if (cy > 0) {
				vl = speed;
				vr = speed;
			} else if (cy == 0) {
				vl = 0;
				vr = 0;
			} else {
				vl = -speed;
				vr = -speed;
			}

		} else {
			if (cy > 0) {
				vl = (int) (speed * Math.sin(Math.atan2(cy, cx)));
				vr = speed;
			} else if (cy == 0) {
				vl = 0;
				vr = speed;
			} else {
				vl = (int) (speed * Math.sin(Math.atan2(cy, cx)));
				vr = -speed;
			}
		}
		vl = -vl;
		vr = -vr;

		if (isConnected()) {
			// try {
			// //TextView tv = (TextView) findViewById(R.id.textInfo);
			// //tv.setText("racio=" + racio + ",vl=" + vl + ", vr=" + vr);
			// Log.d(TAG, "vl=" + vl + ", vr=" + vr);
			//
			// nxtCommand.setOutputState(getMotorLeft(), (byte) vl,
			// getNXTmode(), 0, 0, 0, 0);
			// nxtCommand.setOutputState(getMotorRight(), (byte) vr,
			// getNXTmode(), 0, 0, 0, 0);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}

	}

	/**
	 * Connects to NXT setting input and output channels
	 * 
	 * @return true if successfull
	 */
	public boolean connectToNXT() {
		/*
		setupNXJCache();
		nxtConn = connect();
		if (nxtConn != null) {
			DataOutputStream dos = new DataOutputStream(nxtConn.getOutputStream());
			DataInputStream dis = new DataInputStream(nxtConn.getInputStream());
			bti = new ProcessBTInput(app, dis);
			bto = new ProcessBTOutput(app, dos);

			bto.setupNXT(motorLPos, motorRPos, motorFlags, initSpeed, minDistance, touchPos,
					colorPos, sonicleftPos, sonicrightPos, sonicfrontPos);
			// stopMotors();
			// bto.sendSpeed(speed);
			return true;
		} else {
			return false;
		}
		*/
		return false;
	}

	/**
	 * setups the NXJ Cache
	 */
	private void setupNXJCache() {

		File root = Environment.getExternalStorageDirectory();

		try {
			String androidCacheFile = "nxj.cache";
			File mLeJOS_dir = new File(root + "/leJOS");
			if (!mLeJOS_dir.exists()) {
				mLeJOS_dir.mkdir();
			}
			File mCacheFile = new File(root + "/leJOS/", androidCacheFile);

			if (root.canWrite() && !mCacheFile.exists()) {
				FileWriter gpxwriter = new FileWriter(mCacheFile);
				BufferedWriter out = new BufferedWriter(gpxwriter);
				out.write("");
				out.flush();
				out.close();
				if (BuildConfig.DEBUG)
					Log.d(TAG, "nxj.cache (record of connection addresses) written to: "
							+ mCacheFile.getName());
			} else {
				if (BuildConfig.DEBUG)
					Log.d(TAG,
							"nxj.cache file not written as"
									+ (!root.canWrite() ? mCacheFile.getName()
											+ " can't be written to sdcard."
											: " cache already exists."));

			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "Could not write nxj.cache " + e.getMessage(), e);
		}
	}

}
