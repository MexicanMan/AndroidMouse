package com.test.sombrero.testbluetoothapp.Activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.test.sombrero.testbluetoothapp.BluetoothWork.BluetoothManager;
import com.test.sombrero.testbluetoothapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainActivity extends BaseActivity {

    //region Constants

    private final static int REQUEST_ENABLE_BT = 1;

    //endregion

    //region ActivityComponents

    @BindView(R.id.pairedDevicesListView)
    protected ListView pairedDevices;

    //endregion

    private BluetoothManager bluetoothManager;
    private PairedDevicesList pairedDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ArrayList<BluetoothDevice> devices = new ArrayList<>();
        pairedDevicesAdapter = new PairedDevicesList(this, devices);
        pairedDevices.setAdapter(pairedDevicesAdapter);

        bluetoothManager = new BluetoothManager(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK)
                    finish();
                else
                    bluetoothManager.loadPairedDevices();
                break;
            default:
                throw new RuntimeException("Unknown requestCode!");
        }
    }

    @OnItemClick(R.id.pairedDevicesListView)
    protected void onPairedDeviceClicked(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = pairedDevicesAdapter.getItem(position);
        bluetoothManager.connect(device);
    }

    @Override
    public void showEnableBtDialog(Intent enableBtIntent) {
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void outputFoundedDevices(BluetoothDevice device) {
        pairedDevicesAdapter.AddDevice(device);
        pairedDevicesAdapter.notifyDataSetChanged();
    }

}
