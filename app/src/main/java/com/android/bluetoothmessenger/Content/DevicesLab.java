package com.android.bluetoothmessenger.Content;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

public class DevicesLab {
    private static DevicesLab sDevicesLab;
    private List<BluetoothDevice> mDevices;

    public static DevicesLab get() {
        if (sDevicesLab == null){
            sDevicesLab = new DevicesLab();
        }
        return sDevicesLab;
    }

    public void updateDevices(List<BluetoothDevice> devices) {
        if (devices == null) {
            mDevices = new ArrayList<>();
        } else {
            mDevices = devices;
        }
    }

    public List<BluetoothDevice> getDevices() {
        return mDevices;
    }
}
