package com.changgou.search.listener;

import com.changgou.search.config.RabbitMQConfig;
import com.changgou.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsDownListener {

    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMQConfig.GOODS_DOWN_QUEUE)
    public void goodsDown(String spuId) {
        System.out.println("收到到消息为：" + spuId);
        //调用Service方法修改ElasticSearch到数据
        esManagerService.delDataBySpuId(spuId);
    }
}
