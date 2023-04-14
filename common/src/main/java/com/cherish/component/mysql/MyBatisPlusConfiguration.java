package com.cherish.component.mysql;

import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlus配置类
 */

@Configuration
@ConditionalOnClass(TableNameHandler.class)
@ConditionalOnBean(TableNameHandler.class)
public class MyBatisPlusConfiguration {

    /**
     * 注册动态表名拦截器
     * @return 动态表名拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(Map<String, TableNameHandler> tableNameHandlers) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandlerMap(tableNameHandlers);

        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }
}