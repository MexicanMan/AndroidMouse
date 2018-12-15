package com.example.sombrero.bluem.SensorsWork;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.example.sombrero.bluem.Activities.BaseActivity;

public abstract class BaseEventListener implements SensorEventListener {

    public final int sensorType;

    ///region AxisValues

    protected ObservableField<float[]> axisValues;
    public ObservableField<float[]> getAxisValues() {
        return axisValues;
    }

    ///endregion

    public BaseEventListener(int sensorType) {
        this.sensorType = sensorType;
        axisValues = new ObservableField<>();
    }

    @Override
    public abstract void onAccuracyChanged(Sensor sensor, int accuracy);

    @Override
    public abstract void onSensorChanged(SensorEvent event);
}
