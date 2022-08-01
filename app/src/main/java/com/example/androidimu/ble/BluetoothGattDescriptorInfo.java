package com.example.androidimu.ble;

import java.util.UUID;

/**
 * Created by 601042 on 2018/6/28.
 * <p>
 * ble的service的Characteristic的Descriptor信息
 */

public class BluetoothGattDescriptorInfo {

    //描述者的UUID
    private UUID uuid;
    //描述者的权限
    public int permissions;

    public BluetoothGattDescriptorInfo(UUID uuid, int permissions) {
        this.uuid = uuid;
        this.permissions = permissions;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "BluetoothGattDescriptorInfo{" +
                "uuid=" + uuid +
                ", permissions=" + permissions +
                '}';
    }
}
