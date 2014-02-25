package robocup2014.lisa.connection;

import android.bluetooth.BluetoothDevice;

public class BTDevice {
	private BluetoothDevice b;

	public BTDevice(BluetoothDevice device) {
		b = device;
	}

	public BluetoothDevice getDevice() {
		return b;
	}

	@Override
	public String toString() {
		return b.getName() + "\n" + b.getAddress();
	}

}
