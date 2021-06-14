package com.itheima.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.itheima.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author ZJ
 */
@CanalEventListener
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_business", table = "tb_ad")
    public void adUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.err.println("广告数据发生变化");

        /*rowData.getBeforeColumnsList().forEach((data) -> {
            System.out.println("之前的数据:" + data.getName() + ":" + data.getValue());
        });

        rowData.getAfterColumnsList().forEach((data) -> {
            System.out.println("修改后的数据:" + data.getName() + ":" + data.getValue());
        });*/

        //获取数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            //如果为position字段则发送消息
            if ("position".equals(column.getName())) {
                //打印消息到控制台
                System.out.println("RabbitMQ即将发送消息：" + column.getValue());
                //发送消息
                rabbitTemplate.convertAndSend("", RabbitMQConfig.AD_UPDATE_QUEUE, column.getValue());
            }
        }
    }
}
