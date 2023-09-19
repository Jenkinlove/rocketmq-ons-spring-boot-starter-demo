package com.chen.rocketmqdemo.config;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.chen.rocketmqdemo.consumer.ConsumerListener;
import com.chen.rocketmqdemo.consumer.RocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * rocket 的消费者自动注册
 * 使用容器工厂扫描所有consumer, 并根据注解配置属性 subscribe到相应的队列
 */
public class ConsumerAutoRegister implements ApplicationListener<WebServerInitializedEvent> {

  @Autowired
  private RocketProperties configuration;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private PropertyResolver propertyResolver;

  protected Logger logger = LoggerFactory.getLogger(this.getClass());


  /**
   * 用来注册consumer的
   */
  public void consumerListenerRegister() {
    AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
    String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(ConsumerListener.class);


    List<RocketListener<?>> rocketListeners = new ArrayList<>();

    Arrays.stream(beanNamesForAnnotation)
            .map(x -> (RocketListener<?>) autowireCapableBeanFactory.getBean(x))
            .forEach(x -> {
              ConsumerListener consumerListener = x.getClass().getSuperclass().getAnnotation(ConsumerListener.class);
              RocketProperties.RocketConfig rocketConfig = configuration.getConfigs().get(RocketProperties.RocketKey.ERP_KEY);
              if ("on".equalsIgnoreCase(consumerListener.enable()) && rocketConfig.isEnable()) {
                for (int i = 0; i < consumerListener.consumers(); i++) {
                  rocketListeners.add(x);
                }
              }
            });


    // 注册消费者, 并执行订阅
    listenerRegister(rocketListeners.toArray(new RocketListener[0]));

  }

  /**
   * 注册consumerListener
   */
  private void listenerRegister(RocketListener<?>... listener) {
    Arrays.stream(listener).forEach(x -> {
      // 是否开启注册
      RocketProperties.RocketConfig rocketConfig = configuration.getConfigs().get(RocketProperties.RocketKey.ERP_KEY);
      Properties properties = rocketConfig.rocketProperties();
      // 获取注册注解
      ConsumerListener consumerListener = x.getClass().getSuperclass().getAnnotation(ConsumerListener.class);

      properties.put(PropertyKeyConst.GROUP_ID, rocketConfig.getGroupSuffix() + propertyResolver.resolvePlaceHolders(consumerListener.group()));
      properties.put(PropertyKeyConst.MessageModel, propertyResolver.resolvePlaceHolders(consumerListener.pattern()));

      Consumer consumer = ONSFactory.createConsumer(properties);
      // 注册消费者监听器
      consumer.subscribe(propertyResolver.resolvePlaceHolders(consumerListener.topic()), String.join("||", consumerListener.tags()), x);
      // 启动消费者
      logger.info("启动消费者: {}", properties.get(PropertyKeyConst.GROUP_ID));
      consumer.start();

    });

  }

  @Override
  public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
    consumerListenerRegister();
  }
}
