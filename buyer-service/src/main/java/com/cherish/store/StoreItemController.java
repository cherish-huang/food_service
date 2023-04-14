package com.cherish.store;

import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.StoreItem;
import com.cherish.service.store.StoreItemService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreItemController {

    @DubboReference(check = false)
    private StoreItemService storeItemService;

    @GetMapping("/store/{storeId}/items")
    public Response<List<StoreItem>> items(@PathVariable long storeId){
        return storeItemService.queryItemsByStore(storeId);
    }
}
