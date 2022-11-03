### 20221011
新建商品、订单服务，对应模块分别是shop-product-api、shop-product-server、shop-order-api、shop-order-server
1.订单服务通过RestTemplate调用商品服务接口，见com.wangc.service.impl.OrderServiceImpl.createOrder中调用商品服务查询商品信息
```
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

@Autowired
RestTemplate restTemplate;

Product product = restTemplate.getForObject("http://localhost:8081/product/" + productId, Product.class);
```

### 20221012
鉴于上面过程中订单服务调用商品服务接口的耦合问题，考虑引入服务注册中心

#### 常见的服务注册中心
Zookeeper
Zookeeper是一个分布式服务框架，是Apache Hadoop的一个子项目，它主要是用来解决分布式应用中经常遇到的一些数据管理问题，
如：统一名命服务、状态同步服务、分布式应用配置项的管理等。

Eureka
Eureka是Springcloud Netflix中的中套组件，主要作用就是做服务注册和发现。但是Netflix现在已经闭源了。

Consul
Consul是基于GO语言开发的开源工具，主要面向分布式，服务化的系统提供服务注册、服务发现和配置管理的功能。
Consul的功能都很实用，其中包括：服务注册/发现、健康检查、Key/Value存储、多数据中心和分布式一致性保证
等特性。Consul本身只是一个二进制的可执行文件，所以安装和部署都非常简单，只需要从官网下载后，再执行对应
的启动脚本即可。

Nacos
Naocs是一个更易于构建元原生应用的动态服务发现、配置管理和服务管理平台，它是Spring Cloud Alibaba组件之一，
负责服务注册发现和服务配置。Nacos的功能更强大。
下面这句话得慎重理解(是对的吗，就算是nacos，难道客户端都不是要用对应版本的包来接入吗)：
Nacos与服务之间是通过Restful交互的，Restful是定义的一套请求规范，所以在这个基础上，服务就不再受编程语言的约束。

#### Nacos简介
Nacos致力于帮助发现、配置和管理微服务。Nacos提供了一组简单易用的特性集，帮助快速实现动态服务发现、服务配置、服务
元数据及流量管理。
总结：Nacos的作用就是一个注册中心，用来管理注册上来的各个微服务。

中文网地址：https://nacos.io/zh-cn/docs/quick-start.html

核心功能点：
* 服务注册：Nacos Client会通过发送REST请求向Nacos Server注册自己的服务，提供自身的元数据，比如ip地址，端口等信
息。Nacos Server接收到注册请求后，就会把这些元数据存储到一个双层的内存Map中。
* 服务心跳：在服务注册后，Nacos Client会维护一个定时心跳来维持统治Nacos Server，说明服务一直处于可用状态，防止
被剔除，默认5s发送一次心跳。
* 服务同步：Nacos Server集群之间会相互同步服务实例，用来保证服务信息的一致性。
* 服务发现：服务消费者(Nacos Client)在调用服务提供的服务时，会发送一个REST请求给Nacos Server，获取上面注册的服务
清单，并且缓存在Nacos Client本地，同时Nacos Client本地开启一个定时任务拉取服务最新的注册表信息更新到本地缓存。
* 服务健康检查：Nacos Server会开启一个定时任务来检查注册服务实例的健康情况，对于超过15s没有收到客户端心跳的实例会
将它的healthy属性设置为false(客户端服务发现时不会发现)，如果某个实例超过30s没有收到心跳，直接剔除该实例(被剔除的实例
如果恢复发送心跳则会重新注册)。

##### 安装

后台管理页面地址：
http://192.168.19.128:8848/nacos 
账号/密码：nacos/nacos

##### 使用
1. 引入依赖：
```
<!-- Nacos客户端 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
2. 主类上添加@EnableDiscoveryClient注解
3. application.yml中添加Nacos服务的地址
```
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.19.128:8848
```

订单服务调用商品服务可以根据商品服务名获取到nacos中注册的商品服务的实例信息，
取其中的一个拿到服务实例的主机、端口，然后拼成url进行调用，这一步算是将写死在
订单服务中的商品服务主机名、端口解耦出来。如：
```
上面的代码：

Product product = restTemplate.getForObject("http://localhost:8081/product/" + productId, Product.class);

改成如下：

AtomicInteger i = new AtomicInteger();

@Autowired
DiscoveryClient discoveryClient;

// 引入nacos后，可以拿到远程服务实例
ServiceInstance serviceInstance = discoveryClient.getInstances("product-service").get((discoveryClient.getInstances("product-service")).size()%i.get());
i.incrementAndGet();
String host = serviceInstance.getHost();
int port = serviceInstance.getPort();
String url = "http://" + host + ":" + port + "/product/" + productId;
// 通过RestTemplate调用
Product product = restTemplate.getForObject(url, Product.class);
```
上面的代码中维护一个计数器，然后做到客户端对服务负载均衡

#### 使用Ribbon进行负载均衡
负载均衡一般分为两种：客户端负载均衡、服务器负载均衡
客户端负载均衡：是指客户端拿到服务实例后，在调用端做的负载均衡。常见的如Ribbon
服务器负载均衡：是指服务器接受请求后，选择哪个服务实例来响应。常见的如Nginx

##### 让RestTemplate具备负载均衡的能力
```
@Bean
@LoadBalanced // 集成Ribbon进行负载均衡
public RestTemplate restTemplate() {
    return new RestTemplate();
}

上面订单服务调用商品服务的的代码改成:
String url = "http://product-service/product/" + productId;
// 通过RestTemplate调用
Product product = restTemplate.getForObject(url, Product.class);
```

##### 通过配置调整服务的负载均衡策略
```
product-service:
  # 配置product-service服务的ribbon负载均衡策略
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
```

#### 使用Feign进行远程调用
前面远程调用是通过RestTemplate，但是这种方式还是耦合的太紧，需要在业务代码指定远程服务、传参、返回结果，这些都不够灵活。下面引入Feign组件
替换掉RestTemplate进行远程调用。

##### 什么是Feign
Feign是Spring Cloud提供的一个声明式的伪http客户端，它使得调用远程服务就像调用本地服务一样简单，只需要创建一个接口并添加一个注解即可。
Nacos很好的兼容了Feign，Feign默认集成了Ribbon，所以在Nacos下使用Feign默认就实现了负载均衡的效果。

##### 使用
1. 引入依赖
在订单服务中
```
<!-- Feign组件 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
2. 启动类上添加@EnableFeignClients

3.创建远程调用接口
```
@FeignClient(name = "product-service")
public interface ProductFeignApi {

    @RequestMapping("/product/${pid}")
    Product findByPid(@PathVariable("pid") Long pid);
}
```

4.调用
将上面使用RestTemplate调用的代码改成如下：
```
@Autowired
ProductFeignApi productFeignApi;

Product product = productFeignApi.findByPid(productId);
```
注意：只需要对调用方做这些事，被调用方没做任何关于feign的事，因为被调用方的接口是http接口，而feign也是通过http调用的。

### 引入Sentinel
见README-Sentinel.md文档

### 引入Spring Cloud Gateway
见README-Gateway.md文档

### 集成链路追踪组件Sleuth、Zipkin
见README-Sleuth-Zipkin.md文档

### 引入Nacos Config配置中心
见README-Nacos-config.md文档

### 分布式调度Elastic-job
见README-Elastic-job.md文档

### 引入消息中间件RocketMQ
见README-RocketMQ.md文档

### 引入Seata分布式事务解决方案
见README-Seata.md文档



