package com.test.sombrero.testbluetoothapp.Exceptions;

import android.app.Activity;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private final Activity myContext;

    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        /*AlertDialog.Builder errorMessage  = new AlertDialog.Builder(myContext);
        errorMessage.setMessage(exception.getMessage());
        errorMessage.setTitle("Error!");
        errorMessage.setCancelable(false);
        errorMessage.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //dialog.dismiss();
                myContext.finish();
            }
        });
        errorMessage.create().show();

        //android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(-1);*/
    }

}
