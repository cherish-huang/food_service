package com.cherish.dao;

import com.cherish.entity.ItemEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ItemEsRepository extends ElasticsearchRepository<ItemEs, String> {

}
