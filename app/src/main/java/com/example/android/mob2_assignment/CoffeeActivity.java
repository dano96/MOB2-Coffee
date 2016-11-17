package com.example.android.mob2_assignment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class CoffeeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    ConnectionHandler mHandler;
    ConnectionHandler.ConnectedThread readWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        final ArrayList<String> arrayList = new ArrayList<String>();
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        Preset first = new Preset("Coffie every morning", 1, new Date());
        Preset second = new Preset("Coffie for 2 in lunch", 2, new Date());
        Preset third = new Preset("Coffie in an hour", 1, new Date());

        ImageButton oneCupButton = (ImageButton) findViewById(R.id.oneCupButton);
        ImageButton twoCupsButton = (ImageButton) findViewById(R.id.twoCupsButton);
        ImageButton heatUpButton = (ImageButton) findViewById(R.id.heatUp);
        Button presetButton = (Button)findViewById(R.id.setPresetButton);
        ListView listView = (ListView) findViewById(R.id.presetList);

        oneCupButton.setOnClickListener(this);
        twoCupsButton.setOnClickListener(this);
        heatUpButton.setOnClickListener(this);
        presetButton.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        mHandler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();

        readWrite = mHandler.getReadWriteThread();
        mHandler.setCoffeeActivity(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String nfcValue = bundle.getString("nfcValue", "");
            if(nfcValue != ""){
                readWrite.write(nfcValue.getBytes());
            }
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oneCupButton:
                Log.d("Writing", "Is it working");
                readWrite.write("1".getBytes());
                break;
            case R.id.twoCupsButton:
                readWrite.write("2".getBytes());
                break;
            case R.id.heatUp:
                readWrite.write("0".getBytes());
                break;
            case R.id.setPresetButton:

                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.getReadWriteThread().closeReadWriteConnection();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}