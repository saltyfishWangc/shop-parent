package com.wangc.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @author
 * @Description: 定义RocketMQ监听器
 * consumerGroup ：指定消费者所属的组，这个指定一个不重复的值即可
 * topic ：指定监听哪个topic
 * consumeMode : 指定消费模式，默认是ConsumeMode.CONCURRENTLY并发模式，要用顺序消息，配置成consumeMode = ConsumeMode.ORDERLY即可
 * messageModel : 指定消息模式，默认是MessageModel.CLUSTERING集群模式。要用广播模式，配置成messageModel = ConsumeMode.BROADCASTING即可
 * @date 2022/10/17 19:37
 */
@Component
@RocketMQMessageListener(consumerGroup = "htbConsumerGroup", topic = "helloTopicBoot")
@Slf4j
public class HelloTopicListener implements RocketMQListener<MessageExt> {
    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("收到的消息:{}", new String(messageExt.getBody(), Charset.defaultCharset()));
    }
}
