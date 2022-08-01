package com.example.androidimu;

import android.app.Application;
import android.view.Gravity;

import com.gzshixiang.common.AndroidContext;
import com.hjq.toast.ToastUtils;

/**
 * Copyright © Guanzhou ShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2022/7/29
 * @email 2751358839@qq.com
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Toast 框架
        ToastUtils.init(this);
        ToastUtils.setGravity(Gravity.BOTTOM);
        AndroidContext.initialize(this);
    }
}

