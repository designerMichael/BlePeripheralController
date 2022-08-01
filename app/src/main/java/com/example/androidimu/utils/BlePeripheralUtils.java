package com.example.androidimu.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.androidimu.ble.BlePeripheralCallback;
import com.example.androidimu.ble.BluetoothGattCharacteristicInfo;
import com.example.androidimu.ble.BluetoothGattDescriptorInfo;
import com.example.androidimu.ble.BluetoothGattServiceInfo;
import com.hjq.toast.ToastUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by 601042 on 2018/6/28.
 * <p>
 * 封装好Ble Peripheral模式的工具类
 */

public class BlePeripheralUtils {
    private static final String TAG = "BlePeripheralUtils";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private Context context;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer bluetoothGattServer;

    private boolean bIsInitBle = false;

    //连接上的设备
    private ArrayList<BluetoothDevice> connectedDeviceArrayList = new ArrayList<BluetoothDevice>();
    //ble的状态callback
    private BlePeripheralCallback blePeripheralCallback;


    public BlePeripheralUtils(Context context) {
        this.context = context;
    }

    /**
     * 服务事件的回调
     */
    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        /**
         * 1.连接状态发生变化时
         * @param device ：连接的设备
         * @param status ：操作状态（0是成功，其他值为失败）
         * @param newState ：当前连接状态（2是已连接 0是已断开）
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.e(TAG, String.format("1.onConnectionStateChange：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("1.onConnectionStateChange：status = %s ==> newState =%s (0--STATE_DISCONNECTED,1--STATE_CONNECTING,2--STATE_CONNECTED)", status, newState));

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                //连接成功后保存当前的设备
                connectedDeviceArrayList.add(device);
            } else {
                //断开后从连接的列表里删除设备
                int index = 0;
                for (int i = 0; i < connectedDeviceArrayList.size(); i++) {
                    if (connectedDeviceArrayList.get(i).getAddress().equals(device.getAddress())) {
                        index = i;
                        break;
                    }
                }
                connectedDeviceArrayList.remove(index);
            }
            //通过回调发送出去
            if (blePeripheralCallback != null) {
                blePeripheralCallback.onConnectionStateChange(device, status, newState);
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.e(TAG, String.format("onServiceAdded：status = %s(0--GATT_SUCCESS)", status));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                ToastUtils.show("[onServiceAdded] failed!");
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, String.format("onCharacteristicReadRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("onCharacteristicReadRequest：requestId = %d, offset = %d,charac_value = %s", requestId, offset,
                    ByteUtils.bytes2HexStr(characteristic.getValue())));

            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
//            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            //通过回调发送出去
            if (blePeripheralCallback != null) {
                blePeripheralCallback.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }
        }

        /**
         * 3. onCharacteristicWriteRequest,接收具体的字节
         * @param device ：连接的设备
         * @param requestId ：请求的ID（也可以理解为流水号）
         * @param characteristic ：发送消息使用的characteristic
         * @param preparedWrite ：是否需要等待后续操作
         * @param responseNeeded ：是否需要回复
         * @param offset ： 数据内容偏移
         * @param requestBytes ：数据内容
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            Log.e(TAG, String.format("3.onCharacteristicWriteRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("3.onCharacteristicWriteRequest：requestId = %d, preparedWrite=%b, responseNeeded=%bs, offset=%d, value=%s,charac_value = %s",
                    requestId, preparedWrite, responseNeeded, offset, ByteUtils.bytes2HexStr(requestBytes), ByteUtils.bytes2HexStr(characteristic.getValue())));
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            //通过回调发送出去
            if (blePeripheralCallback != null) {
                blePeripheralCallback.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, requestBytes);
            }
        }

        /**
         * 2.描述被写入时，在这里执行 bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...  收，触发 onCharacteristicWriteRequest
         * @param device
         * @param requestId
         * @param descriptor
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param value
         */
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.e(TAG, String.format("2.onDescriptorWriteRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("2.onDescriptorWriteRequest：requestId = %d, preparedWrite = %b, responseNeeded = %b, " +
                            "offset = %d, value = %s,", requestId, preparedWrite, responseNeeded, offset,
                    ByteUtils.bytes2HexStr(value)));

            // now tell the connected device that this was all successfull
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        /**
         * 5.特征被读取。当回复响应成功后，客户端会读取然后触发本方法
         * @param device
         * @param requestId
         * @param offset
         * @param descriptor
         */
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.e(TAG, String.format("onDescriptorReadRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId));
//            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{0, 1, 2, 3, 4});
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.e(TAG, String.format("5.onNotificationSent：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("5.onNotificationSent：status = %d", status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            Log.e(TAG, String.format("onMtuChanged：mtu = %d", mtu));
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.e(TAG, String.format("onExecuteWrite：requestId = %d,execute = %b", requestId, execute));
        }


    };


    /**
     * 初始化
     */
    public void init() {
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled.  If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            bIsInitBle = enableBle();
        } else {
            bIsInitBle = true;
        }
    }

    private void checkInitBle() {
        if (!bIsInitBle) {
            throw new IllegalStateException("Please Call Init Fun First!");
        }
    }


    /**
     * 打开蓝牙
     */
    private boolean enableBle() {

        boolean result = false;
        try {
            if (mBluetoothAdapter == null) {
                return false;
            }
            for (Method temp : Class.forName(mBluetoothAdapter.getClass().getName()).getMethods()) {
                if (temp.getName().equals("enableNoAutoConnect")) {
                    result = (boolean) temp.invoke(mBluetoothAdapter);
                }
            }
        } catch (Exception e) {
            //反射调用失败就启动通过enable()启动;
            result = mBluetoothAdapter.enable();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 关闭蓝牙
     */
    public void disableBle() {
        checkInitBle();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * 开启广播
     *
     * @param bleName：ble设备的名称
     * @param serviceData：要放到scanrecord中的数据
     * @param parcelUUID：要放到scanrecord中的UUID
     * @param callback
     */
    public void startBluetoothLeAdvertiser(String bleName, byte[] serviceData, UUID parcelUUID, AdvertiseCallback callback) {
        checkInitBle();
        //广播设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setConnectable(true) //是否被连接
                .setTimeout(0)        //超时时间
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)  //广播模式: 低时延
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)   //发射功率
                .build();

        //广播数据设置
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)    //是否在广播中携带设备的名称
                .setIncludeTxPowerLevel(true)  //是否在广播中携带信号强度
                .build();
        //扫描回应的广播设置
        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)  //是否在广播中携带设备的名称
                .addServiceData(new ParcelUuid(parcelUUID), serviceData) //在scanrecord中添加的数据
                .build();

        //设置BLE设备的名称
        mBluetoothAdapter.setName(bleName);
        //开启广播
        bluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);

    }

    /**
     * 停止广播
     */
    public void stopBluetoothLeAdvertiser(AdvertiseCallback callback) {
        checkInitBle();
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.stopAdvertising(callback);
        } else {
            Log.e(TAG, "stopBluetoothLeAdvertiser: bluetoothLeAdvertiser is null !!!");
        }

    }

    /**
     * 添加ble的service
     *
     * @param serviceInfo：需要添加服务列表
     */
    public void addServices(BluetoothGattServiceInfo... serviceInfo) {
        checkInitBle();

        try {
//            for (Method temp : Class.forName(bluetoothManager.getClass().getName()).getMethods()) {
//                if (temp.getName().equals("openGattServer")) {
//                    temp.setAccessible(true);
//                    bluetoothGattServer = (BluetoothGattServer) temp.invoke(bluetoothManager, context, bluetoothGattServerCallback,
//                            BluetoothDevice.PHY_LE_2M);
//                }
//            }
            Method method = BluetoothManager.class.getDeclaredMethod("openGattServer", Context.class, BluetoothGattServerCallback.class, Integer.TYPE);
            method.setAccessible(true);
            method.invoke(bluetoothManager, context, bluetoothGattServerCallback,
                    BluetoothDevice.PHY_LE_2M);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (bluetoothGattServer == null) {
            //先获取GattServer
            bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallback/*,BluetoothDevice.PHY_LE_2M*/);

        }
        //循环添加需要添加的service
        for (BluetoothGattServiceInfo temp : serviceInfo) {
            //实例化一个service
            BluetoothGattService service_temp = new BluetoothGattService(temp.getUuid(), temp.getServiceType());
            //添加其中需要的Characteristic
            for (BluetoothGattCharacteristicInfo temp_CharacteristicInfo : temp.getCharacteristicInfos()) {
                //实例化需要的characteristic
                BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(temp_CharacteristicInfo.getUuid(),
                        temp_CharacteristicInfo.getProperties(), temp_CharacteristicInfo.getPermissions());
                //看看需不需要添加descriptor
                BluetoothGattDescriptorInfo descriptorInfo = temp_CharacteristicInfo.getBluetoothGattDescriptorInfo();
                if (descriptorInfo != null) {
                    //需要就先实例化descriptor
                    BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(descriptorInfo.getUuid(), descriptorInfo.permissions);
                    //添加到characteristic里
                    characteristic.addDescriptor(descriptor);

                }
                //把characteristic添加到service
                service_temp.addCharacteristic(characteristic);
            }
            //把service添加到GattServer
            bluetoothGattServer.addService(service_temp);
        }
    }

    /**
     * 发送通知给主机
     *
     * @param device         ：发送的目标设备
     * @param characteristic ：用来通知的characteristic
     * @param data           ：通知的内容
     */
    public boolean notify(BluetoothDevice device, BluetoothGattCharacteristic characteristic, byte[] data) {
        if (device != null && characteristic != null && data != null) {
            //设置写操作的类型 WRITE_TYPE_DEFAULT的情况选  底层会自动分包 不用人为分包
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            //把要设置的数据装进characteristic
            characteristic.setValue(data);
            //发送出去
            return bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
        } else {
            return false;
        }
        
    }


    /**
     * 获取service下的所有Characteristic
     *
     * @param serviceUuid ：service的UUID
     */
    public List<BluetoothGattCharacteristic> getCharacteristicList(UUID serviceUuid) {
        //根据UUID获取service
        BluetoothGattService service = bluetoothGattServer.getService(serviceUuid);
        //获取到了service则获取其中所有的BluetoothGattCharacteristic列表并返回出去
        if (service != null) {
            return service.getCharacteristics();
        } else {
            return null;
        }
    }

    /**
     * 获取service下的所有Characteristic
     *
     * @param serviceUuid        ：service的UUID
     * @param characteristicUuid ： Characteristic的UUID
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
        //根据UUID获取service
        BluetoothGattService service = bluetoothGattServer.getService(serviceUuid);
        //获取到了service则根据Characteristic的UUID获取Characteristic
        if (service != null) {
            return service.getCharacteristic(characteristicUuid);
        } else {
            return null;
        }
    }


    public ArrayList<BluetoothDevice> getConnectedDeviceArrayList() {
        return connectedDeviceArrayList;
    }

    public BlePeripheralCallback getBlePeripheralCallback() {
        return blePeripheralCallback;
    }

    public void setBlePeripheralCallback(BlePeripheralCallback blePeripheralCallback) {
        this.blePeripheralCallback = blePeripheralCallback;
    }
}
