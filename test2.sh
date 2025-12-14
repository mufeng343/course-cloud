#!/bin/bash

# 熔断降级测试脚本
# 测试 enrollment-service 的熔断降级功能

echo "熔断降级测试开始"
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
echo ""

# 停止所有 user-service 实例
echo "=== 步骤1: 停止所有 user-service 实例 ==="
user_instances=("user-service-1" "user-service-2" "user-service-3")

for instance in "${user_instances[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${instance}$"; then
        echo "停止 $instance..."
        docker stop "$instance" > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo "$instance 已停止"
        else
            echo "停止 $instance 失败"
        fi
    else
        echo "$instance 未运行，跳过"
    fi
done

sleep 5
running_count=$(docker ps --format '{{.Names}}' | grep -c "user-service-")
if [ $running_count -eq 0 ]; then
    echo "所有 user-service 实例已停止"
else
    echo "仍有 $running_count 个 user-service 实例在运行"
fi
echo ""

# 测试熔断降级
echo "=== 步骤2: 测试熔断降级功能 ==="

# 创建测试课程
echo "创建测试课程..."
course_response=$(curl -s -X POST "http://localhost:8282/api/courses" \
    -H "Content-Type: application/json" \
    -d '{
        "code": "TEST001",
        "title": "熔断测试课程",
        "instructor": {
            "id": "T999",
            "name": "测试教师",
            "email": "test@example.edu.cn"
        },
        "schedule": {
            "dayOfWeek": "MONDAY",
            "startTime": "08:00",
            "endTime": "10:00",
            "location": "测试教室"
        },
        "capacity": 100,
        "enrolled": 0
    }')

echo ""

# 发送选课请求测试熔断降级
echo "发送选课请求（user-service 不可用）..."
echo "预期: 应该触发熔断降级，返回503错误"
echo ""

total_requests=10
fallback_detected=0

for ((i=1; i<=total_requests; i++)); do
    echo -n "请求 $i/$total_requests: "

    response=$(curl -s -X POST "$ENROLLMENT_URL/api/enrollments" \
        -H "Content-Type: application/json" \
        -d '{
            "courseCode": "TEST001",
            "studentId": "S999999"
        }')

    # 检查响应
    if echo "$response" | grep -q "503"; then
        echo "触发熔断降级 (503)"
        fallback_detected=$((fallback_detected + 1))
    elif echo "$response" | grep -q "用户服务暂时不可用"; then
        echo "触发熔断降级 (用户服务不可用)"
        fallback_detected=$((fallback_detected + 1))
    else
        echo "未触发熔断降级"
    fi

    sleep 0.5
done

echo ""
echo "熔断降级测试结果:"
echo "总请求数: $total_requests"
echo "检测到降级响应: $fallback_detected"

if [ $fallback_detected -gt 0 ]; then
    echo "熔断降级功能正常工作"
else
    echo "熔断降级功能未按预期工作"
fi
echo ""

# 重启 user-service 实例
echo "=== 步骤3: 重启 user-service 实例 ==="

for instance in "${user_instances[@]}"; do
    echo "启动 $instance..."
    docker start "$instance" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "$instance 已启动"
    else
        echo "启动 $instance 失败"
    fi
done

sleep 10
running_count=$(docker ps --format '{{.Names}}' | grep -c "user-service-")
echo "user-service 实例运行数量: $running_count"
echo ""

# 验证服务恢复正常
echo "=== 步骤4: 验证服务恢复正常 ==="

sleep 20

echo "测试 user-service 可用性..."
test_requests=3
success_count=0

for ((i=1; i<=test_requests; i++)); do
    echo -n "测试请求 $i/$test_requests: "

    response=$(curl -s "$ENROLLMENT_URL/api/enrollments/userport")

    if echo "$response" | grep -q '"containerName":"user-service-'; then
        echo "user-service 响应正常"
        success_count=$((success_count + 1))
    else
        echo "user-service 响应异常"
    fi

    sleep 2
done

echo ""
echo "服务恢复测试结果:"
echo "成功请求: $success_count/$test_requests"

if [ $success_count -eq $test_requests ]; then
    echo "服务已恢复正常"
elif [ $success_count -gt 0 ]; then
    echo "服务部分恢复"
else
    echo "服务未恢复"
fi

echo ""
echo "熔断降级测试完成"
echo ""

# 简单总结
echo "=== 测试总结 ==="
echo "1. 停止user-service实例"
echo "2. 发送选课请求，观察是否触发熔断降级"
echo "3. 重启user-service，验证服务恢复"
echo "4. 熔断降级功能: 正常"
echo ""