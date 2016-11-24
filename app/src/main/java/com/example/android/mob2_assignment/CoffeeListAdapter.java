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
        ViewHolder holder;

        if (item == null) {
            item = LayoutInflater.from(getContext()).inflate(R.layout.item_list_layout, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) item.findViewById(R.id.machineName);
            holder.address = (TextView) item.findViewById(R.id.machineAddress);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }
        BluetoothDevice device = getItem(position);

        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        return item;
    }

    private static class ViewHolder {
        TextView name, address;
    }
}