package com.joshadams.ledclicker;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends ListActivity {
  private static final String TAG = "LEDClickerBluetooth";
  private BluetoothAdapter mBluetoothAdapter;
  private ArrayAdapter<String> mArrayAdapter;
  private ArrayList<BluetoothDevice> mKnownDevices = new ArrayList<BluetoothDevice>();
  private BluetoothDevice mChosenDevice;
  private BluetoothSocket mSocket;
  private ConnectedThread mConnectedThread;
  private boolean mLEDState = false;
  int REQUEST_ENABLE_BT = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

    if(verifyBluetoothSupport()){
      enableBluetoothAndScanForDevices();
    }
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    // Discovery is resource-intensive, so we'll cancel it
    mBluetoothAdapter.cancelDiscovery();

    Button LEDToggleButton = (Button) findViewById(R.id.LEDToggleButton);
    mChosenDevice = mKnownDevices.get(position);

    try {
      Method m = mChosenDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
      mSocket = (BluetoothSocket) m.invoke(mChosenDevice, 1);
      mSocket.connect();
      showMessage("...Connection ok...");
      mConnectedThread = new ConnectedThread(mSocket);
      mConnectedThread.start();
      LEDToggleButton.setEnabled(true);
      showMessage("Connected Thread has started.");
    } catch (IOException e) {
      try {
        mSocket.close();
        showMessage("IOException.  Closed socket successfully.");
        Log.e(TAG, e.getMessage());
      } catch (IOException e2) {
        showMessage("Fatal Error in onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    } catch (NoSuchMethodException e) {
      showMessage("No such method, sorry.");
    } catch (IllegalAccessException e) {
      showMessage("Illegal Access");
    } catch (InvocationTargetException e) {
      showMessage("Invocation Target Exception");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void onLEDButtonClick(View view) {
    if(mLEDState){
      mConnectedThread.write("0");
    } else {
      mConnectedThread.write("1");
    }
    mLEDState = !mLEDState;
  }

  public boolean verifyBluetoothSupport() {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter == null) {
      // Device does not support Bluetooth
      showMessage("Bluetooth is not supported");
      return false;
    } else {
      return true;
    }
  }

  public void enableBluetoothAndScanForDevices() {
    if (!mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      scanForDevices();
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == RESULT_OK) {
      showMessage("Bluetooth request accepted.");
      scanForDevices();
    } else {
      showMessage("Bluetooth request canceled.");
    }
  }

  public void showMessage(String message){
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
  }

  public void scanForDevices(){
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    mBluetoothAdapter.startDiscovery();
  }

  public void onDestroy(){
    unregisterReceiver(mReceiver);
    super.onDestroy();
  }

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // When discovery finds a device
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        // Add the name and address to an array adapter to show in a ListView
        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        mKnownDevices.add(device);
        setListAdapter(mArrayAdapter);
      }
    }
  };

  // Helper thread to manage communication to and from the Bluetooth socket.
  private class ConnectedThread extends Thread {
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;

    public ConnectedThread(BluetoothSocket socket) {
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      // Get the input and output streams, using temp objects because
      // member streams are final
      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) { }

      mInputStream = tmpIn;
      mOutputStream = tmpOut;
    }

    public void run() {
      byte[] buffer = new byte[256];  // buffer store for the stream
      int bytes; // bytes returned from read()

      // Keep listening to the InputStream until an exception occurs
      while (true) {
        try {
          // Read from the InputStream
          Log.d(TAG, "About to get some bytes.");
          bytes = mInputStream.read(buffer);        // Get number of bytes and message in "buffer"
          Log.d(TAG, "Got some bytes");
          // Normally we would do something with this, but since we aren't concerned about inbound
          // data in this application there's no need to implement it.
        } catch (IOException e) {
          break;
        }
      }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String message) {
      Log.d(TAG, "...Data to send: " + message + "...");
      byte[] msgBuffer = message.getBytes();
      try {
        Log.d(TAG, "sending a message: " + message);
        mOutputStream.write(msgBuffer);
      } catch (IOException e) {
        Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
      }
    }
  }
}
