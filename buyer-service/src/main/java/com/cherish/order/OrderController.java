package com.cherish.order;

import com.cherish.entity.order.DeliveryAddress;
import com.cherish.entity.order.Order;
import com.cherish.entity.order.SubmitOrderReq;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @DubboReference(check = false)
    private OrderService orderService;

    @PostMapping("/order/submit")
    public Response<Long> submitOrder(@RequestBody SubmitOrderReq submitOrderReq){
        return orderService.submitOrder(submitOrderReq);
    }
}
