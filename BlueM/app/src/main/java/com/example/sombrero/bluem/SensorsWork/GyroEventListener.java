package com.example.sombrero.bluem.SensorsWork;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

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
        /*// Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);
        SensorManager.remapCoordinateSystem(rotationMatrixFromVector, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);
        SensorManager.getOrientation(rotationMatrix, orientationVals);

        // Optionally convert the result from radians to degrees
        orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
        orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
        orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);*/

        axisValues.set(event.values);
        axisValues.notifyPropertyChanged(0);
    }

}
