package com.gzshixiang.common.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsUtil {
    private static final String TAG = "AssetsUtil";

    /**
     * 
     * @param context
     * @param assetName
     * @param toSavePath
     * @param pathTree 是否加上assetName做为路径一部分
     * @return
     */
    public static boolean copyFileFromAssets(Context context, String assetName, String toSavePath, boolean pathTree) {
        AssetManager assetManager = context.getAssets();
        return copyDir(assetManager, assetName, assetName, toSavePath, pathTree);
    }

    private static boolean copyDir(AssetManager assetManager, String fileName, String assetName, String toSavePath, boolean pathTree) {
        try {
            if (TextUtils.isEmpty(assetName)) {
                return false;
            }
            String[] files = assetManager.list(assetName);
            if (files == null) {
                return false;
            }
            if (files.length > 0) {
                for (String assetFileName : files) {
                    if (TextUtils.isEmpty(assetFileName)) {
                        continue;
                    }
                    copyDir(assetManager, assetFileName, assetName + File.separator + assetFileName, toSavePath, pathTree);
                }
            } else {
                //pathTree 为true,则加上assetName , 如 /path/gesture_model/xxx_file  若false则为 /path/xxxx_file
                String toPath = toSavePath + File.separator + (pathTree ? assetName : fileName);
                copyFile(assetManager, assetName, toPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void copyFile(AssetManager assetManager, String fileName, String toPath) {
        Log.w("zjr", "fileName = " + fileName + " toPath = " + toPath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        FileUtils.createOrExistsFile(toPath);
        try {
            inputStream = assetManager.open(fileName);
            outputStream = new FileOutputStream(toPath, false);
            byte[] data = new byte[1024];
            int length;
            while ((length = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(outputStream);
            closeStream(inputStream);
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFileFromAssets(Context context, String fileName) {
        if (null == context || null == fileName) {
            return "";
        }
        String result = "";
        AssetManager am = context.getAssets();
        InputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            input = am.open(fileName);
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            output.flush();
            result = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(output);
            closeStream(input);
        }
        return result;
    }
}
