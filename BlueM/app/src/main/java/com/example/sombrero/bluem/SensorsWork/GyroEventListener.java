package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

// not used for now - TODO for future: linear phone movement (imitation of real mouse)
public class GyroEventListener extends BaseEventListener {

    private float[] rotationMatrixFromVector;
    private float[] rotationMatrix;
    private float[] orientationVals;

    public GyroEventListener() {
        super(Sensor.TYPE_ROTATION_VECTOR);

        rotationMatrixFromVector = new float[16];
        rotationMatrix = new float[16];
        orientationVals = new float[3];
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
