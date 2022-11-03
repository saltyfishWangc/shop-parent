### 分布式事务解决方案
#### 全局事务
全局事务管理：
优点：
    比较简单
缺点：
    1. 事务管理器单点故障
    2. 持有锁时间比较长(两阶段提交，第一阶段预留资源，第二阶段提交才释放锁)
    3. 没有重试机制保证commit一定成功
    
#### 可靠消息服务
RocketMQ可靠消息实现数据最终一致性：体现在最终一致性
* 业务方发送事务消息(半消息)，对消费者不可见
* 执行本地事务，给MQ返回执行结果
* 根据执行结果，如果是commit，把半消息变成完整消息，对消费者可见，否则删除消息
* 提供回查机制，如果半消息长时间没有处理，消息中心调用业务方回查方法，判断本地事务的执行情况(也就是业务在本地方法的逻辑是否执行完成，可能已经完成，在把结果发给消息中心的时候网络问题了)，
如果执行了，反馈MQ把半消息变成完整消息，消费者可见去消费。否则删除消息。
#### TCC事务

#### Seata分布式事务解决方案
1. 引入依赖
```
<!-- 引入seata -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <!-- 我们的spring cloud版本是2.2.2.RELEASE，里面包含的io.seata.seata-spring-boot-starter版本不兼容，先排除掉，下面单独引入对应兼容的版本-->
    <exclusions>
        <exclusion>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactId>
    <version>1.3.0</version>
</dependency>
```

2. 配置文件添加配置
```
seata:
    tx-service-group: seckill-service-group
    registry:
        type: nacos
        nacos:
            server-addr: ${spring.cloud.nacos.config.server-addr}
            group: SEATA_GROUP
            application: seata-server
    service:
        vgroup-mapping:
            seckill-service-group: default
```

3. 代码修改
只需要在事务发起的服务的对应方法上加上@GlobalTransactional即可，不需要@Transactional。被远程调用的那些方法都不用加@GlobalTransactional，加上@Transactional保持原样即可。

Seata-AT模式(属于两阶段提交)
发起方向TC注册全局事务和分支事务
发起方调用其他服务的时候，其他服务也需要向TC注册分支事务。
所有服务都调用了成功，发起方调用没有问题，通知TC，TC通知所有的分支事务进行commit操作。
其中有服务出现异常，发起方识别到异常，抛出异常，通知TC，通知所有的分支事务进行rollback操作。
每个服务中都需要有undolog日志，和业务表处于同一事务，保证原子性。
当进行commit操作的时候，本地只是把undolog记录删除。
当进行rollback操作的时候，本地基于undolog进行数据库的回滚并删除记录。
当commit和rollback都需要通知TC执行结果，结果TC没有收到执行结果，定期给服务发送请求，直到执行成功为止。

Seata-TCC模式，也属于两阶段提交
TRY阶段：预留资源
COMMIT阶段：执行预留资源
CONCEL阶段：释放预留资源
-空回滚：try方法没有执行，执行cancel。解决方案：使用日志表，Try方法执行插入记录，Cancel需要判断是否有记录，如果没有就属于空回滚，不做事情。
-幂等：try和commit有可能会被多次调用，要保证接口的幂等性，通过日志表状态机方式解决。
-防悬挂：Cancel比try更早执行，判断是否有try的日志，如果没有，cancel插入日志，状态为cancel，try方法来到之后，插入记录，但是会报主键异常，不做事情。

