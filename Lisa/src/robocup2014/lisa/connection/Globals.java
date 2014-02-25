package robocup2014.lisa.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Globals {
	// Log
	private final static boolean DEBUG = true;
	private final static String DEBUGKEY = "BTA_DEBUG";
	// BT
	private static BluetoothAdapter myBtA = BluetoothAdapter
			.getDefaultAdapter();
	private static BTDevice currentDevice;
	private static BluetoothSocket currentSocket;
	private static ConnectedThread connectedThread;
	// Constant
	public final static String PACK = "com.boschello.prj.bta";

	public final static int CT_CONNECTION_OK = 99;
	public final static int CT_CONNECTION_KO = 98;

	public final static int RA_MSG_READ = 1;

	// buffer shared
	private static String completeRead = "";

	public static void d(String s) {
		if (DEBUG)
			Log.d(DEBUGKEY, s);
	}

	public static BluetoothAdapter getBTA() {
		return myBtA;
	}

	public static void setCurrentDevice(BTDevice b) {
		currentDevice = b;
	}

	public static BTDevice getCurrentDevice() {
		return currentDevice;
	}

	public static BluetoothSocket getCurrentSocket() {
		return currentSocket;
	}

	public static void setCurrentSocket(BluetoothSocket b) {
		currentSocket = b;
	}

	public static void setConnectedThread(ConnectedThread c) {
		connectedThread = c;
	}

	public static ConnectedThread getConnectedThread() {
		return connectedThread;
	}

	public static String getCompleteString() {
		return completeRead;
	}

	public static void setCompleteString(String s) {
		completeRead = s;

	}
}
