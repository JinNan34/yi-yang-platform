<template>
  <el-container class="layout">
    <el-aside v-if="!isMobile" width="220px" class="aside">
      <div class="brand">医生服务系统</div>
      <el-menu
        :default-active="active"
        :default-openeds="desktopOpeneds"
        router
        background-color="#1a5f7a"
        text-color="#e8f4f2"
        active-text-color="#ffd93d"
      >
        <el-sub-menu v-for="g in menuGroups" :key="g.title" :index="'g-' + g.title">
          <template #title>{{ g.title }}</template>
          <el-menu-item v-for="it in g.items" :key="it.index" :index="it.index">{{ it.label }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-drawer
      v-else
      v-model="drawerVisible"
      class="side-drawer"
      direction="ltr"
      size="240px"
      :with-header="false"
    >
      <div class="brand">医生服务系统</div>
      <el-menu
        :default-active="active"
        :default-openeds="mobileOpeneds"
        router
        background-color="#1a5f7a"
        text-color="#e8f4f2"
        active-text-color="#ffd93d"
        @select="drawerVisible = false"
      >
        <el-sub-menu v-for="g in menuGroups" :key="'m-' + g.title" :index="'m-' + g.title">
          <template #title>{{ g.title }}</template>
          <el-menu-item v-for="it in g.items" :key="it.index" :index="it.index">{{ it.label }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-drawer>
    <el-container class="right">
      <el-header class="header">
        <div class="header-left">
          <el-button v-if="isMobile" class="menu-btn" text @click="drawerVisible = true">
            <el-icon :size="22"><Menu /></el-icon>
          </el-button>
          <span v-if="pageTitle" class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <span class="welcome">
            {{ user.profile?.realName || user.profile?.username }}
            <span v-if="roleLabel" class="role-tag">（{{ roleLabel }}）</span>
          </span>
          <el-button type="danger" link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <div class="main-inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const active = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '')

const isMobile = ref(false)
const drawerVisible = ref(false)

const desktopOpeneds = ['g-工作台', 'g-健康业务', 'g-系统与账户']
const mobileOpeneds = ['m-工作台', 'm-健康业务', 'm-系统与账户']

const menuGroups = [
  { title: '工作台', items: [{ index: '/', label: '首页' }] },
  {
    title: '健康业务',
    items: [
      { index: '/elders', label: '老人健康档案' },
      { index: '/health-records', label: '健康档案记录' },
      { index: '/health-alerts', label: '健康预警' },
      { index: '/key-followups', label: '重点人群随访' },
      { index: '/followup-interventions', label: '随访干预记录' },
      { index: '/health-assessments', label: '健康评估' }
    ]
  },
  {
    title: '系统与账户',
    items: [
      { index: '/elder-accounts', label: '老人账户管理' },
      { index: '/profile', label: '个人账户' }
    ]
  }
]

const roleLabel = computed(() => {
  const r = user.profile?.role
  if (r === 'ADMIN') return '系统管理员'
  if (r === 'DEPT_HEAD') return '科室负责人'
  if (r === 'DOCTOR') return '医生'
  return ''
})

function updateMobile() {
  isMobile.value = window.innerWidth < 992
  if (!isMobile.value) drawerVisible.value = false
}

watch(
  () => route.path,
  () => {
    drawerVisible.value = false
  }
)

onMounted(() => {
  updateMobile()
  window.addEventListener('resize', updateMobile)
})
onUnmounted(() => {
  window.removeEventListener('resize', updateMobile)
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
  background: var(--app-primary, #1a5f7a);
  flex-shrink: 0;
}
.right {
  min-width: 0;
}
.brand {
  padding: 18px 16px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  min-width: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.menu-btn {
  color: var(--app-primary, #1a5f7a);
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.welcome {
  color: #606266;
  font-size: 14px;
}
.role-tag {
  color: #909399;
  font-size: 13px;
}
.main {
  background: var(--app-bg-page, #f5f7fa);
  min-height: calc(100vh - 60px);
  padding: 16px 20px;
  box-sizing: border-box;
}
.main-inner {
  max-width: 1400px;
  margin: 0 auto;
  min-width: 0;
}
</style>

<style>
.side-drawer .el-drawer__body {
  padding: 0 !important;
  background: #1a5f7a;
}
</style>
