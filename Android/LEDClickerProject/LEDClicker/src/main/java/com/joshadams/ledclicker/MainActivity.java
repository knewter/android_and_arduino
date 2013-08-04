package com.joshadams.ledclicker;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;

import java.util.Set;

public class MainActivity extends ListActivity {
  private BluetoothAdapter mBluetoothAdapter;
  private ArrayAdapter<String> mArrayAdapter;
  int REQUEST_ENABLE_BT = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    if(verifyBluetoothSupport()){
      enableBluetooth();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void onLEDButtonClick(View view) {
    showMessage("Pretend this is an LED");
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

  public void enableBluetooth() {
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

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // When discovery finds a device
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        // Add the name and address to an array adapter to show in a ListView
        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        setListAdapter(mArrayAdapter);
      }
    }
  };
}
