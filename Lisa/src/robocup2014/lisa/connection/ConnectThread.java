package robocup2014.lisa.connection;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class ConnectThread extends Thread {
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private final BluetoothSocket mySocket;
	private BluetoothAdapter bta;
	private Handler myHandler;

	public ConnectThread(BluetoothDevice device, Handler h) {
		bta = Globals.getBTA();
		BluetoothSocket tmp = null;
		myHandler = h;
		try {
			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			myHandler.obtainMessage(Globals.CT_CONNECTION_KO, 0, -1, null)
					.sendToTarget();
		}
		mySocket = tmp;
	}

	@Override
	public void run() {
		bta.startDiscovery();
		try {
			mySocket.connect();
			Globals.setCurrentSocket(mySocket);
			Globals.d("Connection succeful");
			myHandler.obtainMessage(Globals.CT_CONNECTION_OK, 0, -1, null)
					.sendToTarget();
		} catch (IOException ce) {
			Globals.d("Connection fail");
			myHandler.obtainMessage(Globals.CT_CONNECTION_KO, 0, -1, null)
					.sendToTarget();
			try {
				mySocket.close();
			} catch (IOException closeE) {
			}
		}
	}

	public void cancel() {
		try {
			mySocket.close();
		} catch (IOException closeE) {
		}
	}
}
