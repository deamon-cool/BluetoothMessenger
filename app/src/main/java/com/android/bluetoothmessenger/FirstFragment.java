package com.android.bluetoothmessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.android.bluetoothmessenger.Constants.Constants.REQUEST_DISCOVERABLE;
import static com.android.bluetoothmessenger.Constants.Constants.SERVER;

public class FirstFragment extends Fragment {
    private BluetoothAdapter mBluetoothAdapter;
    private Button mServer;
    private Button mClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.no_support_bluetooth, Toast.LENGTH_LONG).show();
        } else {
            enableBluetooth();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_first, container, false);

        mServer = v.findViewById(R.id.server_button);
        mServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int duration = 300;

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
                startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);

                // ............ZRÓB TEST co jeśli Bluetooth jest już włączony, czy już jest w stanie wykrywania ???? ? ?? ????

            }
        });

        mClient = v.findViewById(R.id.client_button);
        mClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DevicesActivity.class);
                startActivity(intent);
            }
        });


       //  jeżeli Bluetooth nie istnieje na urządzeniu lub nie jest włączony to brak dostępu do przycisków
        if (!mBluetoothAdapter.isEnabled()) {
            mServer.setClickable(false);
            mClient.setClickable(false);
        }

        return v;
    }

    // włączanie Bluetooth jeżeli nie jest włączony + Receiver Bluetooth adapter state
    private void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }

        IntentFilter stateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mBluetoothStateReceiver, stateFilter);
    }

    // odczytywanie stanów Bluetooth tj. on/off  bluetooth etc.
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                // Przeciski nie możliwe do kliknięcia
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(getActivity(), R.string.bluetooth_off, Toast.LENGTH_LONG).show();
                    mServer.setClickable(false);
                    mClient.setClickable(false);
                    break;
                    // Ustawia przecysiki możliwe do kliknięcia, ponieważ Bluetooth on
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(getActivity(), R.string.bluetooth_on, Toast.LENGTH_LONG).show();
                    mServer.setClickable(true);
                    mClient.setClickable(true);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Toast.makeText(getActivity(), R.string.turning_off_bluetooth, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Toast.makeText(getActivity(), R.string.turning_on_bluetooth, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        // Włączono wyjrywalność urządzenia, przejdź do MessagesActivity
        if (requestCode == REQUEST_DISCOVERABLE) {
            Intent intent = MessagesActivity.newIntent(getActivity(), SERVER, null);
            startActivity(intent);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBluetoothStateReceiver);
        mBluetoothAdapter.disable();

        // ...............ZRÓB TEST Sprawdź czy Bluetooth wyłącza się jeżeli jesteś we Fragmencie MessagesFragment ???? ???? ??

    }
}
