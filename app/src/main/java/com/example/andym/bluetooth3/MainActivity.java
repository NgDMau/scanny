package com.example.andym.bluetooth3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView notification, list_devices, status;
    private Button check_blue, connect_blue, receive_blue;
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothDevice mDEVICE = null ;
    private static final String NAME = "bluetooth_3_name";
    private static final String TAG  = "tag_of_my_app";
    private static final UUID MY_UUID= UUID.fromString("507096b2-7500-11e8-adc0-fa7ae01bbebc");
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notification = findViewById(R.id.notification);
        list_devices = findViewById(R.id.list_devices);
        status       = findViewById(R.id.status);

        if (mBluetoothAdapter == null) {
            notification.setText("Bluetooth is not supported");
        } else notification.setText("Bluetooth is supported");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mDEVICE = device;

            }
        }

      /*  IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); */
      makeDiscoverable();

    }

  /*private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    }; */

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //Found, add to a device list
            }
        }
    };


    /*@Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    } */


    public void bonded_devices(View view){
        if(mDEVICE != null) list_devices.setText(mDEVICE.getName()+"\n"+mDEVICE.getAddress());
        else list_devices.setText("No paired devices !");
    }

    public void connect_devices_actively(View view) throws Exception {
        ConnectThread mConnectThread = new ConnectThread(mDEVICE);
        mConnectThread.start();
    }

    public void connect_devices_passively(View view){
        AcceptThread mAcceptThread   = new AcceptThread();
        mAcceptThread.start();

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID);
            } catch (IOException e){
                Log.e(TAG,"listen() failed!",e);
            }
            mmServerSocket = tmp;
            if(mmServerSocket != null) status.setText("receiving...");
        }

        public void run(){
            BluetoothSocket socket;
            while (true){
                try{
                    socket = mmServerSocket.accept();
                } catch (IOException e){
                    Log.e(TAG,"accept() failed!",e);
                    break;
                }
                if(socket == null) status.setText("socket is null!!");
                else {runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("device accepted !!");
                    }
                });

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel(){
            status.setText("cancel done !");
            try{
                mmServerSocket.close();
            } catch (IOException e){
                Log.e(TAG,"Couldn't close the connect socket",e);
            }
        }
    }

    private class ConnectThread extends Thread{
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;


        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmp = null;
            mmDevice = device;

            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e){
                Log.e(TAG, "create() failed", e);
            }

            mmSocket = tmp;
            if(mmSocket != null) status.setText("connecting...");
        }

        public void run(){
            mBluetoothAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            } catch (IOException connectException){
                try{
                    mmSocket.close();
                } catch (IOException closeException){
                    Log.e(TAG,"closing socket failed!!",closeException);
                }
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    status.setText("connected device successfully !!!");
                }
            });

        }

        public void cancel(){
            status.setText("cancel done !");
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG,"closing client socket failed!!", e);
            }
        }
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }




}
