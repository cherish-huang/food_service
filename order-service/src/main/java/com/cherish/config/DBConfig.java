package com.cherish.config;

import com.cherish.component.mysql.ShardTableNameHandler;
import com.cherish.constant.CommonConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Configuration
public class DBConfig {

    @Bean(CommonConstant.DbShardName.ORDER)
    public ShardTableNameHandler idTableNameHandler01(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.ORDER)));
    }

    @Bean(CommonConstant.DbShardName.ORDER_STORE)
    public ShardTableNameHandler idTableNameHandler02(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.ORDER_STORE)));
    }

    @Bean(CommonConstant.DbShardName.ORDER_ITEM)
    public ShardTableNameHandler idTableNameHandler03(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.ORDER_ITEM)));
    }

}
