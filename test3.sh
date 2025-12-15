#!/bin/bash

# 故障转移测试脚本
# 测试微服务的故障转移能力

echo "故障转移测试开始: $(date)"
echo ""

# 基础URL
ENROLLMENT_URL="http://localhost:8083"

# 检查服务可用性
echo "检查服务可用性..."
if ! curl -s --head --request GET "$ENROLLMENT_URL/api/enrollments" | grep "200" > /dev/null; then
    echo "错误: enrollment-service 服务不可用"
    exit 1
fi
echo "enrollment-service 服务正常"

# 检查 user-service 实例
echo "检查 user-service 实例..."
user_instances=("user-service-1" "user-service-2" "user-service-3")
running_instances=0

for instance in "${user_instances[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${instance}$"; then
        echo "$instance 正在运行"
        running_instances=$((running_instances + 1))
    else
        echo "$instance 未运行"
    fi
done

if [ $running_instances -eq 0 ]; then
    echo "错误: 没有 user-service 实例在运行"
    exit 1
fi
echo ""

# 停止一个 user-service 实例
echo "=== 步骤1: 停止一个 user-service 实例 ==="
instance_to_stop="user-service-1"

if docker ps --format '{{.Names}}' | grep -q "^${instance_to_stop}$"; then
    echo "停止 $instance_to_stop..."
    docker stop "$instance_to_stop" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "$instance_to_stop 已停止"
    else
        echo "停止 $instance_to_stop 失败"
    fi
else
    echo "$instance_to_stop 未运行，跳过"
fi

sleep 2
running_count=$(docker ps --format '{{.Names}}' | grep -c "user-service-")
echo "剩余运行实例数: $running_count"
echo ""

# 测试故障转移
echo "=== 步骤2: 测试故障转移功能 ==="
total_requests=30
instance_counts=()

echo "发送 $total_requests 个请求到 userport 接口..."
echo ""

for ((i=1; i<=total_requests; i++)); do
    echo -n "请求 $i/$total_requests: "

    response=$(curl -s "$ENROLLMENT_URL/api/enrollments/userport")
    container_name=$(echo "$response" | grep -o '"containerName":"[^"]*' | cut -d'"' -f4)

    if [ -n "$container_name" ]; then
        echo "路由到 $container_name"
        instance_counts+=("$container_name")
    else
        echo "无法获取容器信息"
    fi

    sleep 0.05
done

echo ""
echo "user-service 故障转移统计:"

for instance in $(echo "${instance_counts[@]}" | tr ' ' '\n' | sort | uniq); do
    count=$(echo "${instance_counts[@]}" | tr ' ' '\n' | grep -c "$instance")
    percentage=$(echo "scale=1; $count * 100 / $total_requests" | bc)
    echo "实例: $instance"
    echo "  调用次数: $count"
    echo "  占比: ${percentage}%"
    echo ""
done

total_count=${#instance_counts[@]}
if [ $total_count -eq $total_requests ]; then
    echo "所有请求都成功处理"
else
    echo "部分请求未能获取容器信息"
fi
echo ""

# 重启 user-service 实例
echo "=== 步骤3: 重启 user-service 实例 ==="
instance_to_restart="user-service-1"

echo "启动 $instance_to_restart..."
docker start "$instance_to_restart" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "$instance_to_restart 已启动"
else
    echo "启动 $instance_to_restart 失败"
fi

sleep 10
running_count=$(docker ps --format '{{.Names}}' | grep -c "user-service-")
echo "user-service 实例运行数量: $running_count"
echo ""

# 验证服务恢复正常
echo "=== 步骤4: 验证服务恢复正常 ==="
sleep 20

echo "测试 user-service 可用性..."
test_requests=5
success_count=0

for ((i=1; i<=test_requests; i++)); do
    echo -n "测试请求 $i/$test_requests: "

    response=$(curl -s "$ENROLLMENT_URL/api/enrollments/userport")

    if echo "$response" | grep -q '"containerName":"user-service-'; then
        echo "响应正常"
        success_count=$((success_count + 1))
    else
        echo "响应异常"
    fi

    sleep 2
done

echo ""
echo "服务恢复测试结果:"
echo "成功请求: $success_count/$test_requests"

if [ $success_count -eq $test_requests ]; then
    echo "服务已完全恢复正常"
elif [ $success_count -gt 0 ]; then
    echo "服务部分恢复"
else
    echo "服务未恢复"
fi

echo ""
echo "故障转移测试完成: $(date)"
echo ""

# 总结
echo "=== 测试总结 ==="
echo "1. 检查服务状态"
echo "2. 停止一个user-service实例"
echo "3. 发送请求测试故障转移"
echo "4. 重启停止的实例"
echo "5. 验证服务恢复正常"
echo ""