package com.gzshixiang.common.utils;

/**
 * Copyright © 2019 - 2021 GZShiXiang. All Rights Reserved.
 *
 * @author MichaelZ
 * @description
 * @date 2021/2/25
 * @email 2751358839@qq.com
 */

public class TimeUtils {

    /**
     * @return返回微秒
     */
    public static Long getCurrentMicroTime() {
       /* Long cutime = System.currentTimeMillis() * 1000; // 微秒
        Long nanoTime = System.nanoTime(); // 纳秒
        return cutime + (nanoTime - nanoTime / 1000000 * 1000000) / 1000;*/
        return System.nanoTime() / 1000;

    }
}
