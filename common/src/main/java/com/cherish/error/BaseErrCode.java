package com.cherish.error;

public enum BaseErrCode implements BaseErr{

    SUCCESS(0, "成功"),
    UNKNOWN_ERROR(1, "服务未知错误"),

    RESOURCE_LIMIT(2, "服务被流控"),

    UNKNOWN_SERVICE(3, "未知的服务");

    private int code;
    private String message;

    BaseErrCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
