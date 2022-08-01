package com.gzshixiang.common.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Copyright ©  Shixiang. All Rights Reserved.
 *
 * @author zhongjr
 * @description 关闭io 工具类
 * @date 2020/8/21
 * @email 2751358839@qq.com
 */

public class CloseIoUtils {

    /**
     * 关闭IO
     *
     * @param closeables closeables
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
