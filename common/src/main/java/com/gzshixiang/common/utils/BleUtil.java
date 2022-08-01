package com.gzshixiang.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Copyright © 2019 - 2021 GZShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2021/3/10
 * @email 2751358839@qq.com
 */

public class BleUtil {
    public static final int REQUEST_CODE_ENABLE_BLE = 2001;
    private static BluetoothAdapter mBluetoothAdapter = null;

    /**
     * 强制开启当前 Android 设备的 Bluetooth
     *
     * @return true：强制打开 Bluetooth　成功　false：强制打开 Bluetooth 失败
     */
    @SuppressLint("MissingPermission")
    public static boolean turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null) {
            return bluetoothAdapter.enable();
        }

        return false;
    }

    public static void askUserToEnableBluetoothIfNeeded(Activity activity) {
        if (isBluetoothLeSupported(activity) && (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            activity.startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLE);
        }

    }


    public static boolean isBluetoothLeSupported(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }
}
