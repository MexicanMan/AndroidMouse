package com.example.sombrero.bluem;

import android.app.Application;

import com.example.sombrero.bluem.Utils.MouseConfigSingleton;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MouseConfigSingleton.initInstance();
    }

}
