package com.wangc.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @author
 * @Description: 使用tag过滤监听指定的消息
 * selectorExpression : 指定的tag要和发送时给的一致
 * @date 2022/10/17 20:40
 */
@Component
@RocketMQMessageListener(consumerGroup = "tagFilterGroupBoot", topic = "tagFilterBoot", selectorExpression = "TagA || TagC")
@Slf4j
public class TagFilterTopicListener implements RocketMQListener<MessageExt> {
    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("收到的消息:{}", new String(messageExt.getBody(), Charset.defaultCharset()));
    }
}
