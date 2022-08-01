package com.gzshixiang.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Copyright ©  Shixiang. All Rights Reserved.
 *
 * @author zhongjr
 * @description
 * @date 2020/8/21
 * @email 2751358839@qq.com
 */

public class ProcessUtils {
    private static ActivityManager mActivityManager;

    public static final boolean isProcessRunning(Context context, String processName) {

        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (TextUtils.isEmpty(processName)) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo process : mActivityManager.getRunningAppProcesses()) {
            if (process.processName.equals(processName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
