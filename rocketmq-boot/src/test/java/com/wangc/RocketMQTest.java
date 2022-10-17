package com.wangc;

import com.wangc.entity.OrderStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Description:
 * @date 2022/10/17 19:27
 */
@SpringBootTest
@Slf4j
public class RocketMQTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送同步消息
     */
    @Test
    public void sendMsg() {
        Message msg = MessageBuilder.withPayload("boot发送同步消息").build();
        rocketMQTemplate.send("helloTopicBoot", msg);
    }

    /**
     * 发送异步消息
     * @throws InterruptedException
     */
    @Test
    public void sendSYNCMsg() throws InterruptedException {
        log.info("发送前");
        Message msg = MessageBuilder.withPayload("boot发送异步消息").build();
        rocketMQTemplate.asyncSend("helloTopicBoot", msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送状态:{}", sendResult.getSendStatus());
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("消息发送失败");
            }
        });
        log.info("发送完成");
        TimeUnit.SECONDS.sleep(5);
    }

    /**
     * 发送一次性消息
     */
    @Test
    public void sendOnewayMsg() {
        Message msg = MessageBuilder.withPayload("boot发送一次性消息").build();
        rocketMQTemplate.sendOneWay("helloTopicBoot", msg);
    }

    /**
     * 发送顺序消息
     */
    @Test
    public void sendOrderlyMsg() {
        // 设置自定义队列选择器
        rocketMQTemplate.setMessageQueueSelector(new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> list, org.apache.rocketmq.common.message.Message message, Object o) {
                // 注意：这里的Object o接收的是发送给rocketmq的消息内容
                String orderIdStr = (String) o;
                Long orderId = Long.parseLong(orderIdStr);
                int index = (int) (orderId % list.size());
                return list.get(index);
            }
        });
        List<OrderStep> orderSteps = new ArrayList<>();
        orderSteps.add(new OrderStep(1l, "创建"));
        orderSteps.add(new OrderStep(1l, "付款"));
        orderSteps.add(new OrderStep(1l, "快递"));

        orderSteps.add(new OrderStep(4l, "创建"));

        orderSteps.add(new OrderStep(2l, "创建"));
        orderSteps.add(new OrderStep(2l, "付款"));
        orderSteps.add(new OrderStep(2l, "快递"));

        orderSteps.add(new OrderStep(4l, "付款"));

        orderSteps.add(new OrderStep(3l, "创建"));
        orderSteps.add(new OrderStep(3l, "付款"));
        orderSteps.add(new OrderStep(4l, "快递"));

        orderSteps.add(new OrderStep(4l, "快递"));
        orderSteps.forEach(orderStep -> {
            Message msg = MessageBuilder.withPayload(orderStep.toString()).build();
            rocketMQTemplate.sendOneWayOrderly("orderlyTopicBoot", msg, String.valueOf(orderStep.getOrderId()));
        });
    }

    /**
     * 发送延时消息
     * 所谓延时消息是指消息在中间件的内存中会存留指定的时间，时间没到，消费者是消费不了的，甚至是不知道有这个消息。延时时间到了，消费者就会见监听到，从而进行消费
     */
    @Test
    public void sendDelayMsg() {
        Message msg = MessageBuilder.withPayload("boot发送延时消息，发送时间:" + new Date()).build();
        rocketMQTemplate.syncSend("helloTopicBoot", msg, 3000, 3);
    }

    /**
     * 消息过滤：发送消息到topic下的tag
     * 为什么rocketMQTemplate没有指定的api来设置tag，而是直接在topic后面接:tag，这是因为spring的rocketMQTemplate是它
     * 自己定义的模板，是用来适配很多消息中间件的，而tag这个概念是rocketmq的，在别的中间件并不存在这个概念
     *
     * 该测试方法的对应监听器：com.wangc.listener.TagFilterTopicListener
     */
    @Test
    public void sendTagFilterMsg() {
        Message msg1 = MessageBuilder.withPayload("消息A").build();
        rocketMQTemplate.sendOneWay("tagFilterBoot:TagA", msg1);
        Message msg2 = MessageBuilder.withPayload("消息B").build();
        rocketMQTemplate.sendOneWay("tagFilterBoot:TagB", msg2);
        Message msg3 = MessageBuilder.withPayload("消息C").build();
        rocketMQTemplate.sendOneWay("tagFilterBoot:TagC", msg3);
    }

    /**
     * 消息过滤：给消息设置属性，再发出去。对应着消费者会根据sql条件来过滤消费满足条件的消息
     * 该测试方法的对应监听器：com.wangc.listener.SQLFilterTopicListener
     */
    @Test
    public void sendSQLFilterMsg() {
        Message msg1 = MessageBuilder.withPayload("美女A,年龄22,体重45")
                .setHeader("age", 22)
                .setHeader("weight", 45)
                .build();
        rocketMQTemplate.sendOneWay("SQLFilterBoot", msg1);

        Message msg2 = MessageBuilder.withPayload("美女B,年龄25,体重60")
                .setHeader("age", 25)
                .setHeader("weight", 60)
                .build();
        rocketMQTemplate.sendOneWay("SQLFilterBoot", msg2);

        Message msg3 = MessageBuilder.withPayload("美女C,年龄40,体重70")
                .setHeader("age", 40)
                .setHeader("weight", 70)
                .build();
        rocketMQTemplate.sendOneWay("SQLFilterBoot", msg3);
    }
}
