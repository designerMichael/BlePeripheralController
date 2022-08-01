package com.gzshixiang.common.permission;

import java.io.Serializable;

public interface PermissionCallback extends Serializable {
    void onPermissionGranted();

    void shouldShowRational(String permisson);

    void onPermissonReject(String permisson);
}
