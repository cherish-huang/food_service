package com.cherish.worker;

import java.util.ArrayList;
import java.util.List;

public class SnowflakeIdWorker {

    // 开始时间戳（2020-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1577836800000L;
    // 机器 ID 所占的位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据中心 ID 所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 序列号所占的位数
    private static final long SEQUENCE_BITS = 12L;
    // 机器 ID 向左移动的位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心 ID 向左移动的位数
    private static final long DATA_CENTER_ID_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;
    // 时间戳向左移动的位数
    private static final long TIMESTAMP_SHIFT = DATA_CENTER_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS;

    // 用于生成序列号的掩码
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1L;
    // 数据中心 ID
    private final long dataCenterId;
    // 机器 ID
    private final long workerId;
    // 序列号
    private long sequence = 0L;
    // 上一次生成 ID 的时间戳
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param dataCenterId 数据中心ID (0~31)
     * @param workerId     工作ID (0~31)
     */
    public SnowflakeIdWorker(long dataCenterId, long workerId) {
        if (dataCenterId > ((1L << DATA_CENTER_ID_BITS )-1) || dataCenterId < 0) {
            throw new IllegalArgumentException("datacenterId超过范围");
        }

        if (workerId > ((1L << WORKER_ID_BITS) -1) || workerId < 0) {
            throw new IllegalArgumentException("workerId超过范围");
        }

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized Long nextId() {
        long timestamp = System.currentTimeMillis();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if(sequence == 0){
                // 序列号溢出，等待下一毫秒
                timestamp = waitNextMillis(timestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    public synchronized List<Long> nextNId(int n) {
        List<Long> ids = new ArrayList<>();
        for(int i=0; i < n; i++){
            ids.add(nextId());
        }
        return ids;
    }

    private long waitNextMillis(long timestamp) {
        long nextTimestamp = System.currentTimeMillis();
        while (nextTimestamp <= timestamp) {
            nextTimestamp = System.currentTimeMillis();
        }
        return nextTimestamp;
    }

}