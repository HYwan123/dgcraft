package com.example.wan_try.dglab.api;

//生成和显示二维码
public interface QRCodeHandler {
    String handleQrCode(String dgLabUrl, String wsUrl, String connId);

}
