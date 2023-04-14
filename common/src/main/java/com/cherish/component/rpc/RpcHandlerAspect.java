package com.cherish.component.rpc;

import com.cherish.entity.rpc.Response;
import com.cherish.error.BaseErrCode;
import com.cherish.error.BaseError;
import com.cherish.utils.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(-1)
@Component
@ConditionalOnProperty(value = "dubbo.protocol.name", havingValue = "dubbo")
public class RpcHandlerAspect {

    Logger logger = LoggerFactory.getLogger(RpcHandlerAspect.class);

    @Pointcut("@annotation(com.cherish.component.rpc.RpcHandler)")
    public void pointcut(){}

    @Around("pointcut()")
    public Object errorHandler(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        RpcHandler rpcHandlerAnno = methodSignature.getMethod().getAnnotation(RpcHandler.class);
        Object result = null;
        try {
            result = joinPoint.proceed();
        }catch (BaseError baseError){
            result = Response.error(baseError.getCode(), baseError.getMessage());
        }catch (Exception exp){
            result = Response.error(BaseErrCode.UNKNOWN_ERROR.getCode(), BaseErrCode.UNKNOWN_ERROR.getMessage());
            logger.error("请求服务:{}, 请求参数:{}", methodSignature.getMethod().getName(), joinPoint.getArgs(), exp);
        }
        if(rpcHandlerAnno.isRecordLog()){
            logger.info("请求服务:{},耗时:{}ms,请求参数:{},返回结果:{}", methodSignature.getMethod().getName(), System.currentTimeMillis() - startTime,
                        joinPoint.getArgs(), JsonUtils.toJson(result));
        }
        return result;
    }
}
