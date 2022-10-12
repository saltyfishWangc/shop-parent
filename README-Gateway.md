见api-gateway模块

### Gateway简介
Spring Cloud Gateway是Spring公司基于Spring 5.0、Spring Boot 2.0、Project Reactor等技术开发的网关，
它旨在为微服务架构提供一种简单有效的统一的API路由管理方式，它的目标是替代Netflix Zuul，其不仅提供统一
的路由方式，并且基于Filter链的方式提供了网关基本的功能，例如：安全、监控和限流。
优点：
* 性能强劲：是第一代网关Zuul的1.6倍
* 功能强大：内置了很多实用的功能，例如转发、监控、限流等
* 设计优雅：容易扩展

缺点：
* 其实现依赖Netty和WebFlux，不是传统的Servlet编程模型，学习成本高
* 不能将其部署在Tomcat、Jetty等Servlet容器，只能打成Jar包执行
* 需要Spring Boot 2.0及以上版本支持

### 使用
1. 引入依赖
```
<!-- spring cloud gateway 网关-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!-- nacos客户端 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>    
```

2. 配置
application.yml中添加初始化配置
```
server:
  port: 9000
spring:
  application:
    name: api-gateway
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.19.129:8848
    gateway:
      discovery:
        locator:
          # 让gateway可以发现nacos中的微服务
          enabled: true
```
现在启动服务类，网关服务就启起来了。
有个问题：不是说网关的作用是用来做路由的吗，这里都没做路由，怎么经过网关请求到各个微服务？
回答：Spring Cloud Gateway默认的路由规则就是根据服务名路由到对应的服务，例如我们通过
http://127.0.0.1:9000/order-service/auth?type=PC，这里我们请求的是网关的端口，并且指定
的是订单服务的服务名，那么网关就会将这个请求转发至订单服务。