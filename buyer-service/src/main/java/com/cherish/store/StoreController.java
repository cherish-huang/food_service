package com.cherish.store;

import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;
import com.cherish.service.store.StoreService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreController {

    @DubboReference(check = false)
    private StoreService storeService;

    @GetMapping("/store/stores")
    public Response<List<Store>> stores(){
        return storeService.queryAllStores();
    }
}
