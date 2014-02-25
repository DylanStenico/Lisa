package robocup2014.lisa.activity;

import robocup2014.lisa.connection.ConnectedThread;
import robocup2014.lisa.connection.Globals;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by luca on 6/14/13.
 */
public class TemperatureActivity extends Activity {

	private Sensor[] tSensor = new Sensor[8];

	private String CM_REQUEST_VALUE = "$", CM_BEGIN = "$", CM_STOP = "%",
			CM_THRESHOLD = "@";

	private String received_value = CM_STOP;

	private ConnectedThread ct;

	private Button sendThreshold;
	private EditText textThreshold;
	private int Threshold;
	private String Threshold_string;

	private Display mDisplay;
	private Point displaySize = new Point();
	private int sizeThreshold = 2100;
	private Boolean Tablet = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temperature);
		// Show the Up button in the action bar.
		setupActionBar();

		ct = Globals.getConnectedThread();
		ct.setHandler(myHandler);
		/*
		 * if (!ct.isAlive()) ct.start();
		 */
		if (ct.getState() == Thread.State.NEW) {
			ct.start();
		}

		mDisplay = getWindowManager().getDefaultDisplay();
		mDisplay.getSize(displaySize);
		if (displaySize.x >= sizeThreshold) {
			Tablet = true;
		}

		tSensor[0] = new Sensor(R.id.tSensor0, 0);
		tSensor[1] = new Sensor(R.id.tSensor1, 1);
		tSensor[2] = new Sensor(R.id.tSensor2, 2);
		tSensor[3] = new Sensor(R.id.tSensor3, 3);
		tSensor[4] = new Sensor(R.id.tSensor4, 4);
		tSensor[5] = new Sensor(R.id.tSensor5, 5);
		tSensor[6] = new Sensor(R.id.tSensor6, 6);
		tSensor[7] = new Sensor(R.id.tSensor7, 7);

		textThreshold = (EditText) findViewById(R.id.textThreshold);
		sendThreshold = (Button) findViewById(R.id.sendThreshold);
		sendThreshold.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Threshold_string = textThreshold.getText().toString();
				Threshold = stringToInt(Threshold_string);
				Threshold_string = CM_THRESHOLD + Threshold_string;
				ct.write(Threshold_string.getBytes());
			}
		});

	}

	// auto build
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.temperature, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_refresh_all:
			request_Sensor_Value(0);
			request_Sensor_Value(1);
			request_Sensor_Value(2);
			request_Sensor_Value(3);
			request_Sensor_Value(4);
			request_Sensor_Value(5);
			request_Sensor_Value(6);
			request_Sensor_Value(7);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Handler
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int Pin;

			switch (msg.what) {
			case Globals.RA_MSG_READ:

				String read_string = (String) msg.obj;

				if (received_value.equals(CM_STOP)) {
					received_value = read_string;
				} else {
					received_value += read_string;
				}

				if (received_value.contains(CM_BEGIN)
						&& received_value.contains(CM_STOP)) {
					String pin = received_value.substring(1, 2);
					String value = received_value.substring(2,
							received_value.indexOf(CM_STOP));

					Pin = stringToInt(pin);
					tSensor[Pin].setValue(value);

					received_value = CM_STOP;
				}
				break;
			}
		}
	};

	private class Sensor {
		private Button mButton;
		private int Value;
		private int Pin;

		Sensor(int id, int pin) {
			Pin = pin;

			mButton = (Button) findViewById(id);
			mButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					request_Sensor_Value(Pin);
				}
			});

			/*
			 * if(!Tablet) { ViewGroup.LayoutParams params =
			 * mButton.getLayoutParams(); //params.width = ((displaySize.x -
			 * 100) / 2); //params.height = ((displaySize.x - 100) / 3);
			 * Toast.makeText
			 * (getApplicationContext(),params.width+":"+params.height
			 * ,Toast.LENGTH_LONG).show(); params.width = 100; //params.height =
			 * 100; mButton.setLayoutParams(params); }
			 */

			refresh();
		}

		public void setValue(String value_string) {

			Value = stringToInt(value_string);

			refresh();
		}

		private void refresh() {
			mButton.setText(Pin + ": " + Value);
		}
	}

	private void request_Sensor_Value(int i) {
		String request = CM_REQUEST_VALUE + i;
		ct.write(request.getBytes());
	}

	private int stringToInt(String string) {
		int value_int = 0;

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			int digit = getInt(c);
			int power = string.length() - (i + 1);
			value_int += digit * (Math.pow(10, power));
		}

		return value_int;
	}

	private int getInt(char c) {
		int i = 0;

		// Toast.makeText(getApplicationContext(),c+"",Toast.LENGTH_LONG).show();

		switch (c) {
		case '0':
			i = 0;
			break;
		case '1':
			i = 1;
			break;
		case '2':
			i = 2;
			break;
		case '3':
			i = 3;
			break;
		case '4':
			i = 4;
			break;
		case '5':
			i = 5;
			break;
		case '6':
			i = 6;
			break;
		case '7':
			i = 7;
			break;
		case '8':
			i = 8;
			break;
		case '9':
			i = 9;
			break;
		}

		return i;
	}
}