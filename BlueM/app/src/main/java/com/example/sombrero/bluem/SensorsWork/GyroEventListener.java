package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.example.sombrero.bluem.Activities.BaseActivity;

public class GyroEventListener extends BaseEventListener {

    public GyroEventListener(BaseActivity activity) {
        super(activity);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float axisX = event.values[0];
        float axisY = event.values[1];
        float axisZ = event.values[2];

        //outputActivity.updateGyroValues(axisX, axisY, axisZ);
    }

}
