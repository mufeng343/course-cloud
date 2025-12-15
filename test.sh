#!/bin/bash

# 负载均衡测试脚本
# 验证 userport 和 courseport 接口的负载均衡效果

echo "负载均衡测试开始"
echo ""

# 基础URL
ENROLLMENT_URL="http://localhost:8083"

# 检查服务是否可用
echo "检查服务可用性..."
if ! curl -s --head --request GET "$ENROLLMENT_URL/api/enrollments" | grep "200" > /dev/null; then
    echo "错误: enrollment-service 服务不可用"
    exit 1
fi
echo "服务正常可用"
echo ""

# 测试 userport 接口负载均衡
echo "=== 测试 user-service 负载均衡 (50次请求) ==="
user_counts=()

for ((i=1; i<=50; i++)); do
    response=$(curl -s "$ENROLLMENT_URL/api/enrollments/userport")
    container_name=$(echo "$response" | grep -o '"containerName":"[^"]*' | cut -d'"' -f4)

    if [ -n "$container_name" ]; then
        echo "请求 $i: $container_name"
        user_counts+=("$container_name")
    else
        echo "请求 $i: 无法获取容器信息"
    fi
    sleep 0.01  # 避免请求过快
done

echo ""
echo "user-service 负载均衡统计:"
for instance in $(echo "${user_counts[@]}" | tr ' ' '\n' | sort | uniq); do
    count=$(echo "${user_counts[@]}" | tr ' ' '\n' | grep -c "$instance")
    percentage=$(echo "scale=1; $count * 100 / 50" | bc)
    echo "  $instance: $count 次 (${percentage}%)"
done
echo ""

# 测试 courseport 接口负载均衡
echo "=== 测试 catalog-service 负载均衡 (50次请求) ==="
course_counts=()

for ((i=1; i<=50; i++)); do
    response=$(curl -s "$ENROLLMENT_URL/api/enrollments/courseport")
    container_name=$(echo "$response" | grep -o '"containerName":"[^"]*' | cut -d'"' -f4)

    if [ -n "$container_name" ]; then
        echo "请求 $i: $container_name"
        course_counts+=("$container_name")
    else
        echo "请求 $i: 无法获取容器信息"
    fi
    sleep 0.01  # 避免请求过快
done

echo ""
echo "catalog-service 负载均衡统计:"
for instance in $(echo "${course_counts[@]}" | tr ' ' '\n' | sort | uniq); do
    count=$(echo "${course_counts[@]}" | tr ' ' '\n' | grep -c "$instance")
    percentage=$(echo "scale=1; $count * 100 / 50" | bc)
    echo "  $instance: $count 次 (${percentage}%)"
done

echo ""
echo "测试完成"
echo ""

# 验证结果
echo "=== 验证结果 ==="
total_user_requests=${#user_counts[@]}
total_course_requests=${#course_counts[@]}

if [ $total_user_requests -eq 50 ] && [ $total_course_requests -eq 50 ]; then
    echo "✓ 所有100次请求都成功返回"
else
    echo "⚠ 请求成功率: user-service $total_user_requests/50, catalog-service $total_course_requests/50"
fi

user_unique=$(echo "${user_counts[@]}" | tr ' ' '\n' | sort -u | wc -l)
course_unique=$(echo "${course_counts[@]}" | tr ' ' '\n' | sort -u | wc -l)

echo "实例数量统计:"
echo "  user-service 检测到 $user_unique 个实例"
echo "  catalog-service 检测到 $course_unique 个实例"

if [ $user_unique -gt 1 ] || [ $course_unique -gt 1 ]; then
    echo "✓ 负载均衡生效 - 请求被分发到多个实例"
else
    echo "⚠ 每个服务只检测到一个实例，请确保启动了多个实例"
fi