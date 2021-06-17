package com.itheima.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.itheima.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@CanalEventListener
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_goods", table = "tb_spu")
    public void listenGoodsChange(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //获取改变之前的数据
        Map<String, String> olderData = new HashMap<>();
        rowData.getBeforeColumnsList().forEach((data) -> olderData.put(data.getName(), data.getValue()));

        //获取改变之后的数据
        Map<String, String> newerData = new HashMap<>();
        rowData.getAfterColumnsList().forEach((data) -> newerData.put(data.getName(), data.getValue()));

        //比较获取改变的数据
        if ("0".equals(olderData.get("is_marketable")) && "1".equals(newerData.get("is_marketable"))) {
            //获取到上架商品，发送消息到Rabbit
            rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_UP_EXCHANGE, "", newerData.get("id"));
        }

        if ("1".equals(olderData.get("is_marketable")) && "0".equals(newerData.get("is_marketable"))) {
            //获取到下架商品，发送消息到Rabbit
            rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_DOWN_EXCHANGE, "", newerData.get("id"));
        }
    }
}
