package com.cherish.consumer.gds;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.cherish.component.consumer.ICanalConsumer;
import com.cherish.dao.ItemEsRepository;
import com.cherish.entity.ItemEs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//@Component
public class CanalConsumer implements ICanalConsumer {

//    @Autowired
    private ItemEsRepository itemEsRepository;

    Logger logger = LoggerFactory.getLogger(CanalConsumer.class);

    @Override
    public void handle(List<CanalEntry.Entry> entrys) throws Exception {
        for(CanalEntry.Entry entry : entrys){
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) continue;
            CanalEntry.RowChange rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            CanalEntry.EventType eventType = rowChage.getEventType();
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                // 删数据
                if (eventType == CanalEntry.EventType.DELETE) {
                    itemEsRepository.deleteById(rowData.getBeforeColumns(0).getValue());
                    logger.info("删除es成功, id={}", rowData.getBeforeColumns(0).getValue());
                }
                // 插入数据或者删除数据
                else if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                    ItemEs itemEs = ItemEs.builder()
                            .id(rowData.getAfterColumns(0).getValue())
                            .name(rowData.getAfterColumns(1).getValue())
                            .price(rowData.getAfterColumns(2).getValue())
                            .price(rowData.getAfterColumns(3).getValue())
                            .build();
                    itemEsRepository.save(itemEs);
                    logger.info("保存es成功, id={}", rowData.getAfterColumns(0).getValue());
                }
            }
        }
    }
}
