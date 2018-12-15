package com.example.sombrero.bluem.Activities;

import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sombrero.bluem.MainViewModel;
import com.example.sombrero.bluem.R;
import com.example.sombrero.bluem.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    //region Constants

    private final static int REQUEST_ENABLE_BT = 1;
    private final static String SHOW_MOUSE_SCREEN = "com.example.sombrero.bluem.SHOW_MOUSE_SCREEN";

    //endregion

    ///region Fields

    private MainViewModel mainViewModel;
    private PairedDevicesList pairedDevicesAdapter;

    ///endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MainViewModel initiation
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setDataContext(mainViewModel);
        binding.setLifecycleOwner(this);

        // PairedDevicesList initiation and configuration
        pairedDevicesAdapter = new PairedDevicesList(this, new ArrayList<BluetoothDevice>());
        binding.pairedDevicesListView.setAdapter(pairedDevicesAdapter);
        binding.pairedDevicesListView.setOnItemClickListener((parent, view, position, id) -> {
            this.mainViewModel.onPairedDevicesItemChosen(position);
            parent.setSelection(position);
        });

        // MainViewModel event handlers configuration
        mainViewModel.getActivityScreen().observe(this, activityType -> {
                switch (activityType) {
                    case BLUETOOTH_REQ:
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        this.showEnableBtDialog(enableBtIntent);
                        break;
                    case MOUSE_SCREEN:
                        Intent intent = new Intent(SHOW_MOUSE_SCREEN);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            });
        mainViewModel.getToastMessage().observe(this, toastMessage -> {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        });
        mainViewModel.getPairedDevices().observe(this, pairedDevicesList -> {
            this.pairedDevicesAdapter.clear();
            this.pairedDevicesAdapter.addAll(pairedDevicesList);
        });
    }

    ///region BluetoothDialog setup

    public void showEnableBtDialog(Intent enableBtIntent) {
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK)
                    finish();
                else
                    mainViewModel.bluetoothLoadPairedDevices();
                break;
            default:
                throw new RuntimeException("Unknown requestCode!");
        }
    }

    ///endregion

}
