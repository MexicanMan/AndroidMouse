package com.example.sombrero.bluem.Utils;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;

import java.io.Serializable;

public class MouseConfig implements Serializable {

    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private BaseEventListener sensorListener;

    public MouseConfig(BluetoothManager.ConnectedWriteThread thread, BaseEventListener sensor) {
        bluetoothWriteThread = thread;
        sensorListener = sensor;
    }

    public BluetoothManager.ConnectedWriteThread getBluetoothWriteThread() {
        return bluetoothWriteThread;
    }

    public void setBluetoothWriteThread(BluetoothManager.ConnectedWriteThread bluetoothWriteThread) {
        this.bluetoothWriteThread = bluetoothWriteThread;
    }

    public BaseEventListener getSensorListener() {
        return sensorListener;
    }

    public void setSensorListener(BaseEventListener sensorListener) {
        this.sensorListener = sensorListener;
    }

}
