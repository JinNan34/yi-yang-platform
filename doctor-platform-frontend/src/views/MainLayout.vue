<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">医生服务系统</div>
      <el-menu :default-active="active" router background-color="#1a5f7a" text-color="#e8f4f2" active-text-color="#ffd93d">
        <el-menu-item index="/"><span>首页</span></el-menu-item>
        <el-menu-item index="/elders">老人健康档案</el-menu-item>
        <el-menu-item index="/health-records">健康档案记录</el-menu-item>
        <el-menu-item index="/health-alerts">健康预警</el-menu-item>
        <el-menu-item index="/key-followups">重点人群随访</el-menu-item>
        <el-menu-item index="/followup-interventions">随访干预记录</el-menu-item>
        <el-menu-item index="/health-assessments">健康评估</el-menu-item>
        <el-menu-item index="/elder-accounts">老人账户管理</el-menu-item>
        <el-menu-item index="/profile">个人账户</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="welcome">
          {{ user.profile?.realName || user.profile?.username }}
          <span v-if="roleLabel" class="role-tag">（{{ roleLabel }}）</span>
        </span>
        <el-button type="danger" link @click="logout">退出</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const active = computed(() => route.path)

const roleLabel = computed(() => {
  const r = user.profile?.role
  if (r === 'ADMIN') return '系统管理员'
  if (r === 'DEPT_HEAD') return '科室负责人'
  if (r === 'DOCTOR') return '医生'
  return ''
})

function logout() {
  user.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #1a5f7a;
}
.brand {
  padding: 18px 16px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid rgba(255,255,255,0.12);
}
.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.welcome {
  color: #606266;
}
.role-tag {
  color: #909399;
  font-size: 13px;
}
.main {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>
