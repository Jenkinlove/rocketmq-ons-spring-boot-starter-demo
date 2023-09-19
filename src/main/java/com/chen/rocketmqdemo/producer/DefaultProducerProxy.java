package com.chen.rocketmqdemo.producer;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.google.common.base.Throwables;
import com.chen.rocketmqdemo.config.BaseMessage;
import com.chen.rocketmqdemo.config.PropertyResolver;
import com.chen.rocketmqdemo.config.RocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 通用的生产者基类
 */
@Slf4j
public abstract class DefaultProducerProxy {

  @Autowired
  protected RocketProperties configuration;

  @Autowired
  private PropertyResolver propertyResolver;

  protected Producer producer;

  protected Logger logger = LoggerFactory.getLogger(this.getClass());


  /**
   * 初始化 producer
   */
  @PostConstruct
  public void setup() {
    if (producerConfig() == null || !"on".equalsIgnoreCase(producerConfig().enable())) {
      logger.warn("配置开关已关闭!");
      return;
    }
    RocketProperties.RocketConfig rocketConfig = configuration.getConfigs().get(producerConfig().rocketKey());
    Properties properties = rocketConfig.rocketProperties();
    properties.put(PropertyKeyConst.GROUP_ID, rocketConfig.getGroupSuffix() + propertyResolver.springElResolver(producerConfig().group()));

    producer = ONSFactory.createProducer(properties);
    logger.info("启动 producer :-> {}", properties.get(PropertyKeyConst.GROUP_ID));

    producer.start();
  }

  public Producer getProducer() {
    return producer;
  }


  /**
   * 发送同步消息
   */
  public <T extends BaseMessage> SendResult send(String tag, T message) {
    return send(getTopic(), tag, message);
  }

  /**
   * 发送同步消息
   */
  public <T extends BaseMessage> SendResult send(String topic, String tag, T message) {
    printLog(message, topic);
    Message msg = new Message(topic, tag, message.getKey(), toByte(message));
    return producer.send(msg);
  }

  /**
   * 发送异步消息
   */
  public <T extends BaseMessage> void sendAsync(String tag, T message) {
    sendAsync(getTopic(), tag, message);
  }

  /**
   * 发送异步消息
   */
  public <T extends BaseMessage> void sendAsync(String topic, String tag, T message) {
    printLog(message, topic);
    Message msg = new Message(topic, tag, message.getKey(), toByte(message));
    producer.sendAsync(msg, new SendCallback() {
      @Override
      public void onSuccess(SendResult sendResult) {
        log.info("[{}]异步消息[{}]发送结果[{}]", sendResult.getTopic(), sendResult.getMessageId(), toByte(sendResult));
      }

      /**
       * 发送失败回调方法.
       * @param context 失败上下文.
       */
      @Override
      public void onException(OnExceptionContext context) {
        log.error("[{}]异步消息[{}]发送异常[{}]", context.getTopic(), context.getMessageId(), Throwables.getStackTraceAsString(context.getException()));

      }
    });
  }

  /**
   * 发送延迟消息
   */
  public <T extends BaseMessage> SendResult send(String tag, T message, long delayTime) {
    return send(getTopic(), tag, message, delayTime);
  }

  /**
   * 发送延迟消息
   */
  public <T extends BaseMessage> SendResult send(String topic, String tag, T message, long delayTime) {
    printLog(message, topic);
    Message msg = new Message(topic, tag, message.getKey(), toByte(message));
    if (delayTime > 0) {
      msg.setStartDeliverTime(System.currentTimeMillis() + delayTime);
    }
    return producer.send(msg);
  }

  /**
   * 默认使用基础配置文件中的 topic
   */
  public String getTopic() {
    if (producerConfig() != null) {
      return propertyResolver.springElResolver(producerConfig().topic());
    }
    return configuration.getConfigs().get(producerConfig().rocketKey()).getTopic();
  }

  /**
   * json序列化成字节码
   */
  protected byte[] toByte(Object o) {
    return JSON.toJSONString(o).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * 输出日志
   */
  private <T> void printLog(T msg, String topic) {
    logger.info(" {} 生产者发送消息:{} ", producerConfig().group() + ":" + topic, msg);
  }

  /**
   * 获取 producer 上面注册的ons配置注解
   */
  protected ProducerConfig producerConfig() {
    return this.getClass().getAnnotation(ProducerConfig.class);
  }
}

