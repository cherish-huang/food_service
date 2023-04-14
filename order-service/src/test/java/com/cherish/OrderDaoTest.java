package com.cherish;

import com.cherish.dao.OrderDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {OrderApp.class})
public class OrderDaoTest {

    @Autowired
    private OrderDao orderDao;

    @Test
    public void testOrderDao(){
        System.out.println(orderDao.getOrder(424088799060951040L));
    }
}
