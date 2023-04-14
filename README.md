项目指引

curl -O http://mirror.centos.org/centos/7/os/x86_64/Packages/yum-3.4.3-168

docker network create share-network
1.安装mysql

 1.1 docker pull mysql:5.7
 1.2 编写本地my.cnf
 [mysqld]
   log-bin=mysql-bin
   server-id=1
   character-set-server=utf8mb4
   [client]
   default-character-set=utf8mb4
   [mysql]
   default-character-set=utf8mb4
 1.3 docker run --name mysql --network share-network \
      --privileged=true \
      -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 \
      -v ~/docker_data/mysql/conf/:/etc/mysql/my.cnf \
      -v ~/docker_data/mysql/log/:/var/log/mysql/ \
      -v ~/docker_data/mysql/data/:/var/lib/mysql/ \
      -d mysql:5.7

2. 安装redis

   1.1 docker pull redis 
   1.2 docker run --name redis --network share-network --privileged=true -v ~/docker_data/redis/data:/data -p 6379:6379 -d redis

3. 安装etcd

    1.1 docker pull bitnami/etcd 
    1.2 docker run -p 2379:2379 --name etcd --network share-network -e ALLOW_NONE_AUTHENTICATION=yes -d bitnami/etcd

4. 安装zookeeper
   由于kafka依赖zk，所以需要创建一个network让kafka连接上zk
   2. docker pull zookeeper
   3. docker run -d -p 2181:2181 --name zookeeper --network share-network zookeeper

5. 安装kafka
   1. docker pull bitnami/kafka
   2. docker run -d --name kafka --network share-network -p 9092:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 bitnami/kafka
      docker run -d --name kafka --network share-network -p 9092:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 bitnami/kafka
      kafka-topics.sh --create --bootstrap-server=localhost:9092 --replication-factor 1 --partitions 1 --topic order_event
      kafka-topics.sh --create --bootstrap-server=localhost:9092 --replication-factor 1 --partitions 1 --topic delivery_event
      
6. 安装elasticsearch
   1. docker pull elasticsearch:7.14.0
   2. docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name elasticsearch --network share-network  elasticsearch:7.14.0
   3. 安装ik分词器 bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.14.0/elasticsearch-analysis-ik-7.14.0.zip

7. 安装kibana
   1. docker pull kibana:7.14.0
   2. docker run -p 5601:5601 -d --network share-network --name kibana -e ELASTICSEARCH_URL=http://elasticsearch:9200 kibana:7.14.0

8. 安装canal
   1. docker pull canal/canal-server:v1.1.4
   2. docker run --name canal \
      -e canal.instance.master.address=mysql:3306 \
      -e canal.instance.dbUsername=root \
      -e canal.instance.dbPassword=123456 \
      -p 11111:11111 \
      --network share-network \
      -d canal/canal-server:v1.1.4
   3. idgen服务打镜像运行
       本地测试可以现将服务的网络模式设置为主机模式，这样注册到注册中心的ip就是宿主机的ip而不是容器内的ip，这样在宿主机可以直接访问该服务
       docker run --name idgen-service-node01 -p 8011:8011 --network share-network \
       -e DUBBO_IP_TO_REGISTRY=10.53.48.80 \
       -e dubbo.protocol.port=8011 \
       -e workerId=1 -d idgen-service:1.0.0

      docker run --name idgen-service -p 8011:8011 --network share-network -d idgen-service:1.0.0
   4. store服务打镜像运行
      docker run --name store-service-node01 -p 8021:8021 --network share-network \
      -e DUBBO_IP_TO_REGISTRY=10.53.48.80 \
      -e dubbo.protocol.port=8021 \
      d store-service:1.0.0

      docker run --name store-service -p 8021:8021 --network share-network -d store-service:1.0.0

   5. cart服务打镜像运行
      store服务打镜像运行
      docker run --name cart-service -p 8031:8031 --network share-network -d cart-service:1.0.0
   6. order服务打镜像运行
      docker run --name order-service -p 8041:8041 --network share-network -d order-service:1.0.0
   7. delivery服务打镜像运行
      docker run --name delivery-service -p 8051:8051 --network share-network -d delivery-service:1.0.0
   8. buyer服务打镜像运行
      docker run --name buyer-service -p 8001:8001 --network share-network -d buyer-service:1.0.0
   


