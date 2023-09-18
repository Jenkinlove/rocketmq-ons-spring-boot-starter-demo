package com.chen.rocketmqdemo.producer;

@ProducerConfig(group = "${rocketmq.producer.group}", topic = "${rocketmq.producer.topic}", value = "defaultProducer")
public class DefaultProducer extends DefaultProducerProxy {

}
