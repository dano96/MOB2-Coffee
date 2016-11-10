package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

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

    public ConnectionHandler getConnectionHandler() {
        return this.handler;
    }
}
