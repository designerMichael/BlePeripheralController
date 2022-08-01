package com.gzshixiang.common.utils;

import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.gzshixiang.common.AndroidContext;

public class AppUtils {
    /**
     * Return whether the app is installed.
     *
     * @param pkgName The name of the package.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isAppInstalled(final String pkgName) {
        if (TextUtils.isEmpty(pkgName)) return false;
        PackageManager pm = AndroidContext.get().getPackageManager();
        try {
            return pm.getApplicationInfo(pkgName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}