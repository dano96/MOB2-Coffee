package com.example.android.mob2_assignment.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.mob2_assignment.BackgroundConnection;
import com.example.android.mob2_assignment.CoffeeListAdapter;
import com.example.android.mob2_assignment.R;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;
import com.example.android.mob2_assignment.interfaces.NFChandler;

import java.util.ArrayList;
import java.util.Set;

// First screen when you launch the application
public class ActivityConnect extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final int REQUEST_ENABLE_BT = 105;
    private static final int REQUEST_RUNTIME_PERMISSION = 1;
    private final String MAC_ADDRESS = "mac";
    private ProgressDialog dialog;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;
    private CoffeeListAdapter adapter;

    // Find available devicees
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // If bluetooth device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get bluetooth devices from intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Add devices to arraylist to present on the screen
                if (device.getName() != null && !devices.contains(device)) {
                        devices.add(device);
                        adapter.notifyDataSetChanged();
                }
            }
        }
    };
    private String nfcData;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // Get the bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Your device does not support Bluetooth");
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // If bluetooth is on
            init();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
    }

    // Initialize - get adapter for the list and populating the list
    private void init() {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        devices = new ArrayList<>();
        if (!bondedDevices.isEmpty()) {
            devices.addAll(bondedDevices);
        }
        ListView listView = (ListView) findViewById(R.id.listView_main);
        // Create new customized adapter
        adapter = new CoffeeListAdapter(this, devices);
        listView.setAdapter(adapter);
        // OnItemClickListener for clicking on an item on the list
        listView.setOnItemClickListener(this);

        // The broadcast receiver will only be called if this finds a new device
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register broadcast receiver
        registerReceiver(mReceiver, filter);

        // OnClickListener for clicking the scan button
        Button buttonScan = (Button) findViewById(R.id.scan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This gives runtime permisson, because scanning requires location permission
                if (ContextCompat.checkSelfPermission(ActivityConnect.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                } else {
                    bluetoothAdapter.startDiscovery();
                }
            }
        });

        // Reading NFC tag
        nfcData = NFChandler.readTag(getIntent());

        // Check if we have saved a mac address of a bluetooth device, so when you sign in again the app will connect to the last known device automatically
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mac = preferences.getString(MAC_ADDRESS, "");
        if (!mac.equals("")) {
            connect(mac);
        }
    }
    // Checks if you have bluetooth on in your phone, if not it asks if it can enable it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth enabled");
                init();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Application requires bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // For runtime permission - to check if you give permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RUNTIME_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If you have permission, start looking for devices
                bluetoothAdapter.startDiscovery();
            } else {
                Toast.makeText(this, "Finding devices requires access to your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    // When you click on one item on the list
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String mac = devices.get(position).getAddress();
        connect(mac);
    }

    private void connect(String macAddress) {
        // Creates a Bluetooth device based on the mac address
        BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        // Call setDevice = create new handler
        ((BackgroundConnection) this.getApplicationContext()).setDevice(btDevice, this);
        // Get the handler you just made
        BluetoothHandler handler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();
        // Create a new thread, so it doesn't block the main program when you connect to the bluetooth device
        Thread connection = handler.getConnectionThread();
        // Loading screen
        dialog = ProgressDialog.show(this, "Connecting...", "Connecting to the device", true, false);
        dialog.show();
        // Start the thread
        connection.start();
    }

    // Entering the main activity
    public void startNewActivity(BluetoothSocket socket) {
        // Close loading screen
        dialog.dismiss();
        if (socket.isConnected()) {
            Intent changeActivity = new Intent(ActivityConnect.this, CoffeeActivity.class);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MAC_ADDRESS, socket.getRemoteDevice().getAddress());
            editor.apply();
            if (nfcData != null) {
                changeActivity.putExtra("nfcValue", nfcData);
            }
            startActivity(changeActivity);
        } else {
            Toast.makeText(this, "You cannot connect right now", Toast.LENGTH_LONG).show();
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onBackPressed() {
        // When you press back one time this will show a toast that tells you that if you press back again it will close the application
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            bluetoothAdapter.disable();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}