package com.cherish.component.mysql;

import java.util.Set;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;

public class ShardTableNameHandler implements TableNameHandler {
    /**
     * 哪些表可以使用这个动态表名规则
     * 表名-> 分几张表
     */
    private Set<String> shardTableNames;

    public ShardTableNameHandler(Set<String> shardTableNames){
        this.shardTableNames = shardTableNames;
    }
    //避免多线程数据冲突，使用ThreadLocal
    private static final ThreadLocal<Integer> tShardId = new ThreadLocal<>();


    // 在具体分表的shardId
    public static void setCurrentShardId(int shardId) {
        tShardId.set(shardId);
    }

    private static long getCurrentShardId() {
        return tShardId.get();
    }

    public static void removeCurrentId() {
        tShardId.remove();
    }

    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (!shardTableNames.contains(tableName)) {
            return tableName;
        }
        String shardTableName = String.format(tableName, getCurrentShardId());
        // 删除当前线程的shardId
        removeCurrentId();
        return shardTableName;
    }
}
