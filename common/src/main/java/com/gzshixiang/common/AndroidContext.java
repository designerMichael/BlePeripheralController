package com.gzshixiang.common;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Copyright ©  Shixiang. All Rights Reserved.
 *
 * @author zhongjr
 * @description
 * @date 2020/8/21
 * @email 2751358839@qq.com
 */

public class AndroidContext {
    private static final String TAG = "AndroidContext";
    private static WeakReference<Context> mContext;
    private static WeakReference<Activity> mActivity;

    public static boolean sInitXlog = false;

    public static void initialize(Context context) {
        mContext = new WeakReference<>(context.getApplicationContext());
        Log.d(TAG, "AndroidContext initialize: pid = " + android.os.Process.myPid());

    }

    public static void initActivity(Activity activity) {
        Log.d(TAG, "AndroidContext initializeActivity: pid = " + android.os.Process.myPid());
        mActivity = new WeakReference<>(activity);
    }

    public static Context get() {
        return mContext.get();
    }

    public static Activity getActivity() {
        if (mActivity == null) {
            return null;
        }
        return mActivity.get();
    }
}
