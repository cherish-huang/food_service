#!/bin/bash
set -e

SERVICE_NAME=$1

# 克隆代码仓库并切换分支
git clone https://github.com/cherish-huang/food_service.git
cd food_service
git checkout master

# 编译打包
cd ~/Projects/Food/service
mvn package -DskipTests > /dev/null
echo "1.maven package successful!"

# 将服务target目录下的jar拷贝到指定服务目录下
cd $SERVICE_NAME
cp target/*.jar ./

# 停止原来的容器并且删除容器镜像
set +e
docker stop $SERVICE_NAME > /dev/null
docker rm $SERVICE_NAME > /dev/null
docker rmi $SERVICE_NAME:latest > /dev/null
echo "2.clear old container and image successful!"

# 构建Docker镜像并运行
set -e
docker build -t $SERVICE_NAME:latest . > /dev/null 2> /dev/null
echo "3.docker build image successful!"

docker run -d --network share-network --name $SERVICE_NAME $SERVICE_NAME > /dev/null
echo "4.deployment successful!"