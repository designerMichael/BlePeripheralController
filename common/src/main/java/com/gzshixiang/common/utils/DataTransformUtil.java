package com.gzshixiang.common.utils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Copyright © 1997 - 2019 Gosuncn. All Rights Reserved.
 *
 * @author user
 * @description 数据转换工具类
 * @date 2021/1/6
 * @email 2751358839@qq.com
 */

public class DataTransformUtil {
    private static final String TAG = "DataTransformUtil";

    /**
     * List<Float>  to float[]
     *
     * @param floatList
     * @return
     */
    public static float[] listTofloatArray(List<Float> floatList) {
        float[] floatArray = new float[floatList.size()];
        int i = 0;
        for (Float f : floatList) {
            floatArray[i++] = (f != null ? f : Float.NaN);
        }
        return floatArray;
    }


    /**
     * ByteBuffer to byte[]
     *
     * @param byteBuffer
     * @return
     */
    public static byte[] byteBufferToByteArray(ByteBuffer byteBuffer) {
        //byteBuffer.flip();

        byte[] byteArray = new byte[byteBuffer.remaining()];
      //  Log.d(TAG, "byteBufferToByteArray: " + byteArray.length);
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = byteBuffer.get();
        }
        return byteArray;
    }
}
