package com.example.androidimu.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by 601042 on 2018/6/28.
 */

public interface BlePeripheralCallback {
     void onConnectionStateChange(BluetoothDevice device, int status, int newState);
     void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                       boolean responseNeeded, int offset, byte[] requestBytes);
     void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic);
}
