package com.chen.rocketmqdemo.producer;

import com.chen.rocketmqdemo.config.RocketProperties;

@ProducerConfig(group = "${rocketmq.producer.orderRewrite.group}",
        topic = "${rocketmq.producer.orderRewrite.topic}",
        rocketKey = RocketProperties.RocketKey.ERP_KEY,
        value = "defaultProducer")
public class DefaultProducer extends DefaultProducerProxy {

}
