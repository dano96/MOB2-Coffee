package com.example.android.mob2_assignment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CoffeeActivity extends AppCompatActivity implements View.OnClickListener{
    Button button1;
    Button button2;
    ConnectionHandler mHandler;
    ConnectionHandler.ConnectedThread readWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        mHandler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();

        readWrite = mHandler.getReadWriteThread();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                Log.d("Writing", "Is it working");
                readWrite.write("Say hello".getBytes());
                break;
            case R.id.button2:

                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.getReadWriteThread().closeReadWriteConnection();
    }
}