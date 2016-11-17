package com.example.android.mob2_assignment;

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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class ActivityConnect extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final int REQUEST_ENABLE_BT = 105;
    private ProgressDialog dialog;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;
    private CoffeeListAdapter adapter;
    private static final int REQUEST_RUNTIME_PERMISSION = 1;
    private final String MAC_ADDRESS = "mac";
    private String nfcData;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && !devices.contains(device)) {
                    synchronized (this) {
                        devices.add(device);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Your device does not support Bluetooth");
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            init();
            nfcData = NFChandler.readTag(getIntent());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String mac = preferences.getString(MAC_ADDRESS, "");
            Log.d("mac", mac + "something");
            if(mac != ""){
                connect(mac);
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
    }

    private void init() {
        ListView listView = (ListView) findViewById(R.id.listview_main);
        Button buttonScan = (Button) findViewById(R.id.scan);
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        devices = new ArrayList<>();
        if (!bondedDevices.isEmpty()) {
            devices.addAll(bondedDevices);
        }

        adapter = new CoffeeListAdapter(this, devices);
        listView.setAdapter(adapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        listView.setOnItemClickListener(this);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ActivityConnect.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    bluetoothAdapter.startDiscovery();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth enabled");
                init();
            } else if (resultCode == RESULT_CANCELED) {
                showToast("Application requires bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RUNTIME_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.startDiscovery();
            } else {
                Toast.makeText(this, "Finding devices requires access to your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String mac = devices.get(position).getAddress();
        connect(mac);
    }

    private void connect(String macAddress){
        BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        ((BackgroundConnection) this.getApplicationContext()).setDevice(btDevice, this);
        ConnectionHandler handler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();
        Thread connection = handler.getConnectionThread();

        dialog = ProgressDialog.show(this, "Connecting...", "Connecting to the device", true, false);
        dialog.show();
        connection.start();
    }

    public void startNewActivity(BluetoothSocket socket) {
        dialog.dismiss();
        if (socket.isConnected()) {
            Intent changeActivity = new Intent(ActivityConnect.this, CoffeeActivity.class);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MAC_ADDRESS, socket.getRemoteDevice().getAddress());
            editor.commit();
            editor.apply();
            if(nfcData != null){
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