package com.fileStorage.frontend.network;

import com.fileStorage.common.ByteObject;

@FunctionalInterface
public interface Callback {
    void onReceived(ByteObject byteObject);
}
