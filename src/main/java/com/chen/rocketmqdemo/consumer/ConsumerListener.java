package com.chen.rocketmqdemo.consumer;

import com.aliyun.openservices.ons.api.PropertyValueConst;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 需要注册到rocket 的消费者标记
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsumerListener {

    /**
     * 绑定的消费组
     */
    String group();

    /**
     * 绑定的主题
     */
    String topic();

    /**
     * 是否开启消费者
     * 因为要支持读取配置文件,所以使用字符串类型
     */
    String enable() default "on";

    /**
     * consumer 注册的 接受消息的过滤tag
     */
    String[] tags() default {"*"};

    /**
     * consumer 启动的实例数
     */
    int consumers() default 1;

    /**
     * 容器名
     */
    String value() default "";

    /**
     * 消费模式
     */
    String pattern() default PropertyValueConst.CLUSTERING;

}
