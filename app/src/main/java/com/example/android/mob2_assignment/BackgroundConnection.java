package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

/**
 * Created by Naxxo on 03-Nov-16.
 */
public class BackgroundConnection extends Application {
    private ConnectionHandler handler;
    private BluetoothDevice device;

    public BackgroundConnection(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new ConnectionHandler(device);
    }
}
