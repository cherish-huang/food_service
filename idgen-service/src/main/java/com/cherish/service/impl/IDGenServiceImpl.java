package com.cherish.service.impl;

import com.cherish.component.rpc.RpcHandler;
import com.cherish.context.IDGenContext;
import com.cherish.entity.rpc.Response;
import com.cherish.error.BaseErrCode;
import com.cherish.service.idgen.IDGenService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class IDGenServiceImpl implements IDGenService {
    @Autowired
    private IDGenContext idGenContext;

    @Override
    @RpcHandler
    public Response<Long> genID(int serviceType) {
        if(!idGenContext.isExistService(serviceType)){
            return Response.error(BaseErrCode.UNKNOWN_SERVICE);
        }
        return Response.success(idGenContext.genID(serviceType));
    }

    @Override
    @RpcHandler
    public Response<List<Long>> batchGenID(int serviceType, int num) {
        if(!idGenContext.isExistService(serviceType)){
            return Response.error(BaseErrCode.UNKNOWN_SERVICE);
        }
        return Response.success(idGenContext.batchGenID(serviceType, num));
    }
}
