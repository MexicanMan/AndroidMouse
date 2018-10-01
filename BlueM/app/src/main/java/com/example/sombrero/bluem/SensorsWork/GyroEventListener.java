package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class GyroEventListener extends BaseEventListener {

    public GyroEventListener() {
        super(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        axisValues.set(event.values);
        axisValues.notifyPropertyChanged(0);
    }

}
