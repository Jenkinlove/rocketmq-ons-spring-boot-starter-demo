package com.chen.rocketmqdemo.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.TypeReference;
import com.aliyun.openservices.shade.com.alibaba.fastjson.parser.Feature;
import com.chen.rocketmqdemo.config.BaseMessage;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 注册consumer 的基类
 */
public interface RocketListener<T> extends MessageListener {

  @Override
  default Action consume(Message message, ConsumeContext context) {
    T messageBody = JSON.parseObject(message.getBody(), getJsonType().getType(), Feature.SupportAutoType);
    LoggerFactory.getLogger(getClass()).info("receive msg : {}", messageBody);
    return consume(message, messageBody, context);
  }

  Action consume(Message message, T messageBody, ConsumeContext consumeContext);

  default TypeReference<T> getJsonType() {
    try {
      Type[] interfaces = getClass().getGenericInterfaces();
      for (Type anInterface : interfaces) {
        if (anInterface.getTypeName().contains(RocketListener.class.getTypeName())) {
          Type[] actualTypeArguments = ((ParameterizedType) anInterface).getActualTypeArguments();

          if (actualTypeArguments != null && actualTypeArguments.length > 0) {
            return new TypeReference<T>() {
              @Override
              public Type getType() {
                return actualTypeArguments[0];
              }
            };
          }
        }
      }

    }catch (Exception e){
      e.printStackTrace();
    }

    return null;
  }

}
