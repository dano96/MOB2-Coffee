package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class BackgroundConnection extends Application {
    private ConnectionHandler handler;
    private BluetoothDevice device;

    public BackgroundConnection() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ConnectionHandler getConnectionHandler() {
        return this.handler;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        handler = new ConnectionHandler(device);
    }
}
