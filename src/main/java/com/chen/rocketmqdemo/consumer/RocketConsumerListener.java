package com.chen.rocketmqdemo.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.chen.rocketmqdemo.config.BaseMessage;
import com.chen.rocketmqdemo.producer.DefaultProducerProxy;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
public abstract class RocketConsumerListener<T extends BaseMessage> implements RocketListener<T> {
    /**
     * 默认重试次数
     */
    private static final int MAX_RETRY_TIMES = 3;

    /**
     * 重试前缀
     */
    public static final String RETRY_PREFIX = "retry_";

    /**
     * 延时等级
     */
    private static final int DELAY_TIME = 3000;


    @Resource(name = "defaultProducer")
    private DefaultProducerProxy producer;


    @Override
    public Action consume(Message message, T messageBody, ConsumeContext consumeContext) {
        log.info("mq接收消费消息: {}", JSON.toJSONString(message));
        try {
            dispatchMessage(messageBody);
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
        return Action.CommitMessage;
    }

    /**
     * 消息处理
     *
     * @param message 待处理消息
     * @throws Exception 消费异常
     */
    protected abstract void handleMessage(T message) throws Exception;


    /**
     * 是否需要根据业务规则过滤消息，去重逻辑可以在此处处理
     * @param message 待处理消息
     * @return true: 本次消息被过滤，false：不过滤
     */
    protected boolean filter(T message) {
        return false;
    }

    /**
     * 是否异常时重复发送
     *
     * @return true: 消息重试，false：不重试
     */
    protected boolean isRetry() {
        return false;
    }

    /**
     * 超过重试次数消息，需要启用isRetry
     *
     * @param message 待处理消息
     */
    protected void handleMaxRetriesExceeded(T message) {
        log.error("消费超过重试次数消息,消息内容:{}", message);
    }

    /**
     * 消费异常时是否抛出异常
     * 返回true，则由rocketmq机制自动重试
     * false：消费异常(如果没有开启重试则消息会被自动ack)
     */
    protected boolean throwException() {
        // 是否抛出异常，false搭配retry自行处理异常
        return false;
    }

    /**
     * 最大重试次数
     *
     * @return 最大重试次数，默认3次
     */
    protected int getMaxRetryTimes() {
        return MAX_RETRY_TIMES;
    }

    /**
     * isRetry开启时，重新入队延迟时间(毫秒)
     */
    protected long getDelayTime() {
        return DELAY_TIME;
    }

    /**
     * 使用模板模式构建消息消费框架，可自由扩展或删减
     */
    public void dispatchMessage(T message) {
        log.info("消费者收到消息[{}]", JSONObject.toJSON(message));

        if (filter(message)) {
            log.info("消息id{}不满足消费条件，已过滤。",message.getKey());
            return;
        }
        // 超过最大重试次数时调用子类方法处理
        if (message.getRetryTimes() > getMaxRetryTimes()) {
            handleMaxRetriesExceeded(message);
            return;
        }
        try {
            long now = System.currentTimeMillis();
            handleMessage(message);
            long costTime = System.currentTimeMillis() - now;
            log.info("消息{}消费成功，耗时[{}ms]", message.getKey(),costTime);
        } catch (Exception e) {
            log.error("消息{}消费异常", message.getKey(),e);
            // 是捕获异常还是抛出，由子类决定
            if (throwException()) {
                //抛出异常，由DefaultMessageListenerConcurrently类处理
                throw new RuntimeException(e);
            }
            //此时如果不开启重试机制，则默认ACK了
            if (isRetry()) {
                handleRetry(message);
            }
        }
    }

    protected void handleRetry(T message) {
        ConsumerListener annotation = this.getClass().getAnnotation(ConsumerListener.class);
        if (annotation == null) {
            return;
        }
        //重新构建消息体
        String messageSource = message.getSource();
        if(!messageSource.startsWith(RETRY_PREFIX)){
            message.setSource(RETRY_PREFIX + messageSource);
        }
        message.setRetryTimes(message.getRetryTimes() + 1);

        SendResult sendResult;

        try {
            // 如果消息发送不成功，则再次重新发送，如果发送异常则抛出由MQ再次处理(异常时不走延迟消息)
            sendResult = producer.send(annotation.topic(), String.join("||", annotation.tags()), message, getDelayTime());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (sendResult == null) {
            throw new RuntimeException("重试消息发送失败");
        }

    }
}