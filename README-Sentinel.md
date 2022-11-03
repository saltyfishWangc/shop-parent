#### 服务熔断降级Sentinel
##### 高并发带来的问题
###### 服务器雪崩效应
在分布式系统中，由于网络原因或自身的原因，服务一般无法保证100%可用。如果一个服务出现了问题，调用这个服务就会
出现线程阻塞的情况，此时若有大量的请求涌入，就会出现多条线程阻塞等待，服务器资源是有限的，当资源全部被占用时，
进而导致服务瘫痪。
由于服务与服务之间的依赖性，故障会传播，会对整个微服务系统造成灾难性的严重后果，这就是服务故障的雪崩效应。

###### 常见容错方案
要防止雪崩的扩散，就要做好服务的容错，容错说白了就是保护自己不被猪队友拖垮的一些措施。下面是介绍常见的服务容错思路和组件
####### 常见的容错思路
常见的容错思路有隔离、超时、限流、熔断、降级这几种，下面分别介绍。
* 隔离机制：比如服务A内总共有100个线程，现在服务A可能会调用服务B、服务C、服务D。我们在服务A进行远程调用的时候给不同的服务分配固定的线程，
不会把所有线程都分配给某个微服务，比如调用服务B分配30个线程，调用服务C分配30个线程，调用服务D分配40个线程，这样进行资源的隔离，保证即使
下游某个服务挂了，也不至于把服务A的线程消耗完，比如服务B挂了，这时候最多只会占用服务A的30个线程，服务还有70个线程可以调用服务C和服务D。
* 超时机制：在上游服务调用下游服务的时候，设置一个最大响应时间，如果超过这个时间，下游未做出反应，就断开请求，释放掉线程。
* 限流机制：限流就是限制系统的输入和输出流量已达到保护系统的目的。为了保证系统的稳定运行，一旦达到需要限制的阈值，就需要限制流量并采取减少
措施以完成限制流量的目的。(限流机制是设置在下游服务的入口)
* 熔断机制：在互联网系统中，当下游服务因访问压力过大而响应变慢或失败，上游服务为了保护系统整体的可用性，可以暂时切断对下游服务的调用。这种
牺牲局部，保全整体的措施就叫做熔断。
服务熔断一般有三种状态：
* 熔断关闭状态(Closed)
服务没有故障时，熔断器所处的状态，对调用方的调用不做任何限制
* 熔断开启状态(Open)
后续对该服务接口的调用不再经过网络，直接执行本地的fallback方法
* 半熔断状态(Half-Open)
尝试恢复服务调用，允许有限的流量调用该服务，并监控调用成功率。如果成功率达到预期，则

####### 常见的容错组件
* Hystrix
Hystrix是由Netflix开源的一个延迟和容错库，用于隔离访问远程系统、服务或者第三方库，防止级联失败，从而提升系统的可用性与容错性。
* Resilience4J
Resilience4J是一款非常轻量、简单并且文档非常清晰、丰富的熔断工具，这也是Hystrix官方推荐的替代产品，不仅如此，Resilience4J还
原生支持Spring Boot 1.x/2.x，而且监控也支持prometheus等多款主流产品进行整合。
* Sentinel
Sentinel是阿里巴巴开源的一款断路器实现，本身在案例内部已经被大规模采用，非常稳定。

####### Sentinel
Sentinel(分布式系统的流量防卫兵)是阿里开源的一套用于服务容错的综合性解决方案。它以流量为切入点，从流量控制、熔断降级、系统负载保护
等多个维度来保护服务的稳定性。

Sentinel具有以下特征：
* 丰富的应用场景：Sentinel承接了阿里巴巴近10年的双十一大促流量的核心场景，例如秒杀(及突发流量控制在系统容量可以承受的范围)，
消息削峰填谷、集群流量控制、实时熔断下游不可用应用等。
* 完备的实时监控：Sentinel提供了实时的监控功能。通过控制台可以看到接入医用的单台机器秒级数据，甚至500台以下规模的集群的汇总运行情况。
* 广泛的开原生态：Sentinel提供开箱即用的与其他开源框架/库的整合模块，例如与Spring Cloud、Dubbo、grpc的整合，只需要引入相应的依赖并
进行简单的配置即可快速地接入Sentinel。

Sentinel分为两个部分：
* 核心库(Java客户端)不依赖任何框架/库，能够运行与所有Java运行时环境，同时对Dubbo/Spring Cloud等框架也有较好的支持。
* 控制台(Dashboard)基于Spring Boot开发，打包后可以直接运行，不需要额外的tomcat等应用容器。

####### 使用
1. 添加依赖
在订单服务添加
```
<!-- Sentinel组件 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

2. 安装Sentinel控制台
下载jar包：https://github.com/alibaba/Sentinel/releases/download/1.8.5/sentinel-dashboard-1.8.5.jar
启动控制台
```
java -Dserver.port=8099 -Dcsp.sentinel.dashboard.server=localhost:8099 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.5.jar
```
浏览器访问地址：192.168.19.128:8099
用户/密码：sentinel/sentinel

3. 在需要被Sentinel管理的项目中的配置文件application.yml添加如下配置：
```
spring:
    cloud:
        sentinel:
            transport:
                # 跟Sentinel控制台交互的端口，随意指定一个未被占用的端口即可
                port: 8999
                # Sentinel控制台服务的地址
                dashboard: 192.168.19.128:8099
```

4. Sentinel规则<br><br>
<b>流控规则</b>：流控效果、关联流控、链路流控
链路流控需要代码配合：
添加配置：
```
spring:
    cloud:
        sentinel:
            # 配合链路流控的配置。表示把@SentinelResource链路的上一级地址也展示出来
            web-context-unify: false
// java代码部分见：com.wangc.controller.TraceController            
```
理解：链路流控只要是针对service层的接口做的流控，比如service方法被多个controller入口调用，这样可以针对某一个入口(也就是链路六空中的入口资源)做流控

<b>降级规则</b>：慢比例调用、异常比例、异常数

<b>热点流控</b>：是针对某个接口中的某个参数值做限流，比如都是商品查询接口，针对调用这个接口时传进来的商品编号做限流。
演示方法见：com.wangc.controller.HotSpotController.hotSpot

<b>授权规则</b>：授权规则在Sentinel后台配置页面上是针对某个参数的值做白名单和黑名单
例如：要对某个接口请求的终端进行权限控制，请求方是将终端类型赋值给serviceName传进来的，那么就需要写个方法来描述如何从请求中拿到值。
案例见：
com.wangc.controller.AuthController.auth

<b>系统规则</b>：这个系统规则是Sentinel自己会定期去获取当前操作系统的资源情况，我们在页面上可以针对这些资源设置阈值来给服务做流控。

5. 自定义异常返回
默认的，如果服务被Sentinel限流了，请求方收到的返回是字符串： Blocked by Sentinel (flow limiting)
很明显，这个返回不友好，请求方一脸懵。其实，Sentinel针对上面的各种流控都是返回了对应的异常的，我们可以自定义组件类来接收这些返回的异常，
处理后然后再返回给请求方。
自定义组件类实现com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler
案例见：com.wangc.controller.ExceptionHandlerPage
这样请求方可以针对返回码做对应的处理。比如针对-1返回码，请求方可以让用户输入验证码这种手段来降低对接口的请求频次。

6. 用@SentinelResource针对某个接口自定义异常返回
见：com.wangc.controller.SentinelResourceController

7. Feign整合Sentinel
背景：通常情况下，当下游服务被流控后返回的处理，上游服务是没法处理的，所以得给兜底处理

在服务调用方项目(我们这里是订单服务)的配置文件中开启feign对Sentinel的支持。只有这个配置了这个，那么在远程调用时如果被降级，上级服务才会走对应的降级方法，也就是兜底的方法
```
feign:
    sentinel:
        enabled: true
```

创建容错类：com.wangc.feign.ProductFeignFallBack
这个类是一定要实现@FeignClient接口的，然后针对里面的每个方法给一个兜底的处理，相当于本地的降级方法

在@FeignClient接口添加fallback属性