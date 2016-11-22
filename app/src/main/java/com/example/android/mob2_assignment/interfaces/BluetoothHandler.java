package com.example.android.mob2_assignment.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.example.android.mob2_assignment.activities.ActivityConnect;
import com.example.android.mob2_assignment.activities.CoffeeActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHandler {
    private static BluetoothSocket mSocket;
    private static ActivityConnect activityConnect;
    private static CoffeeActivity coffeeActivity;
    private static ConnectedThread readWrite;
    private BluetoothDevice mDevice;

    public BluetoothHandler(BluetoothDevice mDevice, ActivityConnect activity) {
        this.mDevice = mDevice;
        activityConnect = activity;
    }

    private static void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCoffeeActivity(CoffeeActivity activity) {
        coffeeActivity = activity;
    }

    public ConnectThread getConnectionThread() {
        return new ConnectThread(mDevice);
    }

    public ConnectedThread getReadWriteThread() {
        return readWrite;
    }

    private static class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        public void run() {
            try {
                mSocket.connect();
                readWrite = new ConnectedThread(mSocket);
                readWrite.start();
            } catch (IOException connectException) {
                cancel();
            } finally {
                activityConnect.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityConnect.startNewActivity(mSocket);
                    }
                });
            }
        }
    }

    public static class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // Call this from the main activity to send data to the remote device /
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                if (mSocket == null) {
                    cancel();
                }
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    Log.d("Reading", "Is it reading");
                    bytes = mmInStream.read(buffer);
                    Log.d("Bytes", bytes + " ");
                    coffeeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void closeReadWriteConnection() {
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mmOutStream != null) {
                try {
                    mmOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cancel();
        }
    }
}