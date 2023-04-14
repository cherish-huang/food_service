package com.cherish.link;

import com.cherish.TestApp;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.MerchantConfirmReq;
import com.cherish.entity.order.Order;
import com.cherish.entity.order.QueryOrderByStoreReq;
import com.cherish.entity.order.QueryOrderByStoreRes;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;
import com.cherish.service.order.OrderService;
import com.cherish.service.store.StoreService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class MerchantOpOrder {

    @DubboReference(check = false)
    private StoreService storeService;

    @DubboReference(check = false)
    private OrderService orderService;

    Logger logger = LoggerFactory.getLogger(MerchantOpOrder.class);

    @Test
    public void merchantOpOrder() throws InterruptedException {
        Response<List<Store>> storeResponse = storeService.queryAllStores();
        if(!storeResponse.isSuccess()) {
            logger.error("merchantViewConfirmOrder queryAllStores失败,code={}", storeResponse.getCode());
            return;
        }
        while (true){
            // 循环遍历每个商家
            for(Store store: storeResponse.getData()){
                boolean hasMore = true;
                long lastId = 0;
                while (hasMore){
                    QueryOrderByStoreReq queryOrderByStoreReq = QueryOrderByStoreReq.builder()
                            .storeId(store.getId())
                            .filterType(OrderConstant.QueryStoreFilterType.QUERY_NEW_ORDERS)
                            .lastId(lastId)
                            .size(1)
                            .build();
                    Response<QueryOrderByStoreRes> queryOrderByStoreResResponse = orderService.queryOrderByStore(queryOrderByStoreReq);
                    if(!queryOrderByStoreResResponse.isSuccess()){
                        logger.error("merchantViewConfirmOrder queryOrderByStore失败,storeId={},code={}", store.getId(), queryOrderByStoreResResponse.getCode());
                        break;
                    }
                    if(queryOrderByStoreResResponse.getData().getOrders().isEmpty()){
//                        logger.info("merchantViewConfirmOrder没有新的订单,storeId={}", store.getId());
                        break;
                    }
                    for(Order order: queryOrderByStoreResResponse.getData().getOrders()){
                        logger.info("merchantViewConfirmOrder接受到新的订单，storeId={},orderId={}", store.getId(), order.getId());
                        //丢到一个线程随机过多久之后模拟商家手动confirm
                        confirmOrder(order);
                    }
                    hasMore = queryOrderByStoreResResponse.getData().isHasMore();
                    lastId = queryOrderByStoreResResponse.getData().getLastId();
                }
            }
            Thread.sleep(60 * 1000);
        }
    }

    private void confirmOrder(Order order){
        //在1～3分钟内随机延时
//        long delayTime = (new Random().nextInt(3) + 1) * 60 * 1000;
//        try { Thread.sleep(delayTime);}catch (Exception exp){}
        Response<Void> confirmOrderResponse = orderService.merchantConfirmOrder(MerchantConfirmReq.builder().orderId(order.getId()).build());
        if(confirmOrderResponse.isSuccess()){
            logger.info("confirmOrder成功,orderId={}", order.getId());
        }else{
            logger.warn("confirmOrder失败,orderId={}，code={}", order.getId(), confirmOrderResponse.getCode());
        }
    }

}
