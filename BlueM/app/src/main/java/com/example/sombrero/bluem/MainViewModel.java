package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.SensorType;
import com.example.sombrero.bluem.Utils.MyMutableLiveData;

import java.util.ArrayList;
import java.util.Set;

public class MainViewModel extends ViewModel {

    ///region Fields

    ///region ActivityScreen

    private MyMutableLiveData<String> activityScreen;
    public MyMutableLiveData<String> getActivityScreen() {
        return activityScreen;
    }

    ///endregion

    ///region ToastMessage

    private MyMutableLiveData<String> toastMessage;
    public MyMutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    ///endregion

    ///region PairedDevices

    private MyMutableLiveData<ArrayList<BluetoothDevice>> pairedDevices;
    public MyMutableLiveData<ArrayList<BluetoothDevice>> getPairedDevices() {
        return pairedDevices;
    }

    ///endregion

    ///region SensorType

    private MyMutableLiveData<SensorType> sensorType;
    public MyMutableLiveData<SensorType> getSensorType() {
        return sensorType;
    }

    ///endregion

    private int choosedDeviceNumber = -1;
    private BluetoothManager bluetoothManager;

    ///endregion

    public MainViewModel() {
        activityScreen = new MyMutableLiveData<>();
        toastMessage = new MyMutableLiveData<>();
        pairedDevices = new MyMutableLiveData<>();
        sensorType = new MyMutableLiveData<>();
        sensorType.setValue(SensorType.GYRO);

        // BluetoothManager initiation and configuration
        bluetoothManager = new BluetoothManager();
        if (!bluetoothManager.getIsBluetoothEnabled()) {
            activityScreen.setValue("BluetoothReq");
        } else {
            bluetoothLoadPairedDevices();
        }
    }

    public void bluetoothLoadPairedDevices() {
        Set<BluetoothDevice> devices = bluetoothManager.loadPairedDevices();
        pairedDevices.setValue(new ArrayList<>(devices));
    }

    public void onPairedDevicesItemChosen(int position) {
        choosedDeviceNumber = position;
    }

    public void onSensorTypeChanged(SensorType type) {
        sensorType.setValue(type);
    }

    public void connectToBluetoothDevice() {
        if (choosedDeviceNumber != -1)
            bluetoothManager.connect(pairedDevices.getValue().get(choosedDeviceNumber));
        else
            toastMessage.setValue("Choose device first!");
    }

}
