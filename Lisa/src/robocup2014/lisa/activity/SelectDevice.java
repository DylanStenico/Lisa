package robocup2014.lisa.activity;

import java.util.ArrayList;
import java.util.Set;

import robocup2014.lisa.connection.BTDevice;
import robocup2014.lisa.connection.Globals;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectDevice extends Activity {

	private ArrayList<BTDevice> DeviceList = new ArrayList<BTDevice>();
	private ArrayAdapter<BTDevice> myAdapter;
	private ListView lvDevices;
	private BluetoothAdapter bta = Globals.getBTA();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_device);
		// Show the Up button in the action bar.
		setupActionBar();

		lvDevices = (ListView) findViewById(R.id.lvDevices);
		lvDevices.setOnItemClickListener(listItem_click);

		// TODO: Load paired device

		myAdapter = new ArrayAdapter<BTDevice>(this,
				android.R.layout.simple_list_item_1, DeviceList);
		lvDevices.setAdapter(myAdapter);
		Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice d : pairedDevices) {
				DeviceList.add(new BTDevice(d));
			}
		}
		myAdapter.notifyDataSetChanged();
		Intent intent=getIntent();
		String pkg=getPackageName();
		boolean results =intent.getBooleanExtra(pkg+".myData",false); 
		if(results){
			try {
				Globals.setCurrentDevice(new BTDevice(DeviceList.get(getLisaBtIndex()).getDevice()));
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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
		getMenuInflater().inflate(R.menu.select_device, menu);
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

	//
	private OnItemClickListener listItem_click = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d("address", "dev" + DeviceList.get(position).getDevice().getAddress());
			Globals.setCurrentDevice(new BTDevice(DeviceList.get(position)
					.getDevice()));
			finish();
		}
	};
	private int getLisaBtIndex() throws Exception{
		Log.d("address", "" + DeviceList.size());
		for(int i = 0; i < DeviceList.size(); i++){
			Log.d("address", "dev" + DeviceList.get(i).getDevice().getAddress());
			if(DeviceList.get(i).getDevice().getAddress().equals("00:11:11:02:04:80")){
				return i;
			}
		}
		throw new Exception("non ho trovato nessun device");
	}
}
