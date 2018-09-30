package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.databinding.Observable;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.AccelEventListener;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;
import com.example.sombrero.bluem.SensorsWork.GyroEventListener;
import com.example.sombrero.bluem.SensorsWork.SensorType;
import com.example.sombrero.bluem.Activities.ActivityScreenType;
import com.example.sombrero.bluem.Utils.MouseConfigSingleton;
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

    private MouseConfigSingleton mouseConfigSingleton;
    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private int choosedDeviceNumber = -1;
    private BluetoothManager bluetoothManager;

    ///endregion

    public MainViewModel() {
        activityScreen = new MyMutableLiveData<>();
        toastMessage = new MyMutableLiveData<>();
        pairedDevices = new MyMutableLiveData<>();
        sensorType = new MyMutableLiveData<>();
        sensorType.setValue(SensorType.GYRO);
        mouseConfigSingleton = MouseConfigSingleton.getInstance();

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

        // On bluetooth connection successful
        bluetoothManager.getResultMsg().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                bluetoothWriteThread = bluetoothManager.getBluetoothWriteThread();
                toastMessage.setValue(bluetoothManager.getResultMsg().get());
                setupMouse();
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

    private void setupMouse() {
        BaseEventListener sensor;
        if (sensorType.getValue() == SensorType.GYRO)
            sensor = new GyroEventListener();
        else
            sensor = new AccelEventListener();

        mouseConfigSingleton.setBluetoothWriteThread(bluetoothWriteThread);
        mouseConfigSingleton.setSensorListener(sensor);
        activityScreen.setValue(ActivityScreenType.MOUSE_SCREEN);
    }

}
