package com.example.wan_try.dglab.api;

import com.example.wan_try.dglab.DGLabClient;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.List;

public interface IDGLabClient<T extends DGLabClient.DGLabContext> {
    BitMatrix genQrCode(String clientID) throws WriterException;

    List<T> getContext(String id);
}


