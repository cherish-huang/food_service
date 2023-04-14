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

    @Bean(CommonConstant.DbShardName.DELIVERY_ORDER)
    public ShardTableNameHandler idTableNameHandler01(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.DELIVERY_ORDER)));
    }

    @Bean(CommonConstant.DbShardName.DRIVER_ORDER)
    public ShardTableNameHandler idTableNameHandler02(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.DRIVER_ORDER)));
    }

}
