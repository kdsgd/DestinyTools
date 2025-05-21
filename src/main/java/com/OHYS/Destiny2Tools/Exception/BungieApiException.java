package com.OHYS.Destiny2Tools.Exception;

import lombok.Getter;

@Getter
public class BungieApiException extends Exception {
    // 新增获取状态码的方法
    private final int statusCode;  // 新增状态码字段

    // 构造方法1：带状态码
    public BungieApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    // 构造方法2：带状态码和原因（用于HTTP错误）
    public BungieApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    // 构造方法3：不带状态码（用于非HTTP错误）
    public BungieApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;  // -1 表示非HTTP错误
    }

    public BungieApiException(String s) {

        statusCode = 0;
    }

}