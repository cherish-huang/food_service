package com.cherish.error;

public enum CartErrCode implements BaseErr{

    ERROR_GEN_ORDER_ID(100001, "生成id失败"),
    ERROR_UPDATE_DB(100002, "加购更新db失败"),
    ERROR_INSERT_DB(100003, "加购插入db失败"),

    ERROR_DELETE_DB(100004, "减购删除db失败"),
    ERROR_EMPTY_DB(100005, "清空购物车db失败"),
    ERROR_REDUCE_CART(100006, "错误的减购");


    private int code;
    private String message;

    CartErrCode(int code, String message){
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
