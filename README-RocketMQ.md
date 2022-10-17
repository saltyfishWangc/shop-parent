### 常见的消息中间件
|   | Kafka | RocketMQ | RabbitMQ |
| --- | ----- | -------- | -------- |
|定位|日志消息、监控数据|非日志的可靠消息传输|非日志的可靠消息传输|
|可用性|非常高<br>分布式、主从|非常高<br>分布式、主从|高<br>主从、采用镜像模式实现，数据量大时可能有性能问题|
|消息可靠性|异步刷盘、容易丢数据|同步刷盘、异步刷盘|同步刷盘|
|单机吞吐量|百万级|十万级|万级|
|堆积能力|非常好|非常好|一般|
|顺序消费|支持<br>一台broker宕机后，消息会乱序|支持<br>顺序消息场景下，消费失败时消费队列将会暂停|支持<br>如果一个消费失败，此消息的顺序会被打乱|
|定时消息|不支持|支持|支持|
|事务消息|不支持|支持|不支持|
|消息重试|不支持|支持|支持|
|死信队列|不支持|支持|支持|
|访问权限|无|无|类似数据库、配置用户名和密码|

针对上面的比较，说下几点：
1. 为什么Kafaka在高并发下容易丢数据、不支持事务消息、不支持死信队列？
因为Kafka最开始出来的定位是为了处理大数据中的日志消息的，所以并发量是非常大的，但是对于日志，丢了一条两条是不会影响分析的。
同理，它都允许有消息丢失了，自然也就没要求支持事务、死信这些了，丢了就丢了。

2. 为什么RocketMQ的单机吞吐量只是十万级，不如Kafka的百万级？
因为单机吞吐量和高并发这两个指标是相对的，你要求了高并发，就很难去追求单机吞吐量了。

3. RabbitMQ常用于哪些场景？
RabbitMQ一般用于银行内部系统或者金融系统，因为这些都是内部系统直接交互，对于单机吞吐量要求没有特别高。

4. 为什么选择RocketMQ？
首先RocketMQ是阿里基于Kafka的设计理念上用java语言设计出来的，同时也采用了别的比较好的理念。RocketMQ是阿里内部使用非常广泛的消息中间件，
是经过双11洗礼的，所以它足够强大、稳定。

5. 为什么没有ActiveMQ?
ActiveMQ太老了

### RocketMQ快速入门
参考文档：https://blog.csdn.net/GBS20200720/article/details/121211392

#### RocketMQ消息发送的三种方式
* 同步：客户端发送消息给消息中间件，中间件将消息保存到磁盘后返回成功标识，客户端收到成功返回，继续往下走。调用的是send(Message)
* 异步：客户端发送消息给消息中间件，中间件收到这个消息之后，直接给应用程序响应了(此时消息并没有完全存储到磁盘)，消息中间件继续存储消息，
存储完成(成功或者失败)通过回调地址通知应用程序，消息存储的结果。这样，客户端在调用send方法时，还需要指定回调方法。send(Message, CallBack)
* 一次性消息：应用程序给消息中间件发送消息的时候，不需要知道消息是否在消息中间件存储了，只管发。这种场景一般应用于存储日志。只管发，要是没成功也不会有什么影响。sendOneway(Message)

#### RocketMQ消息消费的两种模式
对应MessageModel枚举类

* 集群模式(消费者默认的消费模式)：对于一个topic，消息中间件会有多个发送消费队列，应用程序(客户端)在发送的时候会做负载均衡，发送到其中的一个队列。这样做主要是为了解决高并发
* 广播模式

#### 顺序消息
按照集群模式，消息是在生产者这边按照顺序发送给RocketMQ的，但是由于一个topic下有多个队列，所以消息会被发送到不同的队列中，在消费者端不同的队列是不同的线程在消费，所以消息被消费的
顺序不一定是按照生产者发送的顺序来的。
RocketMQ也支持消息顺序消费的模式：大致思路就是在发送端，将需要顺序消费的消息都发到一个topic下的同一个队列中，那么消费端自然就有序了。
怎么做到将消息发送到同一个队列呢？
制定某个业务编号，用该业务编号对队列数取余，这样久保证了一个业务编号的对应消息都被发送到同一个队列。
RocketMQ中可以自定义MessageSelector来完成，重写select方法。然后在发送时指定用该选择器。
示例代码：
比如一个订单有3步：创建、付款、完成
```
// 生产者端
public static void main(String... args) {
    DefaultMQProducer producer = new DefaultMQProducer("orderlyProducerGroup");
    producer.setNameServAddr("192.168.19.129:9876");
    producer.start();
    String topic = "orderTopic";
    List<OrderStep> orderSteps = OrderUtil.buildOrders();
    MessageSelector selector = new MessageSelector() {
        @Override
        public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
            Long oderId = (Long) o;
            int index = (int) (orderId % list.size());
            return list.get(index);
        }
    };
    // 设置队列选择器
    for (OrderStep step : orderSteps) {
        Message msg = new Message(topic, step.toString().getBytes(Charset.defaultCharset()));
        // 指定消息选择器
        producer.send(msg, selector, "msg");
    }
    producer.shutdown();
}

// 消费者端
public static void main(String... args) {
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("orderlyConsumerGroup");
    consumer.setNamesrvAddr("192.168.19.129:9876");
    consumer.subscribe("orderTopic");
//    consumer.setMessageListener(new MessageListenerConcurrently() {
        //    @Override
        //    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
            //    for (MessageExt msg : list) {
                //    System.out.println("线程:" + Thread.currentThread() + ",消息的内容:" + new String(msg.getBody(), Charset.defaultCharset()));
            //    }
            //    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        //    }
    //    });
    consumer.setMessageListener(new MessageListenerOrderly() {
        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
            System.out.println("当前线程:" + Thread.currentThread() + ",队列ID:" + msg.getQueueId() + "消息内容:" + new String(msg.getBody(), Charset.defaultCharset()));
            return ConsumeOrderlyStatus.SUCCESS;
        }
    });
    consumer.start();
}
```

#### 延时消息
注意：RocketMQ并不支持任意时间的延时，需要设置几个固定的延时级别，从1s到2h分别对应着等级1到18
"1s 5s 10s 20s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"
这些是默认的，但是是可以改的。我们在控制台的集群菜单下选中一个分片点击配置进去查看messageDelayLevel就可以看到这些级别。
在conf/broker.conf配置文件里面是可以改的

这个延时时间指的是在消息队列中可以待多久，并不包括客户端发送给中间和中间件发给消费端的时间
案例代码：
消费端不用特殊设置，只需要在生产端设置
```
// 生产者端
public static void main(String... args) {
    DefaultMQProducer producer = new DefaultMQProducer("orderlyProducerGroup");
    producer.setNameServAddr("192.168.19.129:9876");
    producer.start();
    String topic = "orderTopic";
    Message msg = new Message(topic, "延时消息,发送时间:" + new Date()).getBytes(Charset.defaultCharset());
    // 设置消息延时级别
    msg.setDelayTimeLevel(3); // 10s
    producer.send(msg);
    producer.shutdown();
}
// 消费者收到这条消息，至少是10s后
```

#### 消息过滤
##### tag
生产者发送消息给中间件时，可以给topic指定tag，同时，消费者在监听时可以指定监听topic下的哪些tag，这样就只会收到对应tag的消息。

##### sql
生产者发送消息给中间件时，可以给message设置userProperties，这样，消费者在监听时可以通过sql条件来监听对应条件的消息。
默认，sql过滤这个功能是关闭的，对应的是enablePropertyFilter属性。在conf/broker.conf中设置enablePropertyFilter = true


##### 不同消费组对相同主体消息消费
Q:我们在创建消息监听时都是指定了consumerGroup的。那么它的作用是什么？
A:在RocketMQ中，消息在队列中存在它是不会去关心这个消息是否被消费掉，而是由每个消费组自己维护了已经消费的消息在队列中的位置，这样，下次他就会从这个位置开始消费。
所以当我们应用是集群部署时，我们不同的机器上存在监听相同主体的消费者，他们不会重复消费。但是如果该消息是广播模式的话，那就是一条消息会发送到每个机器上。
按照上面所说，就算是在并发模式下，如果不同的消费者组监听同一个主体，那么该主体中的每条消息都是会被所有监听的消费者组消费的。因为消息的消费对于消费者组来说只是逻
辑意义的消费。每个消费者组对于该Topic消息都有一个消费记录的文件。并没有说哪个消息一定只能是由哪个消费者组来消费。这个得由开发自己确定。

### 集成Spring Boot
1. 引入依赖
```
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.0.4</version>
</dependency>
```

2. 添加配置
