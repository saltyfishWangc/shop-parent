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

3. 自定义路由规则
```
spring:
  cloud:
    gateway:
      routes:
        # 路由名称，要求唯一
        - id: product_route
          # 将符合条件的请求转发到哪个微服务，lb表示对服务进行负载均衡，是loadbalance的缩写
          uri: lb://product-service
          # 断言，路由条件
          predicates:
            - Path=/product-serv/**
          filters:
            # 在转发请求之前，将拦截到的路径的第一层路径删除掉
            - StripPrefix=1
        - id: order_route
          uri: lb://order-service
          predicates:
            - Path=/order-serv/**
          filters:
            - StripPrefix=1
```

3.1 过滤器Filter
过滤器就是在请求的传递过程中，对请求和响应做一些手脚。对应于配置中的filters集合。
在Gateway中，Filter的作用范围有两种：
* GatewayFilter：局部过滤器，应用到单个路由或者一个分组的路由上。
* GlobalFilter：应用到所有的路由上。

3.1.1 自定义局部过滤器
编写Filter类，注意名称是有固定格式的：xxxGatewayFilterFactory
代码实现见com.wangc.filter.TimeGatewayFilterFactory
所谓局部，就是需要在配置文件中配置上这个过滤器

3.1.2 自定义全局过滤器
编写Filter类，实现org.springframework.cloud.gateway.filter.GlobalFilter.filter()方法，这个全局过滤器类存在即生效，不需要在配置中进行指定配置。
代码实现见：com.wangc.filter.AuthGlobalFilter

#### Spring Cloud Gateway集成Sentinel实现流控
1. 添加依赖
```
<!-- Sentinel组件 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- Sentinel对Spring Cloud Gateway的适配器-->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>
<!-- sentinel集成gateway-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```
2. 添加配置
```
spring:
    cloud:
        sentinel:
            transport:
                # 跟Sentinel控制台交互的端口，随意指定一个未被占用的端口即可
                port: 8299
                # Sentinel控制台服务的地址
                dashboard: 192.168.19.128:8099
```

3. 修改Sentinel限流默认的返回格式
代码实现见：
com.wangc.config.GatewayConfiguration