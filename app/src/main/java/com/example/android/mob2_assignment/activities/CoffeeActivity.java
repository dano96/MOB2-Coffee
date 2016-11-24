package com.example.android.mob2_assignment.activities;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import com.example.android.mob2_assignment.BackgroundConnection;
import com.example.android.mob2_assignment.Preset;
import com.example.android.mob2_assignment.R;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Locale.ENGLISH;

public class CoffeeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private BluetoothHandler mHandler;
    private BluetoothHandler.ConnectedThread readWrite;
    private RadioGroup radioCupGroup;
    private EditText hoursText;
    private EditText minText;
    private RadioGroup timeToMakeRadio;
    private Button btnStep;
    private TextView textDate;
    private EditText nameOfPreset;
    private ArrayAdapter<Preset> itemsAdapter;
    private Calendar date = Calendar.getInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        final ArrayList<Preset> arrayList = new ArrayList<>();
        Calendar date = Calendar.getInstance();

            String sampleDate = DATE_FORMAT.format(date.getTime());
            Preset first = new Preset("Coffee every morning", 1, sampleDate);
            Preset second = new Preset("Coffee for 2 in lunch", 2, sampleDate);
            Preset third = new Preset("Coffee in an hour", 1, sampleDate);

            arrayList.addAll(Arrays.asList(first, second, third));


        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        ImageButton oneCupButton = (ImageButton) findViewById(R.id.oneCupButton);
        ImageButton twoCupsButton = (ImageButton) findViewById(R.id.twoCupsButton);
        ImageButton heatUpButton = (ImageButton) findViewById(R.id.heatUp);
        Button presetButton = (Button) findViewById(R.id.setPresetButton);

        ListView listView = (ListView) findViewById(R.id.presetList);
        listView.setAdapter(itemsAdapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(this);

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
                @SuppressLint("InflateParams") View dialogLayout = getLayoutInflater().inflate(R.layout.preset_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Create a preset");
                builder.setView(dialogLayout);
                builder.setCancelable(false);
                date = Calendar.getInstance();

                nameOfPreset = (EditText) dialogLayout.findViewById(R.id.nameOfPreset);
                radioCupGroup = (RadioGroup) dialogLayout.findViewById(R.id.radioGroupCup);
                hoursText = (EditText) dialogLayout.findViewById(R.id.hours);
                minText = (EditText) dialogLayout.findViewById(R.id.minutes);
                timeToMakeRadio = (RadioGroup) dialogLayout.findViewById(R.id.time_to_makeRadio);
                textDate = (TextView) dialogLayout.findViewById(R.id.date);
                btnStep = (Button) dialogLayout.findViewById(R.id.stepDateBtn);

                textDate.setText(DATE_FORMAT.format(date.getTime()));

                btnStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        date.add(Calendar.DATE, 1);
                        textDate.setText(DATE_FORMAT.format(date.getTime()));
                    }
                });

                builder.setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameOfPreset.getText().toString();

                        int selectedRadioButton = radioCupGroup.getCheckedRadioButtonId();
                        int cup = (selectedRadioButton == R.id.radioButtonOneCup) ? 1 : 2;

                            String sampleDate = DATE_FORMAT.format(date.getTime());
                            Preset current = new Preset(name, cup, sampleDate);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        itemsAdapter.remove(itemsAdapter.getItem(position));
        return true;
    }
}