package com.gzshixiang.common.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.gzshixiang.common.AndroidContext;


/**
 * Copyright © 2019 - 2021 GZShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2021/3/9
 * @email 2751358839@qq.com
 */

public class ToastUtil {
    private static final String TAG = "ToastUtil";

    public static void toastOnUIThread(int resId) {
        Context context = AndroidContext.get();
        if (context == null) {
            Log.e(TAG, "toastOnUIThread: Contex is NULL");
            return;
        }
        String text = context.getString(resId);
        toastOnUIThread(text);
    }

    public static void toastOnUIThread(String text) {
        toastOnUIThread(text, Toast.LENGTH_SHORT);
    }


    public static void toastOnUIThread(String text, int toastTime) {
        Context context = AndroidContext.get();
        if (context == null) {
            Log.e(TAG, "toastOnUIThread: Contex is NULL");
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, text, toastTime).show());
    }
}
