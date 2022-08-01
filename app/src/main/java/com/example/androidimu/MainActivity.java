package com.example.androidimu;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidimu.ble.BlePeripheralCallback;
import com.example.androidimu.ble.BluetoothGattCharacteristicInfo;
import com.example.androidimu.ble.BluetoothGattDescriptorInfo;
import com.example.androidimu.ble.BluetoothGattServiceInfo;
import com.example.androidimu.utils.BlePeripheralUtils;
import com.example.androidimu.utils.ByteUtils;
import com.google.common.primitives.Bytes;
import com.gzshixiang.common.permission.GPermisson;
import com.gzshixiang.common.permission.PermissionCallback;
import com.gzshixiang.common.utils.ToastUtil;
import com.gzshixiang.xlog.XLogUtil;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.DoubleAccumulator;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";


    public final static UUID UUID_SERVER = UUID.fromString("0000181C-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_SERVER1 = UUID.fromString("0000181C-0000-1000-8000-00805F9B34FA");
    public final static UUID UUID_CHARREAD = UUID.fromString("0000F1F1-0000-1000-8000-00805F9B3401");
    public final static UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHARWRITE = UUID.fromString("0000F1F2-0000-1000-8000-00805F9B3402");
    public final static UUID UUID_NOTIFY = UUID.fromString("0000F1F3-0000-1000-8000-00805F9B3403");
    //test
    private final byte[] serviceData = "45464748".getBytes();
    private long bleSendCount = 0;

    private BluetoothGattCharacteristic gattCharacteristic;
    private BlePeripheralUtils blePeripheralUtils;
    private boolean bThreadRunFlag = false;

    //acc,gyro,gravity,timestamp
    private byte[] bleSendBuffer = new byte[20];
    private List<Byte> bleByteList = new ArrayList<>(40);
    private BlockingQueue<Byte> gyroQueue = new LinkedBlockingQueue<>(12);
    private BlockingQueue<Byte> accQueue = new LinkedBlockingQueue<>(16);
    private BlockingQueue<Byte> gravityQueue = new LinkedBlockingQueue<>(12);
    private boolean bIsBleConnected = false;

    private long lastSendTimeStamp = 0L;

    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private Sensor gyroscopeSensor;
    private Sensor linearAccelSensor;

    private TextView tvGravity;
    private TextView tvAccel;
    private TextView tvGyro;
    private Button btnStart;
    private Button btnStop;
    private Button btnDelete;
    private Button btnBleBroadcast;
    private Button btnBleSendData;


    private Handler uiHandler;
    private boolean bStarted = false;
    private boolean bGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initHandler();
        initListener();

        initSensorManager();
        initBlePeripheral();
        initRuntimePermission();

    }

    private void initView() {
        tvGravity = findViewById(R.id.tv_gravity);
        tvAccel = findViewById(R.id.tv_accel);
        tvGyro = findViewById(R.id.tv_gyro);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnDelete = findViewById(R.id.btn_delete);
        btnBleBroadcast = findViewById(R.id.btn_ble_broadcast);
        btnBleSendData = findViewById(R.id.btn_ble_send_data);
    }

    private void initHandler() {
        uiHandler = new Handler(getMainLooper(), message -> {
            switch (message.what) {
                case Sensor.TYPE_GRAVITY:
                    tvGravity.setText((CharSequence) message.obj);
                    break;
                case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                    tvAccel.setText((CharSequence) message.obj);
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    tvGyro.setText((CharSequence) message.obj);
                    break;
            }
            return false;
        });
    }


    private void initListener() {
        btnStart.setOnClickListener(v -> {
            if (bGranted) {
                bStarted = true;
                initxlog();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (bStarted) {
                ToastUtil.toastOnUIThread("必须先调用Stop!");
                return;
            }
            XLogUtil.clearCacheDir();
        });

        btnStop.setOnClickListener(v -> {
            bStarted = false;
            com.tencent.mars.xlog.Log.appenderFlush(false);
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.toastOnUIThread("日志保存于/sdcard/Android/data/xx_packageName/xlog/");
                    com.tencent.mars.xlog.Log.appenderClose();
                }
            }, 1000);

        });

        btnBleBroadcast.setOnClickListener(v -> {
            //先打开广播
            blePeripheralUtils.startBluetoothLeAdvertiser("ZJR_BLE", serviceData, UUID_SERVER1, advertiseCallback);
            //实例化需要添加的service信息
            BluetoothGattCharacteristicInfo[] bluetoothGattCharacteristicInflows = new BluetoothGattCharacteristicInfo[3];
            BluetoothGattDescriptorInfo descriptorInfo = new BluetoothGattDescriptorInfo(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
            bluetoothGattCharacteristicInflows[0] = new BluetoothGattCharacteristicInfo(UUID_CHARREAD, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ, null);
            bluetoothGattCharacteristicInflows[1] = new BluetoothGattCharacteristicInfo(UUID_CHARWRITE, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE, null);
            bluetoothGattCharacteristicInflows[2] = new BluetoothGattCharacteristicInfo(UUID_NOTIFY, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PROPERTY_NOTIFY, descriptorInfo);
            BluetoothGattServiceInfo bluetoothGattServiceInfo = new BluetoothGattServiceInfo(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY, bluetoothGattCharacteristicInflows);
            //添加需要的service
            blePeripheralUtils.addServices(bluetoothGattServiceInfo);
        });

        btnBleSendData.setOnClickListener(v -> {
            if (bIsBleConnected) {
                if (!bThreadRunFlag) {
                    bThreadRunFlag = true;
                    ToastUtils.show("Start BLE Send!");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            while (bThreadRunFlag) {
//
//                                while (gravityQueue.size() != 12 || gyroQueue.size() != 12 || accQueue.size() != 16) {
//                                    Log.w(TAG, String.format("run: data is not all ready! size(%d,%d,%d)", gyroQueue.size(), gyroQueue.size(), accQueue.size()));
//                                    if (!bThreadRunFlag) {
//                                        break;
//                                    }
//                                    continue;
//                                }
//                                bleByteList.clear();
////                                try {
////                                    for (int i = 0; i < 12; i++) {
////                                        bleSendBuffer[i] = gravityQueue.take();
////                                        bleSendBuffer[i + 12] = gyroQueue.take();
////                                        bleSendBuffer[i + 24] = accQueue.take();
////                                    }
////                                    for (int j = 0; j < 4; j++) {
////                                        bleSendBuffer[36 + j] = accQueue.take();
////                                    }
////
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                               }
//
//                                gravityQueue.drainTo(bleByteList);
//                                gyroQueue.drainTo(bleByteList);
//                                accQueue.drainTo(bleByteList);
//                                bleSendBuffer = Bytes.toArray(bleByteList);
//
//                                Log.d(TAG, "run: bleSendBuffer = " + ByteUtils.bytes2HexStr(bleSendBuffer));
//                                notifyBleWriteData(bleSendBuffer);
//                                bleSendCount++;
//                                Log.d(TAG, "run: bleSendCount = " + bleSendCount);
////                                try {
////                                    Thread.sleep(10);
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                                }
//                            }
//                        }
//                    }, "BleImuDataThread").start();
                } else {
                    bThreadRunFlag = false;
                    ToastUtils.show("Stop BLE Send!");
                }


            } else {
                ToastUtils.show("Device Not Connected!");
            }
        });
    }

    private void initRuntimePermission() {
        GPermisson.with(this)
                .permisson(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .callback(new PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        // initxlog();
                        bGranted = true;
                    }

                    @Override
                    public void shouldShowRational(String permisson) {
                        ToastUtil.toastOnUIThread("请授予读写权限！");
                    }

                    @Override
                    public void onPermissonReject(String permisson) {
                        ToastUtil.toastOnUIThread("请授予读写权限！");
                    }
                }).request();
    }


    private void initxlog() {
        XLogUtil.init(this);
        //FileUtils.createOrExistsFile(SHMEM_THREEDOF);
        //FileUtils.createOrExistsFile(SHMEM_SIXDOF);
    }


    private void initBlePeripheral() {
        //实例化工具类
        blePeripheralUtils = new BlePeripheralUtils(MainActivity.this);
        //初始化一下
        blePeripheralUtils.init();
        //设置一个结果callback 方便把某些结果传到前面来
        blePeripheralUtils.setBlePeripheralCallback(callback);


    }


    private void initSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, linearAccelSensor, SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, gyroscopeSensor);
        sensorManager.unregisterListener(this, gravitySensor);
        sensorManager.unregisterListener(this, linearAccelSensor);

        com.tencent.mars.xlog.Log.appenderFlush(false);
        com.tencent.mars.xlog.Log.appenderClose();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                String gravityInfo = Arrays.toString(sensorEvent.values);

                Message gravityMsg = Message.obtain();
                gravityMsg.what = Sensor.TYPE_GRAVITY;
                gravityMsg.obj = "[Gravity]: \n" + gravityInfo + "\ntimestamp = " + sensorEvent.timestamp;
                uiHandler.sendMessageDelayed(gravityMsg, 100);
                //tvGravity.setText("[Gravity]: \n"+gravityInfo);

                // Log.d(TAG, "[Gravity]: "+gravityInfo+" timestamp = "+sensorEvent.timestamp);
                if (bStarted) {
                    com.tencent.mars.xlog.Log.d("", "xlog_gravity: %f %f %f %d", sensorEvent.values[0],
                            sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp);
                }
                byte[] gravityXBytes = ByteUtils.getBytes(sensorEvent.values[0]);
                byte[] gravityYBytes = ByteUtils.getBytes(sensorEvent.values[1]);
                byte[] gravityZBytes = ByteUtils.getBytes(sensorEvent.values[2]);

                gravityQueue.offer(gravityXBytes[0]);
                gravityQueue.offer(gravityXBytes[1]);
                gravityQueue.offer(gravityXBytes[2]);
                gravityQueue.offer(gravityXBytes[3]);

                gravityQueue.offer(gravityYBytes[0]);
                gravityQueue.offer(gravityYBytes[1]);
                gravityQueue.offer(gravityYBytes[2]);
                gravityQueue.offer(gravityYBytes[3]);

                gravityQueue.offer(gravityZBytes[0]);
                gravityQueue.offer(gravityZBytes[1]);
                gravityQueue.offer(gravityZBytes[2]);
                gravityQueue.offer(gravityZBytes[3]);


                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                String gyroInfo = Arrays.toString(sensorEvent.values);

                Message gyroMsg = Message.obtain();
                gyroMsg.what = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
                gyroMsg.obj = "[Gyroscope]: \n" + gyroInfo + "\n timestamp = " + sensorEvent.timestamp;
                uiHandler.sendMessageDelayed(gyroMsg, 100);
                //tvGyro.setText("[Gyroscope]: \n"+gyroInfo);

                //Log.d(TAG, "[Gyroscope]: "+gyroInfo+" timestamp = "+sensorEvent.timestamp);
                if (bStarted) {

                    com.tencent.mars.xlog.Log.d("", "xlog_gyro: %f %f %f %d", sensorEvent.values[0],
                            sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp);
                }

                byte[] gyroXBytes = ByteUtils.getBytes(sensorEvent.values[0]);
                byte[] gyroYBytes = ByteUtils.getBytes(sensorEvent.values[1]);
                byte[] gyroZBytes = ByteUtils.getBytes(sensorEvent.values[2]);

                gyroQueue.offer(gyroXBytes[0]);
                gyroQueue.offer(gyroXBytes[1]);
                gyroQueue.offer(gyroXBytes[2]);
                gyroQueue.offer(gyroXBytes[3]);

                gyroQueue.offer(gyroYBytes[0]);
                gyroQueue.offer(gyroYBytes[1]);
                gyroQueue.offer(gyroYBytes[2]);
                gyroQueue.offer(gyroYBytes[3]);

                gyroQueue.offer(gyroZBytes[0]);
                gyroQueue.offer(gyroZBytes[1]);
                gyroQueue.offer(gyroZBytes[2]);
                gyroQueue.offer(gyroZBytes[3]);

                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                String accelInfo = Arrays.toString(sensorEvent.values);

                Message accelMsg = Message.obtain();
                accelMsg.what = Sensor.TYPE_ACCELEROMETER_UNCALIBRATED;
                accelMsg.obj = "[Linear_Accel]: \n" + accelInfo + "\n timestamp = " + sensorEvent.timestamp;
                uiHandler.sendMessageDelayed(accelMsg, 100);
                //tvAccel.setText("[Linear_Accel]: \n"+accelInfo);

                //Log.d(TAG, "[Linear_Accel]: "+accelInfo+" timestamp = "+sensorEvent.timestamp);
                if (bStarted) {
//                    Log.getImpl().logF("", "ACCEL", "",
//                            0, Process.myPid(), Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(), String.format("xlog_accel: %f %f %f %d", sensorEvent.values[0],
//                                    sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp));
                    com.tencent.mars.xlog.Log.d("", "xlog_accel: %f %f %f %d", sensorEvent.values[0],
                            sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp);
                }


                if (bIsBleConnected&&bThreadRunFlag) {
                    byte[] accXBytes = ByteUtils.getBytes(sensorEvent.values[0]);
                    byte[] accYBytes = ByteUtils.getBytes(sensorEvent.values[1]);
                    byte[] accZBytes = ByteUtils.getBytes(sensorEvent.values[2]);
                    byte[] timestampBytes = ByteUtils.getBytes(sensorEvent.timestamp);

                    accQueue.offer(accXBytes[0]);
                    accQueue.offer(accXBytes[1]);
                    accQueue.offer(accXBytes[2]);
                    accQueue.offer(accXBytes[3]);

                    accQueue.offer(accYBytes[0]);
                    accQueue.offer(accYBytes[1]);
                    accQueue.offer(accYBytes[2]);
                    accQueue.offer(accYBytes[3]);

                    accQueue.offer(accZBytes[0]);
                    accQueue.offer(accZBytes[1]);
                    accQueue.offer(accZBytes[2]);
                    accQueue.offer(accZBytes[3]);

                    accQueue.offer(timestampBytes[0]);
                    accQueue.offer(timestampBytes[1]);
                    accQueue.offer(timestampBytes[2]);
                    accQueue.offer(timestampBytes[3]);

                    bleSendBuffer = Bytes.concat(new byte[]{0x01}, accXBytes, accYBytes, accZBytes, timestampBytes);
                    Log.d(TAG, "onSensorChanged: [acc] bleSendBuffer: " + ByteUtils.bytes2HexStr(bleSendBuffer));
                    boolean res = notifyBleWriteData(bleSendBuffer);
                    if (res) {
                        bleSendCount++;
                    } else {
                        Log.e(TAG, "onSensorChanged: notifyBleWriteData failed");
                    }
                    if (lastSendTimeStamp == 0) {
                        lastSendTimeStamp = System.currentTimeMillis();
                    } else {
                        long currTime = System.currentTimeMillis();
                        Log.d(TAG, String.format("onSensorChanged: dt = %d(ms),sendCount = %d", (currTime - lastSendTimeStamp), bleSendCount));
                        lastSendTimeStamp = currTime;
                    }

                }
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private BlePeripheralCallback callback = new BlePeripheralCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
//            Log.d(TAG, String.format("onConnectionStateChange: %s,status = %d,newstate = %d",device.toString(),status,newState));
            if (newState == BluetoothGattServer.STATE_CONNECTED) {
                ToastUtils.show("Ble Device Connected!");
                bIsBleConnected = true;
            } else {
                ToastUtils.show("Ble Device Disconnected !");
                bIsBleConnected = false;
                bThreadRunFlag = false;
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            android.util.Log.d(TAG, "requestBytes: " + ByteUtils.bytes2HexStr(requestBytes));
            ToastUtils.show(String.format("receive data from device: [%s],\ndata: %s", device.getAddress(), ByteUtils.bytes2HexStr(requestBytes)));
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

        }
    };

    @Override
    protected void onDestroy() {
        if (blePeripheralUtils != null) {
            blePeripheralUtils.stopBluetoothLeAdvertiser(advertiseCallback);
        }
        super.onDestroy();
    }

    private boolean notifyBleWriteData(byte[] data) {
        //看看characteristic是否为空  为空就获取一下
        if (gattCharacteristic == null) {
            gattCharacteristic = blePeripheralUtils.getCharacteristic(UUID_SERVER, UUID_NOTIFY);
        }
        if (gattCharacteristic != null) {
            return blePeripheralUtils.notify(blePeripheralUtils.getConnectedDeviceArrayList().get(0), gattCharacteristic, data);
        }

        return false;

    }

    /**
     * 开启广播的结果callback
     */
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            android.util.Log.d(TAG, "BLE advertisement added successfully");
            ToastUtils.show("开启蓝牙广播成功！");

        }

        @Override
        public void onStartFailure(int errorCode) {
            android.util.Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
            ToastUtils.show("开启蓝牙广播失败：errorCode = " + errorCode);
        }
    };


}