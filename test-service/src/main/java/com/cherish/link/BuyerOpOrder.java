package com.cherish.link;

import com.cherish.TestApp;
import com.cherish.component.producer.KafkaProducer;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.cart.CartItem;
import com.cherish.entity.order.DeliveryAddress;
import com.cherish.entity.order.Order;
import com.cherish.entity.order.PayOrderReq;
import com.cherish.entity.order.SubmitOrderReq;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;
import com.cherish.entity.store.StoreItem;
import com.cherish.service.cart.CartService;
import com.cherish.service.order.OrderService;
import com.cherish.service.store.StoreItemService;
import com.cherish.service.store.StoreService;
import com.cherish.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class BuyerOpOrder {

    private RestTemplate restTemplate = new RestTemplate();
    Logger logger = LoggerFactory.getLogger(BuyerOpOrder.class);

    @Test
    public void buyerSubmitOrders() throws InterruptedException {
        int num = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        for(int i=0; i< num ; i++){
            new Thread(()->{
                try{
                    buyerSubmitOrder();
                }finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
    }

    private void buyerSubmitOrder(){

        long buyerId = (int)(Math.random() * 1000000000) + 1;

        //1. 遍历所有的商铺
        Response<List<Store>> storeRes = JsonUtils.toObject(restTemplate.getForObject("http://localhost:8001/store/stores", String.class), new TypeToken<Response<List<Store>>>() {}.getType());
        if(!storeRes.isSuccess()){
            logger.error("全链路查询所有商铺失败,返回错误码:{},错误信息:{}", storeRes.getCode(), storeRes.getMessage());
            return;
        }
        logger.info("--------全链路查询所有商铺成功, 商铺ids:{}--------", storeRes.getData().stream().map(Store::getId).collect(Collectors.toList()));
        //2. 随机选一个商铺查看该商铺下所有的商品
        int storeIndex = (int)(Math.random() * storeRes.getData().size());
        Store selectStore = storeRes.getData().get(storeIndex);
        Response<List<StoreItem>> itemRes = JsonUtils.toObject(restTemplate.getForObject(String.format("http://localhost:8001/store/%s/items", selectStore.getId()),
                                                             String.class), new TypeToken<Response<List<StoreItem>>>() {}.getType());
        if(!itemRes.isSuccess()){
            logger.error("全链路查询商铺下所有商品失败,返回错误码:{},错误信息:{}", itemRes.getCode(), itemRes.getMessage());
            return;
        }
        logger.info("--------全链路查询商铺下所有商品成功, 商铺id:{}, 商品ids:{}--------", selectStore.getId(), itemRes.getData().stream().map(StoreItem::getId).collect(Collectors.toList()));

        //3. 随机选择菜品添加购物车并且查询购物车的商品
        for(StoreItem storeItem: itemRes.getData()){
            int quantity = (int)(Math.random()) * 10 + 1;
            CartItem cartItem = CartItem.builder()
                    .itemId(storeItem.getId())
                    .buyerId(buyerId)
                    .quantity(quantity)
                    .storeId(storeItem.getStoreId())
                    .build();
            ;
            Response<Void> addCartItemRes = JsonUtils.toObject(restTemplate.postForObject("http://localhost:8001/cart/addCartItem", cartItem, String.class),
                                                                new TypeToken<Response<Void>>() {}.getType());
            if(!addCartItemRes.isSuccess()){
                logger.error("全链路加购失败,返回错误码:{},错误信息:{}", addCartItemRes.getCode(), addCartItemRes.getMessage());
                return;
            }
            logger.info("--------全链路加购成功,商品id:{}, 数量:{}--------", storeItem.getId(), quantity);
        }
        Response<List<CartItem>> cartItems = JsonUtils.toObject(restTemplate.getForObject(String.format("http://localhost:8001/cart/getCartItems/%s/%s", buyerId, selectStore.getId()),
                                                                String.class), new TypeToken<Response<List<CartItem>>>(){}.getType());
        if(!cartItems.isSuccess()){
            logger.error("全链路查询购物车失败,返回错误码:{},错误信息:{}", cartItems.getCode(), cartItems.getMessage());
            return;
        }else{
            for(CartItem cartItem: cartItems.getData()){
                logger.info("加购菜品id={},数量={}", cartItem.getItemId(), cartItem.getQuantity());
            }
        }

        //4. 下单
        SubmitOrderReq submitOrderReq = SubmitOrderReq.builder()
                .buyerId(buyerId)
                .StoreId(selectStore.getId())
                .deliveryAddress(DeliveryAddress.builder()
                        .name("cherish")
                        .phone("10086")
                        .address("深圳市南山区")
                        .latitude(10.11)
                        .longitude(11.00)
                        .build())
                .cartItems(cartItems.getData())
                .build();
        Response<Long> submitOrderRes = JsonUtils.toObject(restTemplate.postForObject("http://localhost:8001/order/submit", submitOrderReq, String.class),
                                        new TypeToken<Response<Long>>(){}.getType());
        if(!submitOrderRes.isSuccess()){
            logger.error("全链路下单失败,返回错误码:{},错误信息:{}", submitOrderRes.getCode(), submitOrderRes.getMessage());
            return;
        }
        logger.info("--------全链路查询下单成功,订单id:{}--------", submitOrderRes.getData());
    }
}
