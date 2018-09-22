package com.example.sombrero.bluem.BluetoothWork;

import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.sombrero.bluem.Exceptions.BluetoothOffException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothManager {

    private boolean isBluetoothEnabled = true;
    public boolean getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    private BluetoothAdapter bluetoothAdapter;

    private Handler mHandler;

    public BluetoothManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new RuntimeException("This device does not support bluetooth!");
        }

        if (!bluetoothAdapter.isEnabled()) {
            isBluetoothEnabled = false;
        }
    }

    public Set<BluetoothDevice> loadPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    public void connect(BluetoothDevice device) {
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to socket
            // because socket is final.
            BluetoothSocket tmpSocket = null;
            this.device = device;

            try {
                int channel = 1; // substitute with channel you are listening on
                Method m = device.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
                tmpSocket = (BluetoothSocket) m.invoke(device, channel);

                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                //tmpSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (Exception e) {
                Log.e("ERROR", "Socket's create() method failed", e);
            }

            socket = tmpSocket;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e("Error", "Could not close the client socket", closeException);
                }

                return ;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ConnectedThread thread = new ConnectedThread(socket);
            String t = "pupa";
            for (int i=0; i < 5; i++) {
                thread.write(t.getBytes());
                try {
                    sleep(3000);
                }catch(Exception e)
                { }
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Error", "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inStream;
        private final OutputStream outStream;
        private byte[] buffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("Error", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Error", "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run() {
            buffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = inStream.read(buffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            0, numBytes, -1,
                            buffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d("Error", "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);

                // Share the sent message with the UI activity.
                /*Message writtenMsg = mHandler.obtainMessage(
                        1, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();*/
            } catch (IOException e) {
                Log.e("Error", "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(2);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Error", "Could not close the connect socket", e);
            }
        }
    }

}
