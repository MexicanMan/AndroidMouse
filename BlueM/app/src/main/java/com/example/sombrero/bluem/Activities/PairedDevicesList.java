package com.example.sombrero.bluem.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sombrero.bluem.R;

import java.util.ArrayList;

public class PairedDevicesList extends ArrayAdapter<BluetoothDevice> {

    private final Activity context;
    private ArrayList<BluetoothDevice> devices;

    public PairedDevicesList(Activity context,ArrayList<BluetoothDevice> devices) {
        super(context, R.layout.paired_device_item, devices);
        this.context = context;
        this.devices = devices;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.paired_device_item, null, true);

        TextView deviceName = rowView.findViewById(R.id.deviceName);
        TextView deviceHardwareAddress = rowView.findViewById(R.id.deviceHardwareAddress);
        deviceName.setText(devices.get(position).getName());
        deviceHardwareAddress.setText(devices.get(position).getAddress());

        return rowView;
    }

    public void AddDevice(BluetoothDevice device) {
        devices.add(device);
    }

    @Nullable
    @Override
    public BluetoothDevice getItem(int position) {
        return super.getItem(position);
    }
}
