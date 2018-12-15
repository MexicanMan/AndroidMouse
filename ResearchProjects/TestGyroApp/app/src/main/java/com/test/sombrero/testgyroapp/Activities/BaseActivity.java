package com.test.sombrero.testgyroapp.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.test.sombrero.testgyroapp.Exceptions.ExceptionHandler;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    public void updateGyroValues(float xValue, float yValue, float zValue) {
        throw new RuntimeException("There is no implementation for this method!");
    }

}
