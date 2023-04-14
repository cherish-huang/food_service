package com.cherish.component.resourcelimit;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cherish.entity.rpc.Response;
import com.cherish.error.BaseErrCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnClass(SentinelResourceAspect.class)
public class SentinelResource extends SentinelResourceAspect {

    private Logger logger = LoggerFactory.getLogger(SentinelResource.class);

    @Override
    protected Object handleBlockException(ProceedingJoinPoint pjp, com.alibaba.csp.sentinel.annotation.SentinelResource annotation, BlockException ex) throws Throwable {
        logger.error("资源被流控, 资源: {}", annotation.value());
        return Response.error(BaseErrCode.RESOURCE_LIMIT);
    }
}
