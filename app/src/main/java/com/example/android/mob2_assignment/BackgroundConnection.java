package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class BackgroundConnection extends Application {
    private ConnectionHandler handler;

    public BackgroundConnection() {
        super();
    }

    public ConnectionHandler getConnectionHandler() {
        return this.handler;
    }

    public void setDevice(BluetoothDevice device, ActivityConnect activityConnect) {
        handler = new ConnectionHandler(device, activityConnect);
    }
}