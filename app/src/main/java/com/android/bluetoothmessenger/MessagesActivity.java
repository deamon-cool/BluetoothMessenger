package com.android.bluetoothmessenger;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.android.bluetoothmessenger.Constants.Constants.BLUETOOTH_DEVICE;
import static com.android.bluetoothmessenger.Constants.Constants.CLIENT;
import static com.android.bluetoothmessenger.Constants.Constants.CONNECTION_NAME;

public class MessagesActivity extends AppCompatActivity {

    public static Intent newIntent(Context context, String connectionName, BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(CONNECTION_NAME, connectionName);
        if (connectionName.equals(CLIENT)) {
            intent.putExtra(BLUETOOTH_DEVICE, bluetoothDevice);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            // pobieranie danych Extras z Intent wysłanych z FirstFragment lub DevicesFragment
            String connectionName = getIntent().getStringExtra(CONNECTION_NAME);
            BluetoothDevice bluetoothDevice = null;
            if (connectionName.equals(CLIENT)) {
                bluetoothDevice = getIntent().getParcelableExtra(BLUETOOTH_DEVICE);
            }

            fragment = MessagesFragment.newInstance(connectionName, bluetoothDevice);
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
