package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.Utils.MyMutableLiveData;

import java.util.ArrayList;
import java.util.Set;

public class MainViewModel extends ViewModel implements Observable {

    ///region Fields

    ///region ActivityScreen

    private MyMutableLiveData<String> activityScreen;
    public MyMutableLiveData<String> getActivityScreen() {
        return activityScreen;
    }

    ///endregion

    ///region PairedDevices

    private MyMutableLiveData<ArrayList<BluetoothDevice>> pairedDevices;
    public MyMutableLiveData<ArrayList<BluetoothDevice>> getPairedDevices() {
        return pairedDevices;
    }

    ///endregion

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private BluetoothManager bluetoothManager;

    ///endregion

    public MainViewModel() {
        activityScreen = new MyMutableLiveData<>();
        pairedDevices = new MyMutableLiveData<>();
        pairedDevices.setValue(new ArrayList<>());

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

    public void onPairedDevicesItemChosen() {

    }

    ///region ObservableInterface

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    private void notifyChange() {
        callbacks.notifyChange(this, 0);
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyChange(this,fieldId);
    }

    ///endregion

}
