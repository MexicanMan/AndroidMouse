package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.databinding.Observable;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.SensorType;
import com.example.sombrero.bluem.Utils.ActivityScreenType;
import com.example.sombrero.bluem.Utils.MyMutableLiveData;

import java.util.ArrayList;
import java.util.Set;

public class MainViewModel extends ViewModel {

    ///region Fields

    ///region ActivityScreen

    private MyMutableLiveData<ActivityScreenType> activityScreen;
    public MyMutableLiveData<ActivityScreenType> getActivityScreen() {
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
    ///region BluetoothWriteThread

    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    public BluetoothManager.ConnectedWriteThread getBluetoothWriteThread() {
        return bluetoothWriteThread;
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
            activityScreen.setValue(ActivityScreenType.BLUETOOTH_REQ);
        } else {
            bluetoothLoadPairedDevices();
        }

        // BluetoothManager error messages throwing to UI toasts
        bluetoothManager.getErrorMsg().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                toastMessage.setValue(bluetoothManager.getErrorMsg().get());
            }
        });

        bluetoothManager.getResultMsg().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                bluetoothWriteThread = bluetoothManager.getBluetoothWriteThread();
                toastMessage.setValue(bluetoothManager.getResultMsg().get());
                activityScreen.setValue(ActivityScreenType.MOUSE_SCREEN);
            }
        });
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
