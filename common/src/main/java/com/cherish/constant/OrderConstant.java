package com.cherish.constant;

public final class OrderConstant {

    public static class OrderStatus {
        public final static int CREATED = 1;
        public final static int PAID = 2;
        public final static int APPROVED = 3;
        public final static int MERCHANT_CONFIRMED = 4;
        public final static int DRIVER_ACCEPTED = 5;
        public final static int DRIVER_ARRIVED = 6;
        public final static int DRIVER_PICKUPED = 7;
        public final static int DELIVERED = 8;
        public final static int COMPLETED = 9;
        public final static int CANCELED = 10;
    }

    public static class CancelSource {
        public final static int BUYER = 1;
        public final static int MERCHANT = 2;
        public final static int DRIVER = 3;
        public final static int SYSTEM = 4;
    }

    public static class CancelReason {
        // 1~100是买家端取消原因
        public final static int BUYER = 1;
        // 101～200是商家端取消原因
        public final static int MERCHANT_CONFIRM_EXPIRED = 101;
        // 201～300是骑手端取消原因
        public final static int DRIVER_ACCEPT_EXPIRED = 201;
        public final static int DELIVERY_CANCELED = 202;
        // 301~400是系统取消原因
        public final static int PAID_TIMEOUT = 301;
    }

    public static class KafkaEvent{
        public final static String ORDER_CREATE = "order.create";
        public final static String ORDER_PAY = "order.pay";
        public final static String ORDER_APPROVE = "order.approve";
        public final static String ORDER_MERCHANT_CONFIRM = "order.merchant_confirm";
        public final static String ORDER_DRIVER_ACCEPT = "order.driver_accept";
        public final static String ORDER_DRIVER_ARRIVE = "order.driver_arrive";
        public final static String ORDER_DRIVER_PICKUP = "order.driver_pickup";
        public final static String ORDER_DRIVER_DELIVER = "order.driver.deliver";
        public final static String ORDER_COMPLETE = "order.complete";
        public final static String ORDER_CANCEL = "order.cancel";
    }

    public static class QueryStoreFilterType{
        public final static int QUERY_NEW_ORDERS = 1;
        public final static int QUERY_ONGOING_ORDERS = 2;
    }

    public static class DelayTask{
        public final static String ORDER_PAY_EXPIRED_NAME = "order.paid.expired.task";
        public final static int ORDER_PAY_EXPIRED_VALUE = 10 * 60 * 1000;
        public final static String ORDER_MERCHANT_CONFIRM_EXPIRED_NAME = "order.merchant_confirm.expired.task";
        public final static int ORDER_MERCHANT_CONFIRM_EXPIRED_VALUE = 10 * 60 * 1000;

        public final static String ORDER_PAY_NAME = "order.pay.task";
        public final static String ORDER_MERCHANT_CONFIRM_NAME = "order.merchant_confirm.task";
    }

    public static class RedisPrefix{
        public final static String ORDER_BASIC_INFO = "order:%s";
    }

    public static class FlagBit {
        public final static int DFF_BIT = 0;
        public final static int AUTO_CONFIRM_BIT = 1;

        public final static int MERCHANT_OVERTIME_BIT = 2;
    }

    public static class MerchantOverTimeMode{
        public final static int CONFIRM = 0;
        public final static int CANCEL = 1;
    }

}
