package com.example.android.mob2_assignment.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.android.mob2_assignment.BackgroundConnection;
import com.example.android.mob2_assignment.Preset;
import com.example.android.mob2_assignment.R;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CoffeeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    BluetoothHandler mHandler;
    BluetoothHandler.ConnectedThread readWrite;
    RadioGroup radioCupGroup;
    EditText hoursText;
    EditText minText;
    RadioGroup timeToMakeRadio;
    EditText nameOfPreset;
    ArrayAdapter<Preset> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        final ArrayList<Preset> arrayList = new ArrayList<>();

        Preset first = new Preset("Coffee every morning", 1, new Date());
        Preset second = new Preset("Coffee for 2 in lunch", 2, new Date());
        Preset third = new Preset("Coffee in an hour", 1, new Date());

        arrayList.addAll(Arrays.asList(first, second, third));

        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        ImageButton oneCupButton = (ImageButton) findViewById(R.id.oneCupButton);
        ImageButton twoCupsButton = (ImageButton) findViewById(R.id.twoCupsButton);
        ImageButton heatUpButton = (ImageButton) findViewById(R.id.heatUp);
        Button presetButton = (Button) findViewById(R.id.setPresetButton);

        ListView listView = (ListView) findViewById(R.id.presetList);
        listView.setAdapter(itemsAdapter);

        oneCupButton.setOnClickListener(this);
        twoCupsButton.setOnClickListener(this);
        heatUpButton.setOnClickListener(this);
        presetButton.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        mHandler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();

        readWrite = mHandler.getReadWriteThread();
        mHandler.setCoffeeActivity(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String nfcValue = bundle.getString("nfcValue", "");
            if (!nfcValue.equals("")) {
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
                View dialogLayout = getLayoutInflater().inflate(R.layout.preset_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Create a preset");
                builder.setView(dialogLayout);
                builder.setCancelable(false);

                nameOfPreset = (EditText) dialogLayout.findViewById(R.id.nameOfPreset);
                radioCupGroup = (RadioGroup) dialogLayout.findViewById(R.id.radiocup);
                hoursText = (EditText) dialogLayout.findViewById(R.id.hour);
                minText = (EditText) dialogLayout.findViewById(R.id.minutes);
                timeToMakeRadio = (RadioGroup) dialogLayout.findViewById(R.id.time_to_makeradio);

                builder.setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedRadioButton = radioCupGroup.getCheckedRadioButtonId();
                        RadioButton button = (RadioButton) findViewById(R.id.radioButton4);
                        String name = nameOfPreset.getText().toString();
                        int cup = selectedRadioButton == R.id.radioButton4? 1 : 2;
                        Date somedate = new Date();
                        Preset current = new Preset(name, cup, somedate);
                        itemsAdapter.add(current);

                    }
                });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
        //To-Do
    }
}