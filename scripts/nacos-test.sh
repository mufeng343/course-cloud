#!/bin/bash
set -euo pipefail

echo "启动所有服务 ..."
docker compose up -d

echo "等待服务启动 ..."
sleep 30

echo "检查 Nacos 控制台..."
curl -s http://localhost:8848/nacos/ >/dev/null && echo "Nacos 控制台可访问"

echo "检查服务注册情况 ..."
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service"
echo
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service"
echo
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service"

if command -v jq >/dev/null 2>&1; then
  echo "\n测试服务调用 ..."
  for i in {1..10}; do
    echo "第 $i 次请求:";
    curl -s http://localhost:8082/api/enrollments/test | jq .
  done
else
  echo "\n测试服务调用 ... (未检测到 jq，直接输出原始响应)"
  for i in {1..10}; do
    echo "第 $i 次请求:";
    curl -s http://localhost:8082/api/enrollments/test
  done
fi

echo "查看容器状态 ..."
docker compose ps
