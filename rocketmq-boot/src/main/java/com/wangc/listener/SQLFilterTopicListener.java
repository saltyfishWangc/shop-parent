package com.wangc.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @author
 * @Description: 使用sql表达式过滤监听指定的消息
 * selectorType : 选择器类型，默认是SelectorType.TAG，要指定为selectorType = SelectorType.SQL92
 * selectorExpression : 指定sql表达式来筛选消息的属性
 * @date 2022/10/17 20:40
 */
@Component
@RocketMQMessageListener(consumerGroup = "SQLFilterBoot", topic = "SQLFilterBoot", selectorType = SelectorType.SQL92, selectorExpression = "age > 23 and weight > 60")
@Slf4j
public class SQLFilterTopicListener implements RocketMQListener<MessageExt> {
    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("收到的消息:{}", new String(messageExt.getBody(), Charset.defaultCharset()));
    }
}
