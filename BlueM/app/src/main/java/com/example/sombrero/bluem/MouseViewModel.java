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
    private final static int MAX_COUNT = 4;
    private final static double GYRO_EPS = 1e-1;
    private final static double ACCEL_EPS = 1.2;

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
    private boolean isXInverted;
    private boolean isYInverted;
    private int count;
    private float[] tempSensorValues;

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
        isXInverted = mouseConfigSingleton.getIsXInverted();
        isYInverted = mouseConfigSingleton.getIsYInverted();

        count = 0;
        tempSensorValues = new float[]{0, 0};

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

                if (count < MAX_COUNT) {
                    count++;
                    tempSensorValues[0] += values[0];
                    tempSensorValues[1] += values[1];
                }

                if (count == MAX_COUNT) {
                    char[] direction = new char[3];
                    int accel;

                    tempSensorValues[0] /= MAX_COUNT;
                    tempSensorValues[1] /= MAX_COUNT;

                    switch (sensorListener.sensorType) {
                        case Sensor.TYPE_ACCELEROMETER:
                            if (tempSensorValues[1] > ACCEL_EPS)
                                direction[0] = isYInverted ? 'd' : 'u';
                            else if (tempSensorValues[1] < -ACCEL_EPS)
                                direction[0] = isYInverted ? 'u' : 'd';
                            else
                                direction[0] = 'n';

                            if (tempSensorValues[0] < -ACCEL_EPS)
                                direction[1] = isXInverted ? 'l' : 'r';
                            else if (tempSensorValues[0] > ACCEL_EPS)
                                direction[1] = isXInverted ? 'r' : 'l';
                            else
                                direction[1] = 'n';

                            accel = (int) Math.max(Math.abs(tempSensorValues[0]), Math.abs(tempSensorValues[1]));
                            break;
                        case Sensor.TYPE_ROTATION_VECTOR: // not used for now -> look GyroEventListener
                            if (tempSensorValues[1] > GYRO_EPS)
                                direction[0] = 'u';
                            else if (tempSensorValues[1] < -GYRO_EPS)
                                direction[0] = 'd';
                            else
                                direction[0] = 'n';

                            if (tempSensorValues[0] > GYRO_EPS)
                                direction[1] = 'l';
                            else if (tempSensorValues[0] < -GYRO_EPS)
                                direction[1] = 'r';
                            else
                                direction[1] = 'n';

                            accel = (int) (Math.max(Math.abs(tempSensorValues[0]), Math.abs(tempSensorValues[1])) * 10);
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
                    count = 0;
                    tempSensorValues[0] = 0;
                    tempSensorValues[1] = 0;
                }
            }
        });
    }

    public void OnLeftClick()
    {
        bluetoothWriteThread.write("lk".getBytes());
    }

    public void OnRightClick()
    {
        bluetoothWriteThread.write("rk".getBytes());
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
