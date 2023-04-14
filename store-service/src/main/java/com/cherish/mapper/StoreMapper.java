package com.cherish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cherish.entity.store.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

}
