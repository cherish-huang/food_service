package com.cherish.error;

public enum DeliveryErrCode implements BaseErr{
    ERROR_QUERY_STORE(100001, "查询商家失败"),
    ERROR_GEN_DELIVERY_ID(100002, "生成运单id失败"),
    ERROR_INSERT_DELIVERY(100003, "生成运单失败"),
    ERROR_GET_DELIVERY(100003, "获取运单失败"),

    ERROR_DELIVERY_STATUS(100004, "错误的运单状态"),

    ERROR_DELIVERY_ACCESS(100005, "错误的运单状态"),

    ERROR_GET_ORDER(100005, "查询订单失败"),
    ERROR_GEN_DRIVER_ORDER_ID(100010, "生成driverOrder id失败"),

    ERROR_ACCEPT_DELIVERY_ORDER(100011, "生成driverOrder失败");
    private int code;
    private String message;

    DeliveryErrCode(int code, String message){
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
