package net.jpralves.slandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

//import lejos.pc.comm.NXTCommLogListener;
//import lejos.pc.comm.NXTConnector;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

/**
 * NXTControl Object.
 *
 * <p>
 * Controls actions of the NXT.
 *
 * @author Joao Alves
 * @version 1.0
 */
public class Control {

    private static final String TAG = Control.class.getSimpleName();

    private BluetoothSocket btSocket;
    BufferedWriter bto;
    BufferedReader bti;


    private String btName = "HC-06";

    private int motorLeft = 0;
    private int motorRight = 1;

    private ControllerApp app;

    private BluetoothDevice mybtDevice = null;

    private boolean okSate = true;

    /**
     * @return true if connected
     */
    public boolean isConnected() {
        return btSocket != null;
    }

    /**
     * Sets the environment of the NXT
     *
     * @param btName        NXT Name
     * @param app           reference to NXTControllerApp
     */
    public Control(String btName, ControllerApp app) {
        this.btName = btName;
        this.app = app;
    }

    /**
     * Closes connection with NXT
     */
    public void close() {
        if (isConnected()) {
            try {
                bti.close();
                bto.close();
                btSocket.close();
            } catch (IOException e) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, e.getMessage(), e);
            }
        }
    }


//	public int getMotorLeft() {
//		return motorLeft;
//	}
//
//	public int getMotorRight() {
//		return motorRight;
//	}

    public String getBTName() {
        return btName;
    }

    /**
     * Connects the android to NXT
     *
     * @return the NXTConnector Object
     */
//	private SimpleBluetoothDeviceInterface connect() {


//		Log.d(TAG, "about to add LEJOS listener");
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
//		return null;
//	}

    /**
     * rotates NXT to left
     */

    public void sendCommand(String command) {
        if (isConnected() && okSate) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "moveLeft");

            try {
                okSate = false;
                bto.write(command);
                bto.write('\n');
                bto.flush();
                String line;
                do {
                    line = getCommand();
                    //TODO: Register Message in Global Variables
                    Log.d(TAG,"Received BT:" + line);
                } while (!line.equals("OK"));
                okSate = true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                // TODO: Close?
            }
        }
    }


    public void print(int layer) {
        sendCommand("LAYR"+String.valueOf(layer));
    }

    public String getCommand() {
        if (isConnected()) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "getCommand");

            try {
                String output = bti.readLine();
                return output;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                // TODO: Close?
            }
        }
        return null;
    }


    /**
     * Connects to NXT setting input and output channels
     *
     * @return true if successfull
     */
    public boolean connectToBT() {
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt != null) {

            for (BluetoothDevice device : bt.getBondedDevices()) {
                Log.d(TAG, "\tDevice Name: " + device.getName());
                Log.d(TAG, "\tDevice MAC: " + device.getAddress());
                if (device.getName().equals(btName)) {
                    mybtDevice = device;
                    break;
                }
            }


            if (mybtDevice != null) {
                try {
                    btSocket = mybtDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                } catch (IOException ex) {
                    Log.d(TAG, "Failed to create RfComm socket: " + ex.toString());
                    return false;
                }

                for (int i = 0; ; i++) {
                    try {
                        btSocket.connect();
                    } catch (IOException ex) {
                        if (i < 5) {
                            Log.d(TAG, "Failed to connect. Retrying: " + ex.toString());
                            continue;
                        }

                        Log.d(TAG, "Failed to connect: " + ex.toString());
                        return false;
                    }
                    break;
                }
                Log.d(TAG, "Connected to the bluetooth socket.");
                String command = "HELO\n";
                try {
                    bto = new BufferedWriter(new OutputStreamWriter(btSocket.getOutputStream(), "ASCII"));
                } catch (IOException ex) {
                    Log.d(TAG, "Failed to write a command: " + ex.toString());
                    return false;
                }
                Log.d(TAG, "Command is sent: " + command);

                String output;
                try {
                    bti = new BufferedReader(new InputStreamReader(btSocket.getInputStream(), "ASCII"));
                } catch (IOException ex) {
                    Log.d(TAG, "Failed to write a command: " + ex.toString());
                    return false;
                }
            }
        }
        return true;
    }

}
