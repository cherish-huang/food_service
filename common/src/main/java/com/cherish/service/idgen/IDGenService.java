package com.cherish.service.idgen;

import com.cherish.entity.rpc.Response;

import java.util.List;

public interface IDGenService {
    Response<Long> genID(int serviceType);

    Response<List<Long>> batchGenID(int serviceType, int num);
}
