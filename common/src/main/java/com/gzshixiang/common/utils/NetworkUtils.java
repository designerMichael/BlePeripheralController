package com.gzshixiang.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;


import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 * Created by linjw on 2017/8/23.
 * e-mail : linjiawei3046@cvte.com
 */

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";


    /**
     * 网络是否连接.
     *
     * @param context context
     * @return 网络是否连接
     */
    @SuppressLint("MissingPermission")
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        return netinfo != null && netinfo.isConnected();
    }


    /**
     * 判断是否联网
     */
    @SuppressLint("MissingPermission")
    public static boolean isOnline(Context context) {
        //获取 ConnectivityManager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }
        //如果版本大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //获取 NetWork
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                //获取 NetworkCapabilities
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        } else {
            //获取NetworkInfo
            NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
            return netinfo != null && netinfo.isConnected();
        }
        return false;
    }

    /**
     * 获取ip地址.
     *
     * @return ip地址
     */
    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "get ip err", e);
        }
        return "";
    }

    /**
     * 获取mac地址
     *
     * @return mac地址
     */
    public static String getMacAddress(Context context) {
        String mac;
        try {
            mac = FileUtils.loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            Log.e(TAG, "can't read /sys/class/net/eth0/address");
            mac = getMacFromNetworkInterface();
            if (mac == null) {
                mac = getMacFromWifiManager(context);
            }
        }
        if (StringUtils.isEmpty(mac)) {
            Toast.makeText(context, "Cannot get MAC address!", Toast.LENGTH_LONG).show();
        }
        return mac;
    }

    /**
     * 从WifiManager获取MAC地址.
     *
     * @return MAC地址
     */
    @SuppressLint("HardwareIds")
    private static String getMacFromWifiManager(Context context) {
        WifiManager wifi = (WifiManager) context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi != null ? wifi.getConnectionInfo() : null;
        return info != null ? info.getMacAddress() : null;
    }


    /**
     * 从NetworkInterface获取设备的mac地址（android6.0以上专用）
     *
     * @return mac地址
     */
    private static String getMacFromNetworkInterface() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null;
        }
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        String mac = null;
        NetworkInterface iF;
        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                mac = macBytesToString(iF.getHardwareAddress());
                if (mac != null) {
                    break;
                }
            } catch (SocketException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return mac;
    }

    /***
     * byte转为String
     * @param bytes byte数组
     * @return 字符串
     */
    private static String macBytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }


}
