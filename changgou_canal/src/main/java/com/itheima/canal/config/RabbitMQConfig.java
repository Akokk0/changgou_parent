package com.itheima.canal.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    //队列名称
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";
    public static final String GOODS_UP_QUEUE = "goods_up_queue";
    public static final String GOODS_DOWN_QUEUE = "goods_down_queue";

    //交换机名称
    public static final String GOODS_UP_EXCHANGE = "goods_up_exchange";
    public static final String GOODS_DOWN_EXCHANGE = "goods_down_exchange";

    //声明队列
    @Bean(AD_UPDATE_QUEUE)
    public Queue AD_UPDATE_QUEUE() {
        return new Queue(AD_UPDATE_QUEUE);
    }

    @Bean(GOODS_UP_QUEUE)
    public Queue GOODS_UP_QUEUE() {
        return new Queue(GOODS_UP_QUEUE);
    }

    @Bean(GOODS_DOWN_QUEUE)
    public Queue GOODS_DOWN_QUEUE() {
        return new Queue(GOODS_DOWN_QUEUE);
    }

    //声明交换机
    @Bean(GOODS_UP_EXCHANGE)
    public Exchange GOODS_UP_EXCHANGE() {
        return ExchangeBuilder.fanoutExchange(GOODS_UP_EXCHANGE).durable(true).build();
    }

    @Bean(GOODS_DOWN_EXCHANGE)
    public Exchange GOODS_DOWN_EXCHANGE() {
        return ExchangeBuilder.fanoutExchange(GOODS_DOWN_EXCHANGE).durable(true).build();
    }

    //绑定队列交换机
    @Bean
    public Binding AD_UPDATE_BINDING(@Qualifier(GOODS_UP_QUEUE) Queue queue, @Qualifier(GOODS_UP_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    @Bean
    public Binding GOODS_DOWN_BINDING(@Qualifier(GOODS_DOWN_QUEUE) Queue queue, @Qualifier(GOODS_DOWN_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

}
