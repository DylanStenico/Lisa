package robocup2014.lisa.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class ConnectedThread extends Thread {
	private final BluetoothSocket btSocket;
	private final InputStream inStream;
	private final OutputStream outStream;
	private Handler myHandler;
	private String currentRead;
	private boolean stop = false;

	public ConnectedThread() {

		btSocket = Globals.getCurrentSocket();
		InputStream is = null;
		OutputStream os = null;
		try {
			is = btSocket.getInputStream();
			os = btSocket.getOutputStream();
		} catch (IOException e) {
		}
		inStream = is;
		outStream = os;
		myHandler = null;
	}

	public void setHandler(Handler h) {
		myHandler = h;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int bytes;
		while (!stop) {
			try {
				for (int i = 0; i < 1024; i++)
					buffer[i] = 0;
				bytes = inStream.read(buffer);
				currentRead = new String(buffer);
				Globals.setCompleteString(Globals.getCompleteString()
						+ currentRead);
				myHandler.obtainMessage(Globals.RA_MSG_READ, bytes, -1,
						currentRead).sendToTarget();
			} catch (IOException e) {
				break;
			}
		}

	}

	public void write(byte[] bytes) {
		try {
			outStream.write(bytes);
		} catch (IOException e) {
		}
	}

	public void cancel() {
		try {
			btSocket.close();
		} catch (IOException e) {
		}
		;
	}
}
