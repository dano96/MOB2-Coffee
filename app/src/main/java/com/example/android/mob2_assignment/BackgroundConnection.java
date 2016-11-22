package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import com.example.android.mob2_assignment.activities.ActivityConnect;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;

public class BackgroundConnection extends Application {
    private BluetoothHandler handler;

    public BackgroundConnection() {
        super();
    }

    public BluetoothHandler getConnectionHandler() {
        return this.handler;
    }

    public void setDevice(BluetoothDevice device, ActivityConnect activityConnect) {
        handler = new BluetoothHandler(device, activityConnect);
    }
}