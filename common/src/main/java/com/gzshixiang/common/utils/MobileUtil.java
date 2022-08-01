package com.gzshixiang.common.utils;

/**
 * Copyright © 2019 - 2021 GZShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2021/9/1
 * @email 2751358839@qq.com
 */

public class MobileUtil {

    public static String brand = android.os.Build.BRAND;
    /**
     * 手机品牌
     */
    // 小米
    public static final String PHONE_XIAOMI = "xiaomi";
    // 华为
    public static final String PHONE_HUAWEI1 = "Huawei";
    // 华为
    public static final String PHONE_HUAWEI2 = "HUAWEI";
    // 华为
    public static final String PHONE_HUAWEI3 = "HONOR";

    //一加手机
    public static final String PHONE_ONEPLUS = "OnePlus";
    // 魅族
    public static final String PHONE_MEIZU = "Meizu";
    // 索尼
    public static final String PHONE_SONY = "sony";
    // 三星
    public static final String PHONE_SAMSUNG = "samsung";
    // LG
    public static final String PHONE_LG = "lg";
    // HTC
    public static final String PHONE_HTC = "htc";
    // NOVA
    public static final String PHONE_NOVA = "nova";
    // OPPO
    public static final String PHONE_OPPO = "OPPO";
    // 乐视
    public static final String PHONE_LeMobile = "LeMobile";
    // 联想
    public static final String PHONE_LENOVO = "lenovo";


    public static boolean isOnePlus() {
        if (PHONE_ONEPLUS.equalsIgnoreCase(brand)) {
            return true;
        }
        return false;
    }
}
