package com.test.sombrero.testgyroapp.Activities;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.test.sombrero.testgyroapp.GyroEventListener;
import com.test.sombrero.testgyroapp.R;

public class MainActivity extends BaseActivity {

    //region ActivityComponents

    @BindView(R.id.gyroValueX)
    protected TextView gyroValueX;

    @BindView(R.id.gyroValueY)
    protected TextView gyroValueY;

    @BindView(R.id.gyroValueZ)
    protected TextView gyroValueZ;

    //endregion

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private GyroEventListener gyroEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        gyroEventListener = new GyroEventListener(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroSensor == null) {
            throw new RuntimeException("This device does not support gyroscope sensors");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroEventListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroEventListener);
    }

    @Override
    public void updateGyroValues(float xValue, float yValue, float zValue) {
        gyroValueX.setText(Float.toString(xValue));
        gyroValueY.setText(Float.toString(yValue));
        gyroValueZ.setText(Float.toString(zValue));
    }

}
