package com.chen.rocketmqdemo.producer;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 消费者和生产者的 topic相关配置,支持${propertyKey} 读取配置文件属性
 */
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProducerConfig {

  /**
   * 绑定的生产组
   */
  String group();

  /**
   * 绑定的主题
   */
  String topic();

  /**
   * 是否开启生产者
   * 因为要支持读取配置文件,所以使用字符串类型
   */
  String enable() default "on";

  /**
   * 容器名
   */
  String value() default "";

}
