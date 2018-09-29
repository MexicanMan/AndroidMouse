package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;

public class MouseViewModel extends ViewModel {

    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private BaseEventListener sensorListener;

    public MouseViewModel() {

    }

}
