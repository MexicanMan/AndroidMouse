package com.test.sombrero.testgyroapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.test.sombrero.testgyroapp.Activities.BaseActivity;

public class GyroEventListener implements SensorEventListener {

    private BaseActivity outputActivity;

    public GyroEventListener(BaseActivity activity) {
        outputActivity = activity;
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

        outputActivity.updateGyroValues(axisX, axisY, axisZ);
    }
}
