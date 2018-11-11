package com.example.sombrero.bluem.BluetoothWork;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.databinding.ObservableField;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.sombrero.bluem.Utils.MessageType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothManager {

    ///region Constants

    private final static int SOCK_CHANNEL = 1;

    ///endregion

    //region Fields

    //region IsBluetoothEnabled

    private boolean isBluetoothEnabled = true;
    public boolean getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    ///endregion
    ///region ErrorMsg

    private ObservableField<String> errorMsg;
    public ObservableField<String> getErrorMsg() {
        return errorMsg;
    }

    ///endregion
    ///region ResultMsg

    private ObservableField<String> resultMsg;
    public ObservableField<String> getResultMsg() {
        return resultMsg;
    }

    ///endregion
    ///region BluetoothWriteThread

    private ConnectedWriteThread bluetoothWriteThread;
    public ConnectedWriteThread getBluetoothWriteThread() {
        return bluetoothWriteThread;
    }

    ///endregion

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;

    ///endregion

    public BluetoothManager() {
        errorMsg = new ObservableField<>();
        resultMsg = new ObservableField<>();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new RuntimeException("This device does not support bluetooth!");
        }

        if (!bluetoothAdapter.isEnabled()) {
            isBluetoothEnabled = false;
        }

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MessageType.MSG_ERROR:
                        errorMsg.set((String) msg.obj);
                        break;
                    case MessageType.MSG_BASE:
                        resultMsg.set((String) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public Set<BluetoothDevice> loadPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    public void connect(BluetoothDevice device) {
        ConnectingThread connectingThread = new ConnectingThread(device);
        connectingThread.start();
    }

    private class ConnectingThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectingThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to socket, because socket is final.
            BluetoothSocket tmpSocket = null;
            this.device = device;

            try {
                int channel = SOCK_CHANNEL;  // Channel we are listening on
                Method m = device.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
                tmpSocket = (BluetoothSocket) m.invoke(device, channel);
            } catch (Exception e) {
                Log.e("ERROR", "Bluetooth socket's create() method failed", e);
                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Oops, can't create bluetooth socket! Restart app.");
                readMsg.sendToTarget();
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
                // Unable to connectToBluetoothDevice: close the socket and return.
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e("Error", "Could not close the client bluetooth socket", closeException);
                }

                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Oops, can't connect to device! Make sure you chose the right one!");
                readMsg.sendToTarget();
                return ;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ConnectedWriteThread thread = new ConnectedWriteThread(socket);
            bluetoothWriteThread = thread;
            Message readMsg = handler.obtainMessage(MessageType.MSG_BASE, -1, -1,
                    "Successfully connected to " + device.getName() + " with address " + device.getAddress() + "!");
            readMsg.sendToTarget();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Error", "Could not close the client bluetooth socket", e);
                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Could not close the client bluetooth socket! Restart app.");
                readMsg.sendToTarget();
            }
        }
    }

    public class ConnectedWriteThread extends Thread {
        private final BluetoothSocket socket;
        private final OutputStream outStream;

        public ConnectedWriteThread(BluetoothSocket socket) {
            this.socket = socket;
            OutputStream tmpOut = null;

            // Get the output streams, using temp objects because member stream are final.
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Error", "Error occurred when creating output stream", e);
                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Error occurred when creating output stream. Restart app!");
                readMsg.sendToTarget();
            }

            outStream = tmpOut;
        }

        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                Log.e("Error", "Error occurred when sending data through bluetooth socket", e);
                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Error occurred when sending data through bluetooth socket. Restart app and server!");
                readMsg.sendToTarget();
            }
        }

        // Call this method to shut down the connection.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Error", "Could not close the connectToBluetoothDevice socket", e);
                Message readMsg = handler.obtainMessage(MessageType.MSG_ERROR, -1, -1,
                        "Could not close the connectToBluetoothDevice socket! Restart app!");
                readMsg.sendToTarget();
            }
        }
    }

}
