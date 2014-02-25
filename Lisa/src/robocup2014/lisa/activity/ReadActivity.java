package robocup2014.lisa.activity;

import robocup2014.lisa.connection.ConnectedThread;
import robocup2014.lisa.connection.Globals;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReadActivity extends Activity {

	private EditText edTextToSend;
	private Button btSend;
	private Button btClear;
	private TextView txtLog;
	private ConnectedThread ct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
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

		edTextToSend = (EditText) findViewById(R.id.txtRAToSend);
		btSend = (Button) findViewById(R.id.btRASend);
		btClear = (Button) findViewById(R.id.btRAClear);
		txtLog = (TextView) findViewById(R.id.txtRALog);
		txtLog.setText(Globals.getCompleteString());
		txtLog.setMovementMethod(new ScrollingMovementMethod());

		btSend.setOnClickListener(btSend_click);
		btClear.setOnClickListener(btClear_click);

	}

	// listener
	private OnClickListener btSend_click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ct.write(edTextToSend.getText().toString().getBytes());
			edTextToSend.setText("");
		}
	};
	private OnClickListener btClear_click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			txtLog.setText("");
			Globals.setCompleteString("");
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
		getMenuInflater().inflate(R.menu.read, menu);
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
				txtLog.append(readStr);
				int ScrollAmount = txtLog.getLayout().getLineTop(
						txtLog.getLineCount())
						- txtLog.getHeight();
				txtLog.scrollTo(0, ScrollAmount);
				break;
			}
		}
	};
}
