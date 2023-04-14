package com.cherish.service.store;

import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;

import java.util.List;

public interface StoreService {
    Response<List<Store>> queryAllStores();

    Response<Store> queryStore(Long storeId);
}
