### Sleuth简介
Sleuth组件的作用是实现微服务中的链路追踪。
通常一笔业务在微服务架构中要经过很多个微服务，那么在每个微服务中的日志都是存在于自己的日志中。
除非人为的干预在接口添加请求标识，才能将整个日志串起来。而Sleuth就是在做这个事，只需要在各个
微服务引入Sleuth依赖即可。

### 使用
1. 引入依赖
```
<!-- Sleuth链路追踪组件 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

2. 格式日志
* 第一个值：spring.application.name的值
* 第二个值：sleuth生成的一个ID，叫Trace ID，用来标识一条请求链路，一条请求链路中包含一个Trace ID，多个Span ID
* 第三个值：span ID 基本的工作单元，获取元数据，如发送一个Http
* 第四个值：true，是否要将该信息输出到zipkin服务中来收集和展示。

### Zipkin + Sleuth整合

#### Zipkin简介
Zipkin是Twitter基于google的分布式监控系统Dapper(论文)的开发源实现，zipkin用于跟踪分布式之间的应用数据链路，分析
处理延时，帮助改进系统的性能和定位故障。

#### 使用
1. 下载
去官网https://zipkin.io下载Zipkin的jar包

2. 运行
通过java -jar运行jar包
```
java -jar zipkin-server-2.22.1-exec.jar
```
默认端口是9411

3. 浏览器访问
```
http://localhost:9411
```

4. 微服务接入
在微服务中添加zipkin依赖。
注意：把原来的Sleuth依赖去掉，只需要添加zipkin依赖即可，因为zipkin里面是依赖了Sleuth的
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</denpendency>
```

微服务中添加zipkin配置
```
spring:
    zipkin:
        # zipkin server的请求地址
        base-url: http://127.0.0.1:9411/
        # 让nacos把它当成一个URL，而不是当成微服务名
        discoveryClientEnabled: false
    sleuth:
        sampler:
            # 采样的百分比
            probability: 1.0
```