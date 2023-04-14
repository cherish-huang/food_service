package com.cherish.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.entity.store.Store;
import com.cherish.entity.store.StoreItem;
import com.cherish.mapper.StoreItemMapper;
import com.cherish.mapper.StoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StoreDao {

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StoreItemMapper storeItemMapper;

    public List<Store> queryAllStores(){
        return storeMapper.selectList(Wrappers.lambdaQuery());
    }

    public Store queryStore(long storeId){
        return storeMapper.selectById(storeId);
    }

    public List<StoreItem> queryItemsByStore(long storeId){
        return storeItemMapper.selectList(Wrappers.<StoreItem>lambdaQuery().eq(StoreItem::getStoreId, storeId));
    }

    public StoreItem queryItem(long itemId){
        return storeItemMapper.selectById(itemId);
    }

    public List<StoreItem> queryItems(List<Long> itemIds){
        return storeItemMapper.selectBatchIds(itemIds);
    }
}
