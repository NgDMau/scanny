package com.example.andym.bluetooth3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BluetoothChatService {

    private static final String TAG    = "BluetoothChatService";

    private static final String NAME   = "BluetoothChatSecure";
    private static final UUID MY_UUID  =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothAdapter mAdapter = null;
    private final Handler mHandler = null ;
    private TextView notification, list_devices;



    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try{
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME,MY_UUID);
            } catch (IOException e){
                Log.e(TAG,"listen() failed",e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            while (true){
                try{
                    socket = mmServerSocket.accept();
                }catch (IOException e){
                    Log.e(TAG,"accept() failed",e);
                    break;
                }

                if(socket != null){
                    // need connected function here

                    break;
                }
            }
        }

        public void cancel(){
            Log.d(TAG, "Socket Type cancel");
            try{
                mmServerSocket.close();
            } catch (IOException e){
                Log.e(TAG,"close() failed", e);
            }
        }
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;

            try{
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e){
                Log.e(TAG,"create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run(){
            mAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            } catch (IOException e){
                try{
                    mmSocket.close();
                } catch (IOException e2){
                    Log.e(TAG, "close() failure");
                }
                return;
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG, "close() fialed", e);
            }
        }
    }

}
