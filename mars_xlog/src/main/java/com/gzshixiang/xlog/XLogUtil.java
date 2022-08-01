package com.gzshixiang.xlog;

import android.content.Context;
import android.os.Environment;



import com.gzshixiang.common.utils.FileUtils;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import java.io.File;

/**
 * Copyright © 2019 - 2021 GZShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2021/8/4
 * @email 2751358839@qq.com
 */

public class XLogUtil {
//    static {
//        System.loadLibrary("c++_shared");
//        System.loadLibrary("marsxlog");
//    }

    private static final String TAG = "XLogUtil";

    private static final String XLOG_NAME_PREFIX = "XLog";

    private static String sLogFolder;
    // this is necessary, or may crash for SIGBUS
    private static String sCachePath;

    public static boolean init(Context context) {
        initFileDir(context);
        //init xlogConfig
        /*Xlog.XLogConfig logConfig = new Xlog.XLogConfig();
        logConfig.mode = Xlog.AppednerModeAsync;
        logConfig.logdir = sLogFolder;
        logConfig.nameprefix = XLOG_NAME_PREFIX;
        logConfig.pubkey = "";
        logConfig.compressmode = Xlog.ZLIB_MODE;
        logConfig.compresslevel = Xlog.COMPRESS_LEVEL1;
        logConfig.cachedir = sCachePath;
        logConfig.cachedays = 30;
        Xlog log = new Xlog();
        long nativeLogPtr = log.newXlogInstance(logConfig);


        if (BuildConfig.DEBUG) {
            logConfig.level = Xlog.LEVEL_VERBOSE;
            log.setConsoleLogOpen(nativeLogPtr,true);
        } else {
            logConfig.level = Xlog.LEVEL_INFO;
            log.setConsoleLogOpen(nativeLogPtr,false);
        }
        Log.setLogImp(log);*/
        Log.setLogImp(new Xlog());

        //全局配置
        Xlog.open(true,Log.LEVEL_VERBOSE,
                Xlog.AppednerModeAsync,
                sCachePath, sLogFolder, XLOG_NAME_PREFIX, "");
        Xlog.setConsoleLogOpen(true);
       /* if (BuildConfig.DEBUG) {
            Log.appenderOpen(Log.LEVEL_VERBOSE, Xlog.AppednerModeAsync, sCachePath, sLogFolder, XLOG_NAME_PREFIX, 30);
            Log.setConsoleLogOpen(true);
        } else {
            Log.appenderOpen(Log.LEVEL_INFO, Xlog.AppednerModeAsync, sCachePath, sLogFolder, XLOG_NAME_PREFIX, 30);
            Log.setConsoleLogOpen(true);
        }*/
        Log.d(TAG, Log.getSysInfo());

        Log.appenderFlush(false);



        return true;
    }


    private static void initFileDir(Context context) {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                sLogFolder = context.getExternalFilesDir("xlog").getAbsolutePath();
                sCachePath = context.getExternalFilesDir("xlog_cache").getAbsolutePath();
            } else {
                sLogFolder = context.getCacheDir() + File.separator + "xlog";
                sCachePath = context.getCacheDir() + File.separator + "xlog_cache";
            }

            FileUtils.createOrExistsDir(sLogFolder);
            FileUtils.createOrExistsFile(sCachePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearCacheDir(){
        FileUtils.deleteAllInDir(sLogFolder);
        FileUtils.deleteAllInDir(sCachePath);
    }

}
