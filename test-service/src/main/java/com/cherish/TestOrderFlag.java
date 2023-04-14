package com.cherish;

import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class TestOrderFlag {

    @Test
    public void testFlag(){
        Order order = Order.builder().build();
//        order.setAutoConfirm();
//        order.setDff();
//        order.setOvertimeCancel();
        System.out.println(order.isDff());
        System.out.println(order.isAutoConfirm());
        System.out.println(order.isOvertimeCancel());
    }
}
