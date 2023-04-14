package com.cherish.service.impl;

import com.cherish.component.rpc.RpcHandler;
import com.cherish.dao.StoreDao;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;
import com.cherish.service.store.StoreService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreDao storeDao;

    @RpcHandler
    @Override
    public Response<List<Store>> queryAllStores() {
        return Response.success(storeDao.queryAllStores());
    }

    @RpcHandler
    @Override
    public Response<Store> queryStore(Long storeId) {
        return Response.success(storeDao.queryStore(storeId));
    }
}
