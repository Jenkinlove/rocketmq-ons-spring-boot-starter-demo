package com.chen.rocketmqdemo.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 阿里云rocketMQ ons消息队列服务
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RocketAutoConfiguration.class})
public @interface EnableRocketONS {
}