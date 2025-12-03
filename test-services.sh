#!/bin/bash
# test-services.sh

echo "=== 测试微服务拆分 ==="

# 1. 测试用户服务 - 创建学生
echo -e "\n1. 测试用户服务 - 创建学生"
curl -X POST http://localhost:8081/api/users/students \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'

# 2. 获取所有学生
echo -e "\n2. 获取所有学生"
curl http://localhost:8081/api/users/students

# 3. 测试课程目录服务 - 创建课程
echo -e "\n3. 测试课程目录服务 - 创建课程"
curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CSC101",
    "title": "计算机科学导论",
    "instructorId": "T001",
    "instructorName": "张教授",
    "instructorEmail": "zhang@example.edu.cn",
    "dayOfWeek": "MONDAY",
    "start": "08:00",
    "end": "10:00",
    "capacity": 60,
    "expectedAttendance": 50
  }'

# 4. 获取所有课程
echo -e "\n4. 获取所有课程"
curl http://localhost:8082/api/courses

# 5. 测试选课（验证服务间通信）
echo -e "\n5. 测试学生选课"
COURSE_ID=$(curl -s http://localhost:8082/api/courses | jq -r '.[0].id')
STUDENT_ID=$(curl -s http://localhost:8081/api/users/students | jq -r '.[0].id')
curl -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{
    \"courseId\": \"$COURSE_ID\",
    \"studentId\": \"$STUDENT_ID\"
  }"

# 6. 查询选课记录
echo -e "\n6. 查询选课记录"
curl http://localhost:8083/api/enrollments

# 7. 测试学生不存在的情况
echo -e "\n7. 测试选课失败（学生不存在）"
curl -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{
    \"courseId\": \"$COURSE_ID\",
    \"studentId\": \"9999999\"
  }"

# 8. 测试课程不存在的情况
echo -e "\n8. 测试选课失败（课程不存在）"
curl -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "non-existent-course",
    "studentId": "2024001"
  }'

echo -e "\n=== 测试完成 ==="
