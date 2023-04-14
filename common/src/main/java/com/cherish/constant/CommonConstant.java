package com.cherish.constant;

public final class CommonConstant {
    public static class KafkaEvent{
        public static final String EVENT_TYPE_NAME = "kafka_event_type";
    }

    public static class IdServiceType{
        public static final int ID_CART_SERVICE_TYPE = 10000;
        public static final int ID_ORDER_SERVICE_TYPE = 10001;
        public static final int ID_DELIVERY_SERVICE_TYPE = 10002;

        public static final int[] ID_SERVICE_TYPES = {ID_CART_SERVICE_TYPE, ID_ORDER_SERVICE_TYPE, ID_DELIVERY_SERVICE_TYPE};
    }

    public static class DbShardName{
        public static final String CART_ITEM = "t_cart_item_%04d";
        public static final String ORDER = "t_order_%04d";
        public static final String ORDER_STORE = "t_order_store_%04d";
        public static final String ORDER_ITEM = "t_order_item_%04d";

        public static final String DELIVERY_ORDER = "t_delivery_order_%04d";
        public static final String DRIVER_ORDER = "t_driver_order_%04d";
    }

    public static class DbShardNum{
        public static final int CART_ITEM = 10;
        public static final int ORDER = 10;
        public static final int ORDER_STORE = 10;
        public static final int ORDER_ITEM = 10;
        public static final int DELIVERY_ORDER = 10;

        public static final int DRIVER_ORDER = 10;
    }

    public static class KafkaTopic{
        public final static String ORDER_EVENT = "order_event";
        public final static String DELIVERY_EVENT = "delivery_event";

    }
}
