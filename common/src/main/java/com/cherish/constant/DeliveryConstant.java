package com.cherish.constant;

public class DeliveryConstant {
    public static class DeliveryOrderStatus {
        public final static int CREATED = 1;
        public final static int CONFIRMED = 2;
        public final static int ARRIVED = 3;
        public final static int PICKED = 4;
        public final static int COMPLETED = 5;
        public final static int CANCELED = 6;

    }

    public static class DriverOrderStatus {
        public final static int CONFIRMED = 1;
        public final static int COMPLETED = 2;
        public final static int CANCELED = 3;

    }

    public static class FlagBit {
        public final static int DFF_BIT = 0;
    }

    public static class KafkaEvent{
        public final static String DELIVERY_CREATE = "delivery.create";
        public final static String DELIVERY_ACCEPT = "delivery.accept ";
        public final static String DELIVERY_ARRIVE = "delivery.arrive";
        public final static String DELIVERY_PICKUP = "delivery.pickup";
        public final static String DELIVERY_COMPLETE = "delivery.complete";
        public final static String DELIVERY_CANCEL = "delivery.cancel";
    }

    public static class RedisPrefix{
        public final static String AVAILABLE_DRIVERS = "available_drivers";
    }

    public static class DelayTask{
        public final static String delivery_arrive_task = "delivery.arrive.task";
        public final static String delivery_pickup_task = "delivery.pickup.task";
        public final static String delivery_complete_task = "delivery.complete.task";
    }

    public static class CancelSource{
        public final static int SYSTEM = 1;
    }

    public static class CancelReason{
        public final static int ASSIGN_TIMEOUT = 1;
        public final static int DELIVER_TIMEOUT = 2;
    }
}
