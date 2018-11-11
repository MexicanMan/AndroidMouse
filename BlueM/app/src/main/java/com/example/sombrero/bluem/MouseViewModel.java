package com.example.sombrero.bluem;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.databinding.Observable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.example.sombrero.bluem.BluetoothWork.BluetoothManager;
import com.example.sombrero.bluem.SensorsWork.BaseEventListener;
import com.example.sombrero.bluem.Utils.MouseConfigSingleton;
import com.example.sombrero.bluem.Utils.MyMutableLiveData;

import static android.content.Context.SENSOR_SERVICE;

public class MouseViewModel extends AndroidViewModel implements LifecycleObserver {

    ///region Constants

    private final static String BYE_MESSAGE = "BYE";

    ///endregion

    ///region AxisValues

    private MyMutableLiveData<String> xAxisValue;
    private MyMutableLiveData<String> yAxisValue;
    private MyMutableLiveData<String> zAxisValue;
    public MyMutableLiveData<String> getXAxisValue() {
        return xAxisValue;
    }
    public MyMutableLiveData<String> getYAxisValue() {
        return yAxisValue;
    }
    public MyMutableLiveData<String> getZAxisValue() {
        return zAxisValue;
    }

    ///endregion
    ///region ToastMessage

    private MyMutableLiveData<String> toastMessage;
    public MyMutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    ///endregion

    private SensorManager sensorManager;
    private Sensor sensor;
    private BluetoothManager.ConnectedWriteThread bluetoothWriteThread;
    private BaseEventListener sensorListener;

    public MouseViewModel(@NonNull Application application) {
        super(application);

        toastMessage = new MyMutableLiveData<>();
        xAxisValue = new MyMutableLiveData<>();
        yAxisValue = new MyMutableLiveData<>();
        zAxisValue = new MyMutableLiveData<>();

        MouseConfigSingleton mouseConfigSingleton = MouseConfigSingleton.getInstance();
        bluetoothWriteThread = mouseConfigSingleton.getBluetoothWriteThread();
        sensorListener = mouseConfigSingleton.getSensorListener();

        sensorManager = (SensorManager) getApplication().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorListener.sensorType);
        if (sensor == null) {
            toastMessage.setValue("This device does not support gyroscope sensors!");
            //throw new RuntimeException("This device does not support gyroscope sensors!");
        }

        sensorListener.getAxisValues().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                float[] values = sensorListener.getAxisValues().get();
                xAxisValue.setValue(Float.toString(values[0]));
                yAxisValue.setValue(Float.toString(values[1]));
                zAxisValue.setValue(Float.toString(values[2]));

                String xyValues = Float.toString(values[0]) + " " + Float.toString(values[1]);
                bluetoothWriteThread.write(xyValues.getBytes());
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void OnActivityResume() {
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void OnActivityPause() {
        sensorManager.unregisterListener(sensorListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void OnActivityDestroy() {
        bluetoothWriteThread.write(BYE_MESSAGE.getBytes());
        bluetoothWriteThread.cancel();
    }

}
