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
    private final static double GYRO_EPS = 1e-1;
    private final static double ACCEL_EPS = 0.5;

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

    private float[] currAccel;

    public MouseViewModel(@NonNull Application application) {
        super(application);

        toastMessage = new MyMutableLiveData<>();
        xAxisValue = new MyMutableLiveData<>();
        yAxisValue = new MyMutableLiveData<>();
        zAxisValue = new MyMutableLiveData<>();

        currAccel = new float[2];
        currAccel[0] = 0.0f;
        currAccel[1] = 0.0f;

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

                char[] direction = new char[3];
                int accel;
                switch (sensorListener.sensorType) {
                    case Sensor.TYPE_ROTATION_VECTOR:
                        if (values[1] > GYRO_EPS)
                            direction[0] = 'u';
                        else if (values[1] < -GYRO_EPS)
                            direction[0] = 'd';
                        else
                            direction[0] = 'n';

                        if (values[0] > GYRO_EPS)
                            direction[1] = 'l';
                        else if (values[0] < -GYRO_EPS)
                            direction[1] = 'r';
                        else
                            direction[1] = 'n';

                        accel = (int) (Math.max(Math.abs(values[0]), Math.abs(values[1])) * 10);
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        if (Math.abs(values[0]) < 0.3 && Math.abs(values[1]) < 0.3) {
                            currAccel[0] = 0;
                            currAccel[1] = 0;
                        } else {
                            currAccel[0] += values[0]*10;
                            currAccel[1] += values[1]*10;
                        }

                        if (currAccel[1] > ACCEL_EPS)
                            direction[0] = 'u';
                        else if (currAccel[1] < -ACCEL_EPS)
                            direction[0] = 'd';
                        else
                            direction[0] = 'n';

                        if (currAccel[0] > ACCEL_EPS)
                            direction[1] = 'l';
                        else if (currAccel[0] < -ACCEL_EPS)
                            direction[1] = 'r';
                        else
                            direction[1] = 'n';

                        accel = (int) (Math.max(Math.abs(values[0]), Math.abs(values[1])) + 1);
                        break;
                    default:
                        accel = 1;
                        break;
                }
                if (accel > 9)
                    accel = 9;
                direction[2] = (char) (accel + '0');

                xAxisValue.setValue(Float.toString(values[0]) + " - " + direction[1]);
                yAxisValue.setValue(Float.toString(values[1]) + " - " + direction[0]);
                zAxisValue.setValue(Float.toString(values[2]) + " / " + direction[2]);

                bluetoothWriteThread.write(new String(direction).getBytes());
            }
        });
    }

    public void OnLeftClick()
    {
        bluetoothWriteThread.write("lk".getBytes());
    }

    public void OnRightClick()
    {
        bluetoothWriteThread.write("rt".getBytes());
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void OnActivityResume() {
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
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
