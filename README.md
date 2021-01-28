# springcloud-nacos-seata

**分布式事务组件seata的使用demo，AT模式，XA模式，TCC模式，集成nacos、springboot、springcloud、mybatis-plus，数据库采用mysql，根据官方demo做了修改**

demo中使用的相关版本号，具体请看代码。如果搭建个人demo不成功，验证是否是由版本导致，由于目前这几个项目更新比较频繁，版本稍有变化便会出现许多奇怪问题

* seata 1.4.0
* spring-cloud-alibaba-seata 2.2.0.RELEASE
* spring-cloud-starter-alibaba-nacos-discovery  2.2.3.RELEASE
* springboot 2.2.5.RELEASE
* mysql-connector-java 5.1.39（也可以支持8.X）

----------

## 1. 服务端配置

### 1.1 Nacos-server

版本为nacos-server-1.4.0，demo采用本地单机部署方式，请参考 [Nacos 快速开始](https://nacos.io/zh-cn/docs/quick-start.html)

### 1.2 Seata-server

seata-server为release版本1.4.0，demo采用本地单机部署，从此处下载 [https://github.com/seata/seata/releases](https://github.com/seata/seata/releases) 并解压

#### 1.2.1 配置springBoot集成参数

~~~
seata:
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      group: "SEATA_GROUP"
      namespace: ""
      username: "nacos"
      password: "nacos"
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      group: "SEATA_GROUP"
      namespace: ""
      username: "nacos"
      password: "nacos"
  enabled: true
  application-id: seata-server
  tx-service-group: my_test_tx_group
  enable-auto-data-source-proxy: true
  data-source-proxy-mode: XA
  use-jdk-proxy: false

~~~

#### 1.2.2 server端nacos配置初始化 
[参考配置](http://seata.io/zh-cn/docs/user/configurations.html)




#### 1.3 启动seata-server

**分两步，如下**

~~~shell

# 目录/script/config-center/nacos/nacos-config.sh
# 初始化seata 的nacos配置
cd conf
sh nacos-config.sh 192.168.21.89

# 启动seata-server
cd bin
sh seata-server.sh -p 8091 -m file
~~~

----------

## 2. 应用配置

### 2.1 数据库初始化

~~~SQL
-- 创建 order库、业务表、undo_log表
create database seata_order;
use seata_order;

CREATE TABLE `order_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT '0',
  `money` int(11) DEFAULT '0',
  `status` int(11) DEFAULT '0',
  `order_no` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;


-- 创建 storage库、业务表、undo_log表
create database seata_storage;
use seata_storage;

DROP TABLE IF EXISTS `storage_tbl`;
CREATE TABLE `storage_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

create database seata_account;
use seata_account;

CREATE TABLE `account_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `money` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- 初始化库存模拟数据
INSERT INTO seata_storage.storage_tbl (id, commodity_code, count) VALUES (1, 'product-1', 9999999);
INSERT INTO seata_storage.storage_tbl (id, commodity_code, count) VALUES (2, 'product-2', 0);
INSERT INTO seata_account.account_tbl (`id`, `user_id`, `money`)VALUES(1, '318', 1000);
INSERT INTO seata_account.account_tbl (`id`, `user_id`, `money`)VALUES(1, '1002', 1000);
~~~

### 2.2 应用配置

见代码

----------

## 3. 测试


1. 默认开启XA模式，分布式事务成功，模拟正常下单、扣库存

   localhost:9091/order/placeOrder/commit   

2. 默认开启XA模式，分布式事务失败，模拟下单成功、扣库存失败，最终同时回滚

   localhost:9091/order/placeOrder/rollback 
   
3. TCC模式
   localhost:9091/order/placeOrder/placeOrderTcc 

4. 开启AT模式只需注释com.work.framework.config.MyBatisConfig这个类
   再修改项目模式配置为
   seata.data-source-proxy-mode: AT
   即可



