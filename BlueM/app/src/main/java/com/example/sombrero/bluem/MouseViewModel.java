package com.example.sombrero.bluem;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModel;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;
import com.example.sombrero.bluem.Utils.MouseConfigSingleton;

public class MouseViewModel extends ViewModel implements LifecycleObserver {

    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private BaseEventListener sensorListener;

    public MouseViewModel() {
        MouseConfigSingleton mouseConfigSingleton = MouseConfigSingleton.getInstance();
        bluetoothWriteThread = mouseConfigSingleton.getBluetoothWriteThread();
        sensorListener = mouseConfigSingleton.getSensorListener();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void OnActivityDestroy() {
        bluetoothWriteThread.cancel();
    }

}
