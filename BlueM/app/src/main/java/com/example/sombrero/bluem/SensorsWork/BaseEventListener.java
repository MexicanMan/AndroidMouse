package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.example.sombrero.bluem.Activities.BaseActivity;

public abstract class BaseEventListener implements SensorEventListener {

    protected BaseActivity outputActivity;

    public BaseEventListener(BaseActivity activity) {
        outputActivity = activity;
    }

    @Override
    public abstract void onAccuracyChanged(Sensor sensor, int accuracy);

    @Override
    public abstract void onSensorChanged(SensorEvent event);
}
