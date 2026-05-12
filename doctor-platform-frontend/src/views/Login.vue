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
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              maxlength="4"
              class="captcha-input"
              @keyup.enter="onSubmit"
            />
            <img
              :src="captchaSrc"
              alt="验证码"
              class="captcha-img"
              title="点击刷新验证码"
              @click="refreshCaptcha"
            />
          </div>
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
import { ref, reactive, computed, onMounted, nextTick, onUnmounted } from 'vue'
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
const captchaKey = ref('')
const captchaSrc = ref('')
const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码为 4 位', trigger: 'blur' }
  ]
}

const redirectTarget = computed(() => safeRedirectPath(route.query.redirect))

async function loadCaptcha() {
  const base = (import.meta.env.VITE_API_BASE?.trim() || '/api').replace(/\/$/, '')
  try {
    const resp = await fetch(`${base}/auth/captcha?t=${Date.now()}`)
    const key = resp.headers.get('Captcha-Key')
    if (key) {
      captchaKey.value = key
    }
    const blob = await resp.blob()
    if (captchaSrc.value && captchaSrc.value.startsWith('blob:')) {
      URL.revokeObjectURL(captchaSrc.value)
    }
    captchaSrc.value = URL.createObjectURL(blob)
  } catch {
    captchaKey.value = ''
    captchaSrc.value = ''
  }
}

function refreshCaptcha() {
  form.captchaCode = ''
  loadCaptcha()
}

async function onSubmit() {
  loginHint.value = ''
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完整填写登录信息')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/auth/login', {
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })
    user.setSession(data.token, data.doctor)
    ElMessage.success('登录成功')
    await router.push(redirectTarget.value)
  } catch (err) {
    refreshCaptcha()
    if (!err.response) {
      loginHint.value = '无法连接服务器，请检查网络或后端是否已启动'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})

onUnmounted(() => {
  if (captchaSrc.value && captchaSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(captchaSrc.value)
  }
})
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
.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  height: 40px;
  width: 110px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  flex-shrink: 0;
}
</style>
