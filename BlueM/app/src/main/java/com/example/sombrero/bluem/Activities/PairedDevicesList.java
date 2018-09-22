package com.example.sombrero.bluem.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.sombrero.bluem.R;
import com.example.sombrero.bluem.databinding.PairedDeviceItemBinding;

import java.util.ArrayList;

public class PairedDevicesList extends ArrayAdapter<BluetoothDevice> {

    private final Context context;
    private ArrayList<BluetoothDevice> devices;

    public PairedDevicesList(Activity context, ArrayList<BluetoothDevice> devices) {
        super(context, R.layout.paired_device_item, devices);
        this.context = context;
        this.devices = devices;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PairedDeviceItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.paired_device_item, parent,false);
        binding.setDevice(devices.get(position));

        return binding.getRoot();
    }

    @Nullable
    @Override
    public BluetoothDevice getItem(int position) {
        return super.getItem(position);
    }
}
