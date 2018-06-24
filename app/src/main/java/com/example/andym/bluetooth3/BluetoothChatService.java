package com.example.andym.bluetooth3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatService {

    private static final String TAG1 = "MY_APP_DEBUG_TAG";
    private Handler mHandler;

    private interface MessageConstants{
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothSocket theSocket;

    public void SEND_MESSAGE(String message){
        byte[] send = message.getBytes();
        ConnectedThread mConnectedThread = new ConnectedThread(theSocket);
        mConnectedThread.start();
        mConnectedThread.write(send);
    }


    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
            } catch (IOException e){
                Log.e(TAG1, "Error occurred when creating input stream", e);
            }
            try{
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                Log.e(TAG1, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream= tmpOut;
        }

        public void run(){
            mmBuffer = new byte[1024];
            int numBytes;
            while (true){
                try{
                    numBytes = mmInStream.read(mmBuffer);
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e){
                    Log.d(TAG1, "Input stream was disconnected",e);
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            try {
                mmOutStream.write(bytes);
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e){
                Log.e(TAG1,"Error occurred when sending data",e);
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast","Couldnt send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG1, "Couldn't close the connect socket",e );
            }
        }
    }
}
