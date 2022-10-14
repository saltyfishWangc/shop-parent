### 配置中心简介
#### 背景
微服务架构下关于配置文件的一些问题：
1. 配置文件相对分散，在一个微服务架构下，配置文件会随着微服务的增多变得越来越多，而且分散在各个微服务中，
不好统一配置和管理。
2. 配置文件无法区分环境。微服务项目可能会有多个环境，例如：测试环境、预发布环境、生产环境，每一个环境所
使用的配置理论上都是不同的，一旦需要修改，就需要我们去各个微服务下手动维护，这比较困难。
3. 配置文件无法实时更新，我们修改了配置文件之后，必须重新启动微服务才能使配置生效，这对一个正在运行的项目
来说是非常不友好的。
基于这样的一个背景，就需要引入配置中心来解决这些问题。

配置中心的思路是：
首先把项目中各种配置全部都放到一个集中的地方进行统一管理，并提供一套标准的接口。
当各个服务需要获取配置的时候，就来配置中心的接口拉取自己的配置。
当配置中心的各种参数有更新的时候，也能通知到各个服务实时的同步过来最新的消息，使之动态更新。

#### 常见的服务配置中心
* Apollo
Apollo是由携程开源的分布式配置中心，特点有很多，比如：配置更新之后可以实时生效。支持灰度发布功能，并且能对
所有的配置进行版本管理、操作审计等功能，提供开放平台API。并且资料也写得很详细。
* Disconf
Disconf是由百度开源的分布式配置中心，它是基于Zookeeper来实现配置变更后实时通知和生效。
* SpringCloud Config
这是Spring Cloud中带的配置中心组件，它和Spring是无缝集成，使用起来非常方便，并且它的配置存储支持Git。不过
它没有可视化的操作界面，配置的生效也不是实时的，需要重启或去刷新。要在gitee或者gitlab上去操作文件
* Nacos
这是Spring Cloud alibaba技术栈中的一个组件，前面已经使用它做过服务注册中心，其实它也集成了服务配置的功能，
可以直接使用它作为服务配置中心。

#### Nacos Config入门
1. 添加依赖
```
<!-- Nacos Config -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

2. 微服务中添加nacos config配置
注意：不能使用原来的application.yml作为配置文件，要新建一个bootstrap.yml作为配置文件
配置文件优先级：bootstrap.properties > bootstrap.yml > application.properties > application.yml
bootstrap.yml配置内容如下：
```
spring:
  application:
    name: order-service
  cloud:
    nacos:
      config:
        server-addr: 192.168.19.128:8848 #nacos中心地址
        file-extension: yaml #配置文件格式
  profiles:
    active: dev #环境标识
```

3. 在Nacos后台管理平台的配置管理->配置列表中添加配置
Data ID: order-service-dev.yaml  // 格式是${bootstrap.yml.spring.application.name}-${bootstrap.yml.spring.profiles.active}.${bootstrap.yml.spring.cloud.nacos.config.file-extension}
描述：order-service的dev环境配置
配置格式：YAML
配置内容：将application.yml中的内容赋值粘贴进去(注意去掉在bootstrap.yml中已经存在的配置)

在Nacos管理平台的配置管理中添加完后就可以把项目中的application.yml删掉了，只保留bootstrap.yml文件

4. 配置动态刷新
在上面的入门案例中，实现了配置的远程存放，但是此时如果修改了配置，我们的程序是无法读取到的。因此，我们
需要开启配置的动态刷新功能。
在配置类上加上@RefreshScope注解。因为代码里面是通过@Value来引用配置的，Nacos不可能有更新就来把所有的代码都找一遍@Value，
所以需要通过@RefreshScope来告诉Nacos，有更新了动态更新这个类里面的配置。
代码实现见：com.wangc.controller.NacosDynamicConfigTestController

5. 配置共享
当配置越来越多的时候，会发现有很多配置是重复的，这时候就需要考虑是否可以将公共配置文件提取出来，然后实现共享？当然是可以的，
下面看看如何实现该功能。

* 同一个微服务的不同环境之间共享配置：只需要提取一个以spring.application.name命令的配置文件，然后将其所有环境的公共配置放在里面。
* 不通微服务之间共享配置：不同微服务之间共享配置类似于文件引入，就是定义一个公共配置，然后在当前配置中引入。注意：这里是在项目代码
的bootstrap.yml中添加配置来引入：
```
spring:
    cloud:
        nacos:
            shared-configs:
                - data-id: ${Nacos后台管理配置的公共配置文件的DATA ID} # 配置要引入的位置
                    refresh: true
```