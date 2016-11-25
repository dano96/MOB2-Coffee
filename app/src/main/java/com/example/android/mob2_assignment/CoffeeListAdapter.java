package com.example.android.mob2_assignment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CoffeeListAdapter extends ArrayAdapter<BluetoothDevice> {

    public CoffeeListAdapter(Activity context, ArrayList<BluetoothDevice> list) {
        super(context, R.layout.item_list_layout, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        // new class that contains name and address textview
        ViewHolder holder;

        // If the item on the list was not created then
        if (item == null) {
            // Use item_listÖlayout.xml to create a java object to put in the list
            item = LayoutInflater.from(getContext()).inflate(R.layout.item_list_layout, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) item.findViewById(R.id.machineName);
            holder.address = (TextView) item.findViewById(R.id.machineAddress);
            // Saving the holder information in the view (one "line" in the list of devices)
            item.setTag(holder);
        } else {
            // If item was created, get the item
            holder = (ViewHolder) item.getTag();
        }
        // Get the devices to put in the list
        BluetoothDevice device = getItem(position);

        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        return item;
    }
    // This class holds this data so we don't have to find id all the time
    private static class ViewHolder {
        TextView name, address;
    }
}