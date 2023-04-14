package com.cherish.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.cherish.component.resourcelimit.SentinelResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

//    @Bean
//    public SentinelResource rpcSentinelResourceAspect(){
//        return new SentinelResource();
//    }
//
//    @PostConstruct
//    public void init(){
//        List<FlowRule> rules = new ArrayList<>();
//        FlowRule rule = new FlowRule("order.submit")
//                            .setGrade(RuleConstant.FLOW_GRADE_QPS)
//                            .setCount(1);
//        rules.add(rule);
//        FlowRuleManager.loadRules(rules);
//    }
}
