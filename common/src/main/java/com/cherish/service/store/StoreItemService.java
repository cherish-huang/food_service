package com.cherish.service.store;

import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.StoreItem;

import java.util.List;

public interface StoreItemService {
    Response<List<StoreItem>> queryItemsByStore(Long storeId);

    Response<StoreItem> queryItem(Long itemId);

    Response<List<StoreItem>> queryItems(List<Long> itemId);
}
