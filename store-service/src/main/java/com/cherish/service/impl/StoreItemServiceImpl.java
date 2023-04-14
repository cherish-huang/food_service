package com.cherish.service.impl;

import com.cherish.component.rpc.RpcHandler;
import com.cherish.dao.StoreDao;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.StoreItem;
import com.cherish.service.store.StoreItemService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class StoreItemServiceImpl implements StoreItemService {

    @Autowired
    private StoreDao storeDao;
    @Override
    @RpcHandler
    public Response<List<StoreItem>> queryItemsByStore(Long storeId) {
        return Response.success(storeDao.queryItemsByStore(storeId));
    }

    @Override
    @RpcHandler
    public Response<StoreItem> queryItem(Long itemId) {
        return Response.success(storeDao.queryItem(itemId));
    }

    @Override
    @RpcHandler
    public Response<List<StoreItem>> queryItems(List<Long> itemId) {
        return Response.success(storeDao.queryItems(itemId));
    }
}
