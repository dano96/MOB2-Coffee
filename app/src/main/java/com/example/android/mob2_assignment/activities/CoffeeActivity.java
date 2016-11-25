package com.example.android.mob2_assignment.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mob2_assignment.BackgroundConnection;
import com.example.android.mob2_assignment.Preset;
import com.example.android.mob2_assignment.R;
import com.example.android.mob2_assignment.interfaces.BluetoothHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.util.Locale.ENGLISH;

public class CoffeeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", ENGLISH);
    private static final String PRESET_KEY = "pKey";
    ArrayList<Preset> arrayListPresets;
    private BluetoothHandler mHandler;
    private BluetoothHandler.ConnectedThread readWrite;
    private RadioGroup radioCupGroup;
    private RadioGroup timeToMakeRadio;
    private EditText timeField;
    private TextView textDate;
    private EditText nameOfPreset;
    private ArrayAdapter<Preset> itemsAdapter;
    private Calendar date = Calendar.getInstance();
    private Button btnStepFor;
    private Button btnStepBack;
    private Gson gson;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean listSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);

        pref = getPreferences(Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();

        arrayListPresets = readArrayList(PRESET_KEY);
        Calendar date = Calendar.getInstance();

        /*String sampleDate = DATE_FORMAT.format(date.getTime());
        Preset first = new Preset("Coffee every morning", 1, sampleDate);
        Preset second = new Preset("Coffee for 2 in lunch", 2, sampleDate);
        Preset third = new Preset("Coffee in an hour", 1, sampleDate);

        arrayListPresets.addAll(Arrays.asList(first, second, third));*/

        // Create a regular adapter for the preset list
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListPresets);

        ImageButton oneCupButton = (ImageButton) findViewById(R.id.oneCupButton);
        ImageButton twoCupsButton = (ImageButton) findViewById(R.id.twoCupsButton);
        ImageButton heatUpButton = (ImageButton) findViewById(R.id.heatUp);
        Button presetButton = (Button) findViewById(R.id.setPresetButton);

        ListView listView = (ListView) findViewById(R.id.presetList);
        listView.setAdapter(itemsAdapter);
        // Delete if you press and hold a list item
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(this);

        oneCupButton.setOnClickListener(this);
        twoCupsButton.setOnClickListener(this);
        heatUpButton.setOnClickListener(this);
        presetButton.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        mHandler = ((BackgroundConnection) this.getApplicationContext()).getConnectionHandler();

        // kGet read and write thread
        readWrite = mHandler.getReadWriteThread();
        // set activity to handler so you can call methods from thread in this activity
        mHandler.setCoffeeActivity(this);
        // Get extras from the previous activity that send extras to this one
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
                readWrite.write("o".getBytes());
                break;
            case R.id.twoCupsButton:
                readWrite.write("t".getBytes());
                break;
            case R.id.heatUp:
                readWrite.write("h".getBytes());
                break;
            case R.id.setPresetButton:
                // Opening the dialog - to set presets
                @SuppressLint("InflateParams") final View dialogLayout = getLayoutInflater().inflate(R.layout.preset_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Create a preset");
                builder.setView(dialogLayout);
                // If you press outside of the dialog it won't close
                builder.setCancelable(false);
                date = Calendar.getInstance();

                nameOfPreset = (EditText) dialogLayout.findViewById(R.id.nameOfPreset);
                radioCupGroup = (RadioGroup) dialogLayout.findViewById(R.id.radioGroupCup);
                timeToMakeRadio = (RadioGroup) dialogLayout.findViewById(R.id.time_to_makeRadio);
                textDate = (TextView) dialogLayout.findViewById(R.id.date);
                timeField = (EditText) dialogLayout.findViewById(R.id.time);
                btnStepFor = (Button) dialogLayout.findViewById(R.id.stepDateForBtn);
                btnStepBack = (Button) dialogLayout.findViewById(R.id.stepDateBackBtn);

                // Get info from radio buttons
                timeToMakeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // Toggle to show or hide date, hide if you choose "Every day"
                        if (checkedId == R.id.radioButtonEvery) {
                            textDate.setVisibility(View.INVISIBLE);
                            btnStepFor.setVisibility(View.INVISIBLE);
                            btnStepBack.setVisibility(View.INVISIBLE);
                        } else if (checkedId == R.id.radioButtonSelected) {
                            textDate.setVisibility(View.VISIBLE);
                            btnStepFor.setVisibility(View.VISIBLE);
                            btnStepBack.setVisibility(View.VISIBLE);
                        }
                    }
                });

                textDate.setText(DATE_FORMAT.format(date.getTime()));

                // Increment date
                btnStepFor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        date.add(Calendar.DATE, 1);
                        textDate.setText(DATE_FORMAT.format(date.getTime()));
                    }
                });

                // Decrement date by adding -1
                btnStepBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (date.after(Calendar.getInstance())) {
                            date.add(Calendar.DATE, -1);
                        }
                        textDate.setText(DATE_FORMAT.format(date.getTime()));
                    }
                });

                builder.setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameOfPreset.getText().toString();

                        int selectedRadioButton = radioCupGroup.getCheckedRadioButtonId();
                        // If the selected radio button == 1 cup, send 1, else 2
                        int cup = (selectedRadioButton == R.id.radioButtonOneCup) ? 1 : 2;

                        String sampleDate = DATE_FORMAT.format(date.getTime());
                        Preset current = new Preset(name, cup, sampleDate);

                        // Make sure everything is filled out/chosen
                        if (name.equals("") || selectedRadioButton == -1) {
                            Toast.makeText(dialogLayout.getContext(), "Name not written or number of cups not set.", Toast.LENGTH_LONG).show();
                        } else {
                            arrayListPresets.add(current);
                            listSave = true;
                            itemsAdapter.notifyDataSetChanged();
                        }
                    }
                });

                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                // Show dialog after defining how it should be
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                break;
        }
    }

    private ArrayList<Preset> readArrayList(String key) {
        ArrayList<Preset> list;
        String favouriteJson = pref.getString(key, "");
        if (!favouriteJson.equals("")) {
            Type type = new TypeToken<List<Preset>>() {
            }.getType();
            list = gson.fromJson(favouriteJson, type);
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    private void saveArrayList(ArrayList<Preset> list, String key) {
        editor.putString(key, gson.toJson(list));
        editor.commit();
        listSave = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listSave) {
            this.saveArrayList(arrayListPresets, PRESET_KEY);
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
        //Here we can add the option to edit a preset/disable it/enable it
    }

    // If you press and hold an item you delete it
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        itemsAdapter.remove(itemsAdapter.getItem(position));
        listSave = true;
        itemsAdapter.notifyDataSetChanged();
        return true;
    }
}