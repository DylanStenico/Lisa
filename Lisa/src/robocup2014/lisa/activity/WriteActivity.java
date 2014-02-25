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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class WriteActivity extends Activity {

	private String CM_AHEAD = "a", CM_BACK = "b", CM_LEFT = "c",
			CM_RIGHT = "d", CM_AHEAD_LEFT = "e", CM_AHEAD_RIGHT = "f",
			CM_BACK_LEFT = "g", CM_BACK_RIGHT = "h", CM_TURN_CW = "i",
			CM_TURN_CCW = "j", CM_SPEED = "k", CM_BEGIN = "#", CM_STOP = "*",
			CM_BREAK = "&";

	private Button goAhead, goBack, goLeft, goRight, goAheadLeft, goAheadRight,
			goBackLeft, goBackRight, turnClockWise, turnCounterClockWise;

	private ConnectedThread ct;
	private SeekBar SpeedBar;
	private TextView SpeedView;
	private Switch Piloting_Switch;

	private Boolean use_break = false;

	private Display mDisplay;
	private Point displaySize = new Point();
	private int sizeThreshold = 2100;
	private Boolean Tablet = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write);
		// Show the Up button in the action bar.
		setupActionBar();

		mDisplay = getWindowManager().getDefaultDisplay();
		mDisplay.getSize(displaySize);
		// Toast.makeText(getApplicationContext(), displaySize.x+":"+
		// displaySize.y,Toast.LENGTH_LONG).show();
		if (displaySize.x >= sizeThreshold) {
			Tablet = true;
		}

		goAhead = (Button) findViewById(R.id.goAhead);
		goBack = (Button) findViewById(R.id.goBack);
		goLeft = (Button) findViewById(R.id.goLeft);
		goRight = (Button) findViewById(R.id.goRight);
		goAheadLeft = (Button) findViewById(R.id.goAheadLeft);
		goAheadRight = (Button) findViewById(R.id.goAheadRight);
		goBackLeft = (Button) findViewById(R.id.goBackLeft);
		goBackRight = (Button) findViewById(R.id.goBackRight);
		turnClockWise = (Button) findViewById(R.id.turnCW);
		turnCounterClockWise = (Button) findViewById(R.id.turnCCW);

		/*
		 * if(!Tablet) { Toast.makeText(getApplicationContext(),
		 * displaySize.x+":"+displaySize.y,Toast.LENGTH_LONG).show();
		 * 
		 * int h = (displaySize.x - 150) / 5; int w = h;
		 * 
		 * goAhead.setHeight(h); goAhead.setWidth(w);
		 * 
		 * goBack.setHeight(h); goBack.setWidth(w);
		 * 
		 * goLeft.setHeight(h); goLeft.setWidth(w);
		 * 
		 * goRight.setHeight(h); goRight.setWidth(w);
		 * 
		 * goAheadLeft.setHeight(h); goAheadLeft.setWidth(w);
		 * 
		 * goAheadRight.setHeight(h); goAheadRight.setWidth(w);
		 * 
		 * goBackLeft.setHeight(h); goBackLeft.setWidth(w);
		 * 
		 * goBackRight.setHeight(h); goBackRight.setWidth(w);
		 * 
		 * turnClockWise.setHeight(h); turnClockWise.setWidth(w);
		 * 
		 * turnCounterClockWise.setHeight(h); turnCounterClockWise.setWidth(w);
		 * }
		 */

		goAhead.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_AHEAD);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		goBack.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_BACK);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		goLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_LEFT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});
		goRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_RIGHT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});
		goAheadLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_AHEAD_LEFT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		goAheadRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_AHEAD_RIGHT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});
		goBackLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_BACK_LEFT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});
		goBackRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_BACK_RIGHT);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		turnClockWise.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_TURN_CW);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		turnCounterClockWise.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
						|| motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
					SendCommand(CM_TURN_CCW);
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
						&& use_break) {
					SendCommand(CM_BREAK);
				}
				return false;
			}
		});

		SpeedBar = (SeekBar) findViewById(R.id.SpeedBar);
		SpeedBar.setMax(255);
		SpeedBar.setProgress(100);
		// SpeedView.setText("Speed: "+100);
		SpeedBar.setOnSeekBarChangeListener(speedBar_change);

		SpeedView = (TextView) findViewById(R.id.SpeedView);

		ct = Globals.getConnectedThread();
		ct.setHandler(myHandler);
		/*
		 * if (!ct.isAlive()) ct.start();
		 */
		if (ct.getState() == Thread.State.NEW) {
			ct.start();
		}

		SendCommand(CM_BEGIN);
	}

	private SeekBar.OnSeekBarChangeListener speedBar_change = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			SpeedView.setText("Speed: " + i);
			byte[] value = { (byte) i };
			for (int x = 0; x < 10; x++) {
				ct.write(value);
				SendCommand(CM_SPEED);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	};

	private Switch.OnCheckedChangeListener piloting_change = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton,
				boolean Checked) {
			if (Checked) {
				SendCommand(CM_BEGIN);
			} else {
				SendCommand(CM_STOP);
			}
		}
	};

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
		getMenuInflater().inflate(R.menu.write, menu);

		Piloting_Switch = (Switch) menu.findItem(R.id.piloting_switch)
				.getActionView();
		Piloting_Switch.setChecked(true);
		Piloting_Switch.setOnCheckedChangeListener(piloting_change);

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
			SendCommand(CM_STOP);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Handler
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Globals.RA_MSG_READ:
				String readStr = (String) msg.obj;
				break;
			}
		}
	};

	private void SendCommand(String command) {
		ct.write(command.getBytes());
	}
}
