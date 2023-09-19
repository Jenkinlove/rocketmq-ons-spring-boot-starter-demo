package com.chen.rocketmqdemo.consumer;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * @Component 注册成一个bean
 * @ConsumerListener 标识是一个ons的消息消费者监听器
 * 1. tags:接受带有相关tag的消息
 * 2. consumers启动的实例数量
 * <p>
 * MessageData 为消息体类型class
 * consume 为消费业务逻辑
 */
@Slf4j
@ConsumerListener(group = "${rocketmq.consumer.orderRewrite.group}",
        topic = "${rocketmq.consumer.orderRewrite.topic}",
        tags = "rewrite"
)
public class ExampleConsumerListener extends RocketConsumerListener<ExampleMessage> {
  /**
   * 消息处理
   *
   * @param message 待处理消息
   */
  @Override
  protected void handleMessage(ExampleMessage message) {
    //todo 业务代码

  }
}
