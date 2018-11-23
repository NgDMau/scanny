package com.example.andjm.scanny;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class CentralActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private MenuItem scanButton;
    private LinearLayout content_layout,fLayout;
    private TextView checking_board;
    private ScrollView scroll_container;
    private final String TAG = "scanny_application";
    private static final int REQUEST_CODE = 1;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    private OutputStream outStream;
    private static InputStream inStream;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName = null;
    private String dialogMessage = "hello it is me";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        scanButton        = findViewById(R.id.nav_bluetooth);
        content_layout    = findViewById(R.id.content_layout);
        mDrawerLayout     = findViewById(R.id.drawer_layout);
        checking_board    = findViewById(R.id.noti_view);
        scroll_container  = findViewById(R.id.scrollable_container);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Toast.makeText(CentralActivity.this,"Bluetooth is not supported",
                    Toast.LENGTH_SHORT).show();
        }

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        if(menuItem.getItemId() == R.id.nav_bluetooth){
                            if (!mBluetoothAdapter.isEnabled()) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                                // Otherwise, setup the chat session
                            }
                            Intent mIntent = new Intent(CentralActivity.this, DeviceListActivity.class);
                            CentralActivity.this.startActivityForResult(mIntent, REQUEST_CODE);
                        }

                        return true;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            String address = data.getExtras().getString("device_address");
            BluetoothDevice Newdevice = mBluetoothAdapter.getRemoteDevice(address);
            mChatService.connect(Newdevice,true);
            Toast.makeText(CentralActivity.this, "Connecting to "+data.getExtras().getString("device_address"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    //BluetoothDevice Bluedevice = new BluetoothDevice("adasdasd");

    private void setupChat(){
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    private void updateStatus(String status){
        checking_board.setText(status);
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    public void querryButtonPressed(View view){
        scroll_container.removeAllViews();
        String command = "checkinfo_123456";
        sendMessage(command);

    }

    public void crontabButtonPressed(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        CrontabFragment crontabFragment = CrontabFragment.newInstance("infore");
        crontabFragment.show(fragmentManager, null);
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            updateStatus("CONNECTED SUCCESSFULLY");
                            content_layout.removeAllViews();
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                            updateStatus("CONNECTING TO "+mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            updateStatus("LISTENING STATE");
                        case BluetoothChatService.STATE_NONE:
                            updateStatus("FREE MODE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    TextView outMessage = new TextView(CentralActivity.this);
                    outMessage.setText("Signals just sent! Waiting for responses..."+"\n");
                    scroll_container.addView(outMessage);
                    break;
                case Constants.MESSAGE_READ:
                    scroll_container.removeAllViews();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    TextView inMessage = new TextView(CentralActivity.this);
                    inMessage.setText(mConnectedDeviceName +": \n" + readMessage+"\n");
                    scroll_container.addView(inMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != CentralActivity.this) {
                        Toast.makeText(CentralActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != CentralActivity.this) {
                        Toast.makeText(CentralActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        // And stop the BluetoothChatService.
        if (mChatService != null) {
            mChatService.stop();
        }
    }




}
