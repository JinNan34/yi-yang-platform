<template>
  <el-card>
    <h2>欢迎使用智慧医养大数据公共服务平台 · 医生服务系统</h2>
    <p class="muted">本系统涵盖：登录认证、老人健康档案、健康预警、重点人群随访、随访干预记录、健康评估、老人账户管理及医生个人账户等功能模块。</p>
    <el-descriptions title="技术说明" :column="1" border class="mt">
      <el-descriptions-item label="后端">Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8</el-descriptions-item>
      <el-descriptions-item label="前端">Vue 3、Vite、Element Plus、Pinia、Vue Router</el-descriptions-item>
      <el-descriptions-item label="扩展">已接入 Redis（登录失败限流）；可按课程要求补充 Nginx 反向代理与 Docker 部署</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style scoped>
.muted { color: #909399; line-height: 1.6; }
.mt { margin-top: 20px; }
h2 { margin-top: 0; color: #1a5f7a; }
</style>
