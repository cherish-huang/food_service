package com.cherish;

import com.cherish.constant.CommonConstant;
import com.cherish.entity.rpc.Response;
import com.cherish.service.idgen.IDGenService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class IdGenTest {

    @DubboReference(check = false)
    private IDGenService idGenService;

    @Test
    public void testIDGen(){
        for(int i=0 ;i<10; i++){
            Response<Long> response = idGenService.genID(CommonConstant.IdServiceType.ID_CART_SERVICE_TYPE);
            System.out.println(response.getData());
        }
    }
}
