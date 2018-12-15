package com.example.sombrero.bluem.Utils;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;

public class MouseConfigSingleton {

    private static MouseConfigSingleton instance;

    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private BaseEventListener sensorListener;
    private boolean isXInverted;
    private boolean isYInverted;

    private MouseConfigSingleton() {  }

    public static void initInstance() {
        if (instance == null) {
            instance = new MouseConfigSingleton();
        }
    }

    public static MouseConfigSingleton getInstance() {
        return instance;
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

    public void setXInverted(boolean XInverted) {
        isXInverted = XInverted;
    }

    public void setYInverted(boolean YInverted) {
        isYInverted = YInverted;
    }

    public boolean getIsXInverted() {
        return isXInverted;
    }

    public boolean getIsYInverted() {
        return isYInverted;
    }

}
