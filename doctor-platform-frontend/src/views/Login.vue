<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="title">智慧医养医生服务系统</div>
        <div class="sub">请使用分配账号登录</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            maxlength="50"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            show-password
            placeholder="请输入密码"
            maxlength="64"
          />
        </el-form-item>
        <p v-if="loginHint" class="login-error" role="alert">{{ loginHint }}</p>
        <el-button type="primary" native-type="submit" class="full" :loading="loading">登录</el-button>
      </el-form>
      <p v-if="isDev" class="hint">
        演示账号（密码均为 123456）：<br />
        doctor — 普通医生；depthead — 科室负责人；admin — 系统管理员
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useUserStore } from '../stores/user'
import { safeRedirectPath } from '../utils/safeRedirect'

const isDev = import.meta.env.DEV

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const loading = ref(false)
const loginHint = ref('')
const formRef = ref()
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const redirectTarget = computed(() => safeRedirectPath(route.query.redirect))

async function onSubmit() {
  loginHint.value = ''
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/auth/login', { ...form })
    user.setSession(data.token, data.doctor)
    ElMessage.success('登录成功')
    await router.push(redirectTarget.value)
  } catch (err) {
    if (!err.response) {
      loginHint.value = '无法连接服务器，请检查网络或后端是否已启动'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a5f7a 0%, #159895 100%);
}
.login-card {
  width: min(400px, 92vw);
}
.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--app-primary, #1a5f7a);
}
.sub {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 4px;
}
.full {
  width: 100%;
  margin-top: 8px;
}
.hint {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.5;
}
.login-error {
  margin: 0 0 8px;
  font-size: 13px;
  color: #f56c6c;
  line-height: 1.4;
}
</style>
