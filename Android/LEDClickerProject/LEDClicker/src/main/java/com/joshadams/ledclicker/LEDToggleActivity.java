package com.joshadams.ledclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.TextView;

public class LEDToggleActivity extends Activity {

  protected String mMacAddress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_led_toggle);
    Intent intent = getIntent();
    Uri macAddressUri = intent.getData();
    // Fetch the MAC address for the bluetooth device from the data sent between activities
    mMacAddress = macAddressUri.toString().replace("bluetoothled://", "");

    TextView macAddressTextView = (TextView) findViewById(R.id.macAddressTextView);
    macAddressTextView.setText(mMacAddress);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.ledtoggle, menu);
    return true;
  }
}
