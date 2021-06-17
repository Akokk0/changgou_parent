package com.changgou.search.listener;

import com.changgou.search.config.RabbitMQConfig;
import com.changgou.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsUpListener {

    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMQConfig.GOODS_UP_QUEUE)
    public void goodsUp(String spuId) {
        System.out.println("接收到消息为：" + spuId);
        //调用Service方法
        esManagerService.importDataBySpuId(spuId);
    }
}
