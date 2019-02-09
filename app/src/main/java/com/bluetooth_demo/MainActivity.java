package com.bluetooth_demo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener,
		OnItemClickListener {
	private static final int Request_enable_Bt = 1; // Varaible for Activtiy
													// result method

	private static Button turn_on, turn_off, paired_devices, search_devices,
			make_visible;
	private static TextView bluetooth_status;
	private static ListView list_devices;
	private static ArrayList<BluetoothDevice> bluetoothDevice_List;
	private static CustomAdapter BtArrayAdapter;

	private static final String TAG = MainActivity.class.getSimpleName();// TAG
																			// for
																			// logs

	// Bluetooth Variables
	private static BluetoothAdapter bluetooth_adapter;
	private static Set<BluetoothDevice> pairedDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		checkBluetoothAdapter();// Check Bluetooth presence

	}

	// Initialize the views
	private void init() {

		turn_on = (Button) findViewById(R.id.turnOn);
		turn_off = (Button) findViewById(R.id.turnOff);
		paired_devices = (Button) findViewById(R.id.show_paired_devices);
		search_devices = (Button) findViewById(R.id.search_new_devices);
		make_visible = (Button) findViewById(R.id.make_visible);
		list_devices = (ListView) findViewById(R.id.list_devices);
		bluetooth_status = (TextView) findViewById(R.id.bluetooth_status);

	}

	// Check if bluetooth is present or not
	private void checkBluetoothAdapter() {
		bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetooth_adapter == null) {

			// If bluetooth doesn't present disable all buttons and display a
			// toast
			disableButtons();
			Toast.makeText(getApplicationContext(),
					"Your device does not support Bluetooth.",
					Toast.LENGTH_LONG).show();

		} else {
			// If bluetooth is there then check if it is ON or not for status
			if (bluetooth_adapter.isEnabled()) {
				bluetooth_status.setText("Status: Bluetooth Enabled.");
			} else {
				bluetooth_status.setText("Status: Bluetooth Disabled.");
			}

			setListeners();// Set listeners

		}
	}

	// Disable all buttons
	private void disableButtons() {
		turn_on.setEnabled(false);
		turn_off.setEnabled(false);
		paired_devices.setEnabled(false);
		search_devices.setEnabled(false);
		make_visible.setEnabled(false);
		bluetooth_status.setText("Status: - Bluetooth Not supported");
	}

	// Set click listeners
	private void setListeners() {
		turn_on.setOnClickListener(this);
		turn_off.setOnClickListener(this);
		make_visible.setOnClickListener(this);
		paired_devices.setOnClickListener(this);
		search_devices.setOnClickListener(this);
		list_devices.setOnItemClickListener(this);
	}

	// On button clicks call there representative methods
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.turnOn:
			turnOn();
			break;
		case R.id.turnOff:
			turnOff();
			break;

		case R.id.make_visible:
			makeBluetoothVisible();
			break;

		case R.id.show_paired_devices:
			showPairedDevices();
			break;

		case R.id.search_new_devices:
			searchNewDevices();
			break;

		}

	}

	// Turn bluetooth ON
	private void turnOn() {
		boolean isBluetoothEnabled = isBluetoothEnabled();// Bluetooth enabled
															// or disabled
		if (!isBluetoothEnabled) {
			// If disabled Switch On bluetooth
			Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(in, Request_enable_Bt);

		} else {

			// If it is already On then display a toast
			Toast.makeText(getApplicationContext(), "Bluetooth is already on",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// On Activity result check the response code with send code
		if (requestCode == Request_enable_Bt) {

			// If bluetooth enabled/disabled then change status
			if (bluetooth_adapter.isEnabled()) {
				Toast.makeText(getApplicationContext(), "Bluetooth turned on",
						Toast.LENGTH_SHORT).show();
				bluetooth_status.setText("Status: Bluetooth Enabled.");
			} else {
				bluetooth_status.setText("Status: Bluetooth Disabled.");
				Toast.makeText(getApplicationContext(),
						"Bluetooth enabling cancelled.", Toast.LENGTH_SHORT)
						.show();

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// Show paired devices
	private void showPairedDevices() {
		boolean isBluetoothEnabled = isBluetoothEnabled();// Before shwoing
															// devices check if
															// bluetooth is on
															// or off
		if (isBluetoothEnabled) {
			pairedDevices = bluetooth_adapter.getBondedDevices();// Get bonded
																	// devices
																	// via this
																	// method
			bluetoothDevice_List = new ArrayList<BluetoothDevice>();
			for (BluetoothDevice device : pairedDevices)
				bluetoothDevice_List.add(device);// Add bonded devices to
													// arrayList

			BtArrayAdapter = new CustomAdapter(MainActivity.this,
					R.layout.customview, bluetoothDevice_List);// set adapter
			list_devices.setAdapter(BtArrayAdapter);// Set adapter over listview
			BtArrayAdapter.notifyDataSetChanged();// notify adapter

		}
	}

	// Search new Bluetooth Devices
	private void searchNewDevices() {
		boolean isBluetoothEnabled = isBluetoothEnabled();// If bluetooth is
															// enabled then
															// proceed further
		if (isBluetoothEnabled) {
			if (bluetooth_adapter.isDiscovering()) {// Check if bluetooth
													// adapter is already
													// discovering or not
				bluetooth_adapter.cancelDiscovery();// If already discovering
													// then cancel discovery
				search_devices.setText(R.string.search_new_devices);// Change
																	// button
																	// text

			} else {
				search_devices.setText(R.string.cancel);// If not discovering
														// then change button
														// text

				bluetooth_adapter.startDiscovery();// and start discovering
													// devices
				registerReceiver(reciever, new IntentFilter(
						BluetoothDevice.ACTION_FOUND));// Register broadcast
														// receiver for New
														// Device found
			}
		}
	}

	// Broadcast Receiver for New Device Found
	final BroadcastReceiver reciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();// Get Sent Action
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {// Check if sent
																// action is
																// equal to
																// device action
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);// Get
																			// new
																			// Device

				// add the name and the MAC address of the object to the
				// arrayAdapter
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					bluetoothDevice_List = new ArrayList<BluetoothDevice>();
					bluetoothDevice_List.add(device);

					BtArrayAdapter = new CustomAdapter(MainActivity.this,
							R.layout.customview, bluetoothDevice_List);
					list_devices.setAdapter(BtArrayAdapter);
					BtArrayAdapter.notifyDataSetChanged();
					Toast.makeText(MainActivity.this, "Device found.",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MainActivity.this, "No Devices found.",
							Toast.LENGTH_SHORT).show();
				}

				search_devices.setText(R.string.search_new_devices);// Change
																	// button
																	// text
																	// after
																	// searching
																	// devices

			}
			// Other Actions of Bluetooth devices
			else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

				// Device is now connected
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// Done searching

			} else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
					.equals(action)) {
				// Device is about to disconnect

			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				// Device has disconnected

			}

		}
	};

	// Make device discoverable
	private void makeBluetoothVisible() {
		boolean isBluetoothEnabled = isBluetoothEnabled();
		if (isBluetoothEnabled) {
			Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);// Make
																			// device
																			// discoverable
																			// for
																			// 300sec
			startActivity(in);
		}
	}

	private void turnOff() {
		boolean isBluetoothEnabled = isBluetoothEnabled();
		if (isBluetoothEnabled) {
			bluetooth_adapter.disable(); // Switch off bluetooth
			bluetooth_status.setText("Status: Bluetooth Disabled."); // Change
																		// status
																		// text
			Toast.makeText(getApplicationContext(), "Bluetooth turned off",
					Toast.LENGTH_SHORT).show();
		}
	}

	// Method that return boolean value about device enabled or disabled
	private boolean isBluetoothEnabled() {
		if (!bluetooth_adapter.isEnabled()) {
			Toast.makeText(MainActivity.this,
					"Bluetooth is disabled. Please turn on first.",
					Toast.LENGTH_SHORT).show();
			return false; // if device is disabled
		}
		return true;// If device is enabled
	}

	// On listview item clicked
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		boolean isBluetoothEnabled = isBluetoothEnabled();// Before doing
															// anything check if
															// bluetooth is on
															// or off
		if (isBluetoothEnabled) {// If bluetooth ON
			// then check if selected device is already paired or not
			if (bluetoothDevice_List.get(position).getBondState() != BluetoothDevice.BOND_BONDED) {

				// If device not paired then start the thread to pair devices
				ConnectThread mConnectedThread = new ConnectThread(
						bluetoothDevice_List.get(position));
				mConnectedThread.start();
			} else {

				// else show a toast
				Toast.makeText(getApplicationContext(), "Already paired.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	// This thread will do task for pairing devices
	private class ConnectThread extends Thread {
		private BluetoothSocket mmSocket; // Bluetooth Socket
		private final BluetoothDevice mmDevice;
		private final UUID MY_UUID = UUID
				.fromString("00001101-0000-1000-8000-00805f9b34fb"); // This is
																		// Unique
																		// Id
																		// for
																		// your
																		// device

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			mmDevice = device;
			try {
				// This methods are used to make pairing
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

				Method m = device.getClass().getMethod("createRfcommSocket",
						new Class[] { int.class });
				tmp = (BluetoothSocket) m.invoke(device, 1);

			} catch (Exception e) {
				e.printStackTrace();

			}
			mmSocket = tmp;
		}

		public void run() {

			// Cancelling discovery is neccessary
			bluetooth_adapter.cancelDiscovery();// If bluetooth adapter is
												// already discovering then
												// cancel it
			try {

				// Also unregister receiver
				unregisterReceiver(reciever);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			try {
				mmSocket.connect();// Pair device
				Log.e(TAG, "Connection Establish"); // Via this Log you can
													// check the connection
													// status

			} catch (IOException connectException) {
				connectException.printStackTrace();
				try {
					mmSocket.close(); // If exception occurs close Socket
					Log.e(TAG, "Connection Failed");
				} catch (IOException closeException) {
					closeException.printStackTrace();
				}
				return;
			}

		}

		// On cancel display Log and close socket
		public void cancel() {
			try {
				Log.e(TAG, "Connection Failed on cancel");
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	// On destroy unregister receiver
	@Override
	protected void onDestroy() {

		super.onDestroy();

		// Try catch block is used because receiver can throw exception if
		// receiver not registered
		try {
			unregisterReceiver(reciever);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	// Custom Adapter class for Custom ListView Items
	private class CustomAdapter extends ArrayAdapter<BluetoothDevice> {
		private Context context;
		private int resource;
		private ArrayList<BluetoothDevice> deviceName;

		public CustomAdapter(Context context, int resource,
				ArrayList<BluetoothDevice> deviceName) {
			super(context, resource);
			this.context = context;
			this.resource = resource;
			this.deviceName = deviceName;

		}

		@Override
		public int getCount() {
			return deviceName.size();// Array List size
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = inflater.inflate(resource, parent, false); // Inflate
																			// layout
				TextView deviceName = (TextView) convertView
						.findViewById(R.id.devices_name);// Find textView id

				// Set device name and device address over textview
				deviceName.setText(this.deviceName.get(position).getName()
						+ "\n" + this.deviceName.get(position).getAddress());
			}
			return convertView;
		}
	}

}
