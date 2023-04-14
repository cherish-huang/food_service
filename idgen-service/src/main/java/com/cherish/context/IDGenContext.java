package com.cherish.context;

import com.cherish.constant.CommonConstant;
import com.cherish.worker.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IDGenContext {

    @Value("${dataCenterId}")
    private long dataCenterId;

    @Value("${workerId}")
    private long workerId;


    private Map<Integer, SnowflakeIdWorker> idWorkers = new HashMap<>();

    @PostConstruct
    public void init(){
        for(int serviceType: CommonConstant.IdServiceType.ID_SERVICE_TYPES){
            idWorkers.put(serviceType, new SnowflakeIdWorker(dataCenterId, workerId));
        }
    }

    public Long genID(int serviceType){
        return idWorkers.get(serviceType).nextId();
    }

    public List<Long> batchGenID(int serviceType, int num){
        return idWorkers.get(serviceType).nextNId(num);
    }

    public boolean isExistService(int serviceType){
        return idWorkers.containsKey(serviceType);
    }

}
