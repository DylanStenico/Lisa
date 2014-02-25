package robocup2014.lisa.activity;

import java.io.IOException;

import robocup2014.lisa.connection.ConnectThread;
import robocup2014.lisa.connection.ConnectedThread;
import robocup2014.lisa.connection.Globals;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private static final int ST_BT_NOT_AVAILABLE = 0, ST_BT_AVAILABLE = 1,
			ST_DEVICE_NOT_CONNECTED = 2, ST_DEVICE_CONNECTING = 3,
			ST_DEVICE_CONNECTED = 4, ST_CONNECTION_ERROR = 99;

	private static final int REQUEST_BT_SETTING_ON = 1,
			REQUEST_CONNECT_DEVICE = 2;

	private int currentStatus;

	private Button btConnect, btDisconnect, btReadActivity, btWriteActivity,
	btMazeActivity, btTemperatureActivity, btLisaActivity;

	private TextView txtState;
	private ProgressBar pbConnecting;
	private BluetoothAdapter bta = Globals.getBTA();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		btConnect = (Button) findViewById(R.id.btMainConnect);
		btDisconnect = (Button) findViewById(R.id.btMainDisconnect);
		btReadActivity = (Button) findViewById(R.id.btMainReadActivity);
		btWriteActivity = (Button) findViewById(R.id.btMainWriteActivity);
		btMazeActivity = (Button) findViewById(R.id.btMainMazeActivity);
		btTemperatureActivity = (Button) findViewById(R.id.btMainTemperatureActivity);
		btLisaActivity = (Button) findViewById(R.id.btMainLisaActivity);
		txtState = (TextView) findViewById(R.id.txtMainState);
		pbConnecting = (ProgressBar) findViewById(R.id.prMainWaiting);

		btConnect.setOnClickListener(btConnect_click);
		btDisconnect.setOnClickListener(btDisconnect_click);
		btReadActivity.setOnClickListener(btReadActivity_click);
		btWriteActivity.setOnClickListener(btWriteActivity_click);
		btMazeActivity.setOnClickListener(btMazeActivity_click);
		btTemperatureActivity.setOnClickListener(btTemperature_click);
		btLisaActivity.setOnClickListener(btLisaActivity_click);

		// manage current status
		if (bta.isEnabled()) {
			Globals.d("BT Available != null");
			if (Globals.getCurrentDevice() == null){
				currentStatus = ST_DEVICE_NOT_CONNECTED;
				
				Intent i = new Intent(getApplicationContext(), SelectDevice.class);
				String pkg=getPackageName();
				i.putExtra(pkg+".myData", true);
				startActivityForResult(i, REQUEST_CONNECT_DEVICE);
			}
			else {
				if (Globals.getCurrentSocket() != null)
					if (Globals.getCurrentSocket().isConnected())
						currentStatus = ST_DEVICE_CONNECTED;
					else
						currentStatus = ST_CONNECTION_ERROR;
				else

					currentStatus = ST_DEVICE_CONNECTING;
			}
			setInterface();
		} else {
			// request BT activation
			Globals.d("BT not Available");
			currentStatus = ST_BT_NOT_AVAILABLE;
			setInterface();
			Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(i, REQUEST_BT_SETTING_ON);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_BT_SETTING_ON:
			if (bta.enable()) {
				currentStatus = ST_DEVICE_NOT_CONNECTED;
			}
			break;
		case REQUEST_CONNECT_DEVICE:
			if (Globals.getCurrentDevice() != null) {
				currentStatus = ST_DEVICE_CONNECTING;
				ConnectThread ct = new ConnectThread(Globals.getCurrentDevice().getDevice(), myHandler);
				ct.start();
			}
			break;
		}
		setInterface();
	}

	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Globals.CT_CONNECTION_OK:
				ConnectedThread ct = new ConnectedThread();
				Globals.setConnectedThread(ct);
				currentStatus = ST_DEVICE_CONNECTED;
				setInterface();
				break;
			case Globals.CT_CONNECTION_KO:
				currentStatus = ST_CONNECTION_ERROR;
				setInterface();
				Globals.setCurrentDevice(null);
				break;

			}
		}
	};
	// listener
	private OnClickListener btConnect_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), SelectDevice.class);
			String pkg=getPackageName();
			i.putExtra(pkg+".myData", false);
			startActivityForResult(i, REQUEST_CONNECT_DEVICE);
		}
	};
	private OnClickListener btDisconnect_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				Globals.getCurrentSocket().close();
			} catch (IOException e) {
			}
			Globals.setCurrentDevice(null);
			currentStatus = ST_DEVICE_NOT_CONNECTED;
			setInterface();
		}
	};
	private OnClickListener btReadActivity_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), ReadActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btWriteActivity_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), WriteActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btMazeActivity_click = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent i = new Intent(getApplicationContext(), MazeActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btTemperature_click = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent i = new Intent(getApplicationContext(),
					TemperatureActivity.class);
			startActivity(i);
		}
	};

	private OnClickListener btLisaActivity_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), LisaActivity.class);
			startActivity(i);
		}
	};

	// method
	public void setInterface() {
		switch (currentStatus) {
		case ST_BT_NOT_AVAILABLE:
			btConnect.setEnabled(false);
			btDisconnect.setEnabled(false);
			btReadActivity.setEnabled(false);
			btWriteActivity.setEnabled(false);
			btMazeActivity.setEnabled(false);
			btTemperatureActivity.setEnabled(false);
			btLisaActivity.setEnabled(false);
			txtState.setText(getString(R.string.main_state_BT_not_available));
			pbConnecting.setVisibility(View.INVISIBLE);
			break;
		case ST_DEVICE_NOT_CONNECTED:
			btConnect.setEnabled(true);
			btDisconnect.setEnabled(false);
			btReadActivity.setEnabled(false);
			btWriteActivity.setEnabled(false);
			btMazeActivity.setEnabled(false);
			btTemperatureActivity.setEnabled(false);
			btLisaActivity.setEnabled(false);
			txtState.setText(R.string.main_state_default);
			pbConnecting.setVisibility(View.INVISIBLE);
			break;
		case ST_DEVICE_CONNECTING:
			btConnect.setEnabled(false);
			btDisconnect.setEnabled(false);
			btReadActivity.setEnabled(false);
			btWriteActivity.setEnabled(false);
			btMazeActivity.setEnabled(false);
			btTemperatureActivity.setEnabled(false);
			btLisaActivity.setEnabled(false);
			txtState.setText(getString(R.string.main_state_connecting) + " "
					+ Globals.getCurrentDevice().getDevice().getName());
			pbConnecting.setVisibility(View.VISIBLE);
			break;
		case ST_DEVICE_CONNECTED:
			btConnect.setEnabled(false);
			btDisconnect.setEnabled(true);
			btReadActivity.setEnabled(true);
			btWriteActivity.setEnabled(false);
			btMazeActivity.setEnabled(false);
			btTemperatureActivity.setEnabled(false);
			btLisaActivity.setEnabled(true);
			txtState.setText(getString(R.string.main_state_connected) + " "
					+ Globals.getCurrentDevice().getDevice().getName());
			pbConnecting.setVisibility(View.INVISIBLE);
			break;
		case ST_CONNECTION_ERROR:
			btConnect.setEnabled(true);
			btDisconnect.setEnabled(false);
			btReadActivity.setEnabled(false);
			btWriteActivity.setEnabled(false);
			btMazeActivity.setEnabled(false);
			btTemperatureActivity.setEnabled(false);
			btLisaActivity.setEnabled(false);
			txtState.setText(getString(R.string.main_state_error_connection)
					+ " " + Globals.getCurrentDevice().getDevice().getName());
			pbConnecting.setVisibility(View.INVISIBLE);
			break;
		}
	}

}
