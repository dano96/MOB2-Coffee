package com.example.android.mob2_assignment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ActivityConnect extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    public static final int REQUEST_ENABLE_BT = 105;
    private static final String TAG = "ActivityConnect";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;
    private ListView listView;
    private CoffeeListAdapter adapter;
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
    private CardView cardView;
    private TextView deviceInfo;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Your device does not support Bluetooth");
            finish();
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT );
        }else {
            init();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
    }

    private void init(){
        findViews();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        devices = new ArrayList<>();
        if(!bondedDevices.isEmpty()){
            devices.addAll(bondedDevices);
        }

        adapter = new CoffeeListAdapter(this,devices);
        listView.setAdapter(adapter);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    private void findViews(){
        listView = (ListView)findViewById(R.id.listview_main);
        cardView = (CardView) findViewById(R.id.cardview_main);
        deviceInfo = (TextView) findViewById(R.id.info_textView);
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
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT );
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
       BackgroundConnection bConnection = new BackgroundConnection(devices.get(position));

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(cardView.getVisibility()==View.GONE) {
            Slide slide = new Slide();
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            TransitionManager.beginDelayedTransition(root, slide);
            cardView.setVisibility(View.VISIBLE);
            deviceInfo.setVisibility(View.VISIBLE);
        }
        TextView deviceName = (TextView) findViewById(R.id.device_name);
        TextView deviceAddress = (TextView) findViewById(R.id.device_mac);
        TextView deviceBonded = (TextView) findViewById(R.id.device_bonded);
        deviceName.setText(devices.get(position).getName());
        deviceAddress.setText(devices.get(position).getAddress());
        deviceBonded.setText(String.valueOf(devices.get(position).getBondState()));
        return true;
    }
    public void showToast(String message){
        Toast toast = Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
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
