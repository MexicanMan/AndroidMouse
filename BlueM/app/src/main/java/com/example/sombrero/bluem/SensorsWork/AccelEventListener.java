package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class AccelEventListener extends BaseEventListener {

    public AccelEventListener() {
        super(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != sensorType)
            return;

        axisValues.set(event.values);
        axisValues.notifyPropertyChanged(0);
    }
}
