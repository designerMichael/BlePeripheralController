package com.gzshixiang.common.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


public class PermissionActivity extends Activity {
    public static final String KEY_PERMISSIONS = "permissions";
    public static final String KEY_CALLBACK = "callback";
    private static final int RC_REQUEST_PERMISSION = 100;
    private static PermissionCallback CALLBACK;

    private PermissionCallback permissionCallback;

    public static void request(Context context, String[] permissions, PermissionCallback callback) {

        CALLBACK = callback;
        Log.e("zjr", "PermissionActivity callback = " + CALLBACK);
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(KEY_PERMISSIONS, permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (!intent.hasExtra(KEY_PERMISSIONS)) {
            return;
        }
        String[] permissions = getIntent().getStringArrayExtra(KEY_PERMISSIONS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, RC_REQUEST_PERMISSION);
        }
        Log.e("zjr", "PermissionActivity onCreate = " + CALLBACK);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_REQUEST_PERMISSION) {
            return;
        }
        boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];
        for (int i = 0; i < permissions.length; ++i) {
            shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
        }
        this.onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
    }


    @TargetApi(Build.VERSION_CODES.M)
    void onRequestPermissionsResult(String[] permissions, int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        Log.e("zjr", "PermissionActivity onRequestPermissionsResult = " + CALLBACK);
        if (CALLBACK == null) {
            return;
        }
        int length = permissions.length;
        int granted = 0;
        for (int i = 0; i < length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale[i] == true) {
                    CALLBACK.shouldShowRational(permissions[i]);
                } else {
                    CALLBACK.onPermissonReject(permissions[i]);
                }
            } else {
                granted++;
            }
        }
        if (granted == length) {
            CALLBACK.onPermissionGranted();
        }
        finish();
    }
}
