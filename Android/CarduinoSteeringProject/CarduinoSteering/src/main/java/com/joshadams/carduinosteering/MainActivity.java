package com.joshadams.carduinosteering;

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
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.widget.VerticalSeekBar;

public class MainActivity extends ListActivity {
  private static final String TAG = "CarduinoBluetooth";
  private BluetoothAdapter mBluetoothAdapter;
  private ArrayAdapter<String> mArrayAdapter;
  private ArrayList<BluetoothDevice> mKnownDevices = new ArrayList<BluetoothDevice>();
  private BluetoothDevice mChosenDevice;
  private BluetoothSocket mSocket;
  private ConnectedThread mConnectedThread;
  private boolean mLEDState = false;
  int REQUEST_ENABLE_BT = 1;
  double mLeft = 0;
  double mRight = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

    final VerticalSeekBar leftStick = (VerticalSeekBar) findViewById(R.id.leftStick);
    final VerticalSeekBar rightStick = (VerticalSeekBar) findViewById(R.id.rightStick);

    leftStick.setProgress(50);
    //mLeftValue.setText(Double.toString(0));
    leftStick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        double stickValue = mapStickValueToMotorValue(i);
        if(stickValue != 0.0){
          mLeft = stickValue;
        }
        writeLeftMotorAction();
        //mLeftValue.setText(Double.toString(stickValue));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // NOTE: This should happen, but it moves the thumb to the bottom for some inexplicable reason
        leftStick.setProgress(50);
        mLeft = 0.01;
        writeLeftMotorAction();
        //mLeftValue.setText(Double.toString(mapStickValueToMotorValue(50)));
      }
    });

    rightStick.setProgress(50);
    //mRightValue.setText(Double.toString(0));
    rightStick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        double stickValue = mapStickValueToMotorValue(i);
        if(stickValue != 0.0){
          mRight = stickValue;
        }
        writeRightMotorAction();
        //mRightValue.setText(Double.toString(stickValue));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // NOTE: This should happen, but it moves the thumb to the bottom for some inexplicable reason
        rightStick.setProgress(50);
        mRight = 0.01;
        writeRightMotorAction();
        //mRightValue.setText(Double.toString(mapStickValueToMotorValue(50)));
      }
    });

    if(verifyBluetoothSupport()) enableBluetoothAndScanForDevices();
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    mChosenDevice = mKnownDevices.get(position);

    // Discovery is resource-intensive, so we'll cancel it
    mBluetoothAdapter.cancelDiscovery();

    // connect to the bluetooth serial port
    if(connectBluetooth()){
      hideBluetoothList();
      showSteeringInterface();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  protected void hideBluetoothList() {
    View bluetoothList = (View) findViewById(android.R.id.list);
    bluetoothList.setVisibility(View.INVISIBLE);
  }

  protected void showSteeringInterface() {
    View leftStick = (VerticalSeekBar) findViewById(R.id.leftStick);
    View rightStick = (VerticalSeekBar) findViewById(R.id.rightStick);
    leftStick.setVisibility((View.VISIBLE));
    rightStick.setVisibility((View.VISIBLE));
  }

  protected void writeLeftMotorAction() {
    Log.d(TAG, Double.toString(mLeft));
    Log.d(TAG, mapValueToProtocol(mLeft));
    mConnectedThread.write(mapValueToProtocol(mLeft/2));
  }

  protected void writeRightMotorAction() {
    //Log.d(TAG, mapValueToProtocol(mRight));
    mConnectedThread.write(mapValueToProtocol(mRight/2).toUpperCase());
  }

  protected String mapValueToProtocol(double value){
    if(value <= -0.9){
      return "a";
    } else if (value <= -0.8){
      return "b";
    } else if (value <= -0.7){
      return "c";
    } else if (value <= -0.6){
      return "d";
    } else if (value <= -0.5){
      return "e";
    } else if (value <= -0.4){
      return "f";
    } else if (value <= -0.3){
      return "g";
    } else if (value <= -0.2){
      return "h";
    } else if (value <= -0.1){
      return "i";
    } else if (value < 0){
      return "j";
    } else if (value <= 0.1){
      return "k";
    } else if (value <= 0.2){
      return "l";
    } else if (value <= 0.3){
      return "m";
    } else if (value <= 0.4){
      return "n";
    } else if (value <= 0.5){
      return "o";
    } else if (value <= 0.6){
      return "p";
    } else if (value <= 0.7){
      return "q";
    } else if (value <= 0.8){
      return "r";
    } else if (value <= 0.9){
      return "s";
    } else if (value <= 1){
      return "t";
    }
    return "z"; // no-op if outside the ranges
  }

  protected boolean connectBluetooth() {
    try {
      Method m = mChosenDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
      mSocket = (BluetoothSocket) m.invoke(mChosenDevice, 1);
      mSocket.connect();
      showMessage("...Connection ok...");
      mConnectedThread = new ConnectedThread(mSocket);
      mConnectedThread.start();
      showMessage("Connected Thread has started.");
      return true;
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
    return false;
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

  protected double mapStickValueToMotorValue(int stickValue) {
    double middle = 50;
    double middleMapped = (double) stickValue - middle;
    return middleMapped / middle;
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
