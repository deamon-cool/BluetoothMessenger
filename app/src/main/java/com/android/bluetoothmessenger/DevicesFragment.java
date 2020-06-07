package com.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.bluetoothmessenger.Content.DevicesLab;

import java.util.ArrayList;
import java.util.List;

import static com.android.bluetoothmessenger.Constants.Constants.CLIENT;
import static com.android.bluetoothmessenger.Constants.Constants.REQUEST_ENABLE_BLUETOOTH;


public class DevicesFragment extends Fragment {
    private RecyclerView mDevicesRecyclerView;
    private DevicesAdapter mDevicesAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mDevices;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mFoundDeviceReceiver, filter);
    }

    // Odbieranie informacji z systemu, że znaleziono urządzenie
    private final BroadcastReceiver mFoundDeviceReceiver = new BroadcastReceiver() {   // Kiedy Bluetooth adapter znajdzie urządzenie to wykonuje poniższą instukcję
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevices.size() != 0) {
                    for (int i = 0; i < mDevices.size(); i++) {     // Jeżeli istnieje już takie urządznie na liście to usuń urządzenie Device
                        if (foundDevice.getAddress().equals(mDevices.get(i).getAddress())) {
                            mDevices.remove(i);
                            break;
                        }
                    }
                }

                mDevices.add(foundDevice);
                mDevicesAdapter.notifyDataSetChanged();    // zanotuj znmiany w liście dla adaptera recycler View
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_devices, container, false);

        mDevicesAdapter = new DevicesAdapter();

        mDevicesRecyclerView = v.findViewById(R.id.devices_recycler);
        mDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDevicesRecyclerView.setAdapter(mDevicesAdapter);

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_devices, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // obsługa kliknięcia ikony bluetooth na toolbarze aplikacji
        if (item.getItemId() == R.id.search_device_bluetooth) {
            if (mBluetoothAdapter.isEnabled()) {
                System.out.println("");
                mBluetoothAdapter.startDiscovery();                        // szukanie urządzeń
            } else {
                enableBluetooth();
            }
        }
        return true;
    }

    // włączanie bluetooth
    private void enableBluetooth() {
        Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBt, REQUEST_ENABLE_BLUETOOTH);
    }


    private class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private BluetoothDevice mDevice;
        private TextView mNameTextView;
        private TextView mMacTextView;

        public DeviceViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.device_item, parent, false));
            itemView.setOnClickListener(this);

            mNameTextView = itemView.findViewById(R.id.device_name);
            mMacTextView = itemView.findViewById(R.id.device_mac);
        }

        public void bind(BluetoothDevice device) {
            mDevice = device;
            mNameTextView.setText(mDevice.getName());
            mMacTextView.setText(mDevice.getAddress());
        }

        @Override
        public void onClick(View view) {
            mBluetoothAdapter.cancelDiscovery();                        // kończenie wykrywania urządzeń

            DevicesLab.get().updateDevices(mDevices);                   // przed przejściem do wysłania wiadomości zapisz chwilowo listę BluetoothDevice w Singletone

            Intent intent = MessagesActivity.newIntent(getActivity(), CLIENT, mDevice);
            startActivity(intent);
        }
    }


    private class DevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

        @NonNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new DeviceViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {  // bindSendMessageView information between Holder and Adapter
            BluetoothDevice device = mDevices.get(position);
            holder.bind(device);
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mFoundDeviceReceiver);    // po zniszczeniu aktywności razem z tym fragmentem usuń działanie mReceiver

    }

}
