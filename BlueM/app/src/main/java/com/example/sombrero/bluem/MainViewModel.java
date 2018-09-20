package com.example.sombrero.bluem;

import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.Exceptions.BluetoothOffException;
import com.example.sombrero.bluem.Utils.MyMutableLiveData;

import java.util.Set;

public class MainViewModel extends ViewModel implements Observable {

    ///region Fields

    ///region ActivityScreen

    private MyMutableLiveData<String> activityScreen;
    public MyMutableLiveData<String> getActivityScreen() {
        return activityScreen;
    }

    ///endregion

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private BluetoothManager bluetoothManager;

    ///endregion

    public MainViewModel() {
        activityScreen = new MyMutableLiveData<>();

        try {
            bluetoothManager = new BluetoothManager();
            BluetoothLoadPairedDevices();
        } catch (BluetoothOffException e) {
            activityScreen.setValue("BluetoothReq");
        }
    }

    public void BluetoothLoadPairedDevices() {
        Set<BluetoothDevice> devices = bluetoothManager.LoadPairedDevices();
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
