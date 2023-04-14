package com.cherish.error;

public enum OrderErrCode implements BaseErr{

    ERROR_QUERY_STORE(100001, "查询商家失败"),
    ERROR_QUERY_ITEM(100002, "查询商品失败"),
    ERROR_GEN_ORDER_ID(100003, "生成订单id失败"),
    ERROR_GEN_ORDER_ITEM_IDS(100004, "批量生成订单item id失败"),
    ERROR_INSERT_ORDER(100005, "批量生成订单item id失败"),
    ORDER_NOT_FOUND(100005, "查询订单不存在"),
    ERROR_CANCEL_STATUS(100006, "订单取消状态错误"),
    ERROR_ORDER_UPDATE_NUM(100007, "订单更新数量错误"),
    ERROR_APPROVE_STATUS(100008, "订单审批状态错误"),
    ERROR_CONFIRM_STATUS(100009, "订单确状态错误"),
    ERROR_DRIVER_ACCEPT_STATUS(100010, "订单接单状态错误"),
    ERROR_PAY_STATUS(100011, "订单支付状态错误"),
    ERROR_DRIVER_ARRIVE_STATUS(100012, "订单到店状态错误"),
    ERROR_DRIVER_PICKUP_STATUS(100013, "订单取餐状态错误"),
    ERROR_DELIVER_STATUS(100014, "订单送达状态错误"),
    ERROR_COMPLETE_STATUS(100015, "订单完成状态错误"),
    ERROR_DEAL_AUTO_CONFIRM_ORDER(100016, "处理自动扭转订单异常"),
    ERROR_DEAL_MANUAL_CONFIRM_ORDER(100017, "处理手动扭转订单异常"),
    ERROR_DEAL_MANUAL_CANCEL_ORDER(100018, "处理手动扭转订单异常");

    private int code;
    private String message;

    OrderErrCode(int code, String message){
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
