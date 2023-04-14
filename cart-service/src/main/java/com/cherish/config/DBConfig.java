package com.cherish.config;

import com.cherish.component.mysql.ShardTableNameHandler;
import com.cherish.constant.CommonConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class DBConfig {

    @Bean(CommonConstant.DbShardName.CART_ITEM)
    public ShardTableNameHandler idTableNameHandler(){
        return new ShardTableNameHandler(new HashSet<>(Arrays.asList(CommonConstant.DbShardName.CART_ITEM)));
    }

}
