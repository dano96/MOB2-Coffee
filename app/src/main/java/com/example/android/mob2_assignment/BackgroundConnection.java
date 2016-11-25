package com.example.android.mob2_assignment;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import com.example.android.mob2_assignment.activities.ActivityConnect;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;

// Used so we can keep the bluetooth connection open between activities
// If we put the handler in a separate class instead of in a activity it will stay alive for the whole session the app is open
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