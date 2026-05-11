<template>
  <el-card>
    <h3>个人账户信息</h3>
    <el-form
      ref="profileRef"
      v-loading="loading"
      :model="form"
      :rules="profileRules"
      label-width="120px"
      style="max-width: 520px; margin-top: 16px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="系统角色" prop="role">
        <el-input :model-value="roleLabel" disabled readonly />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="真实姓名" maxlength="30" show-word-limit clearable />
      </el-form-item>
      <el-form-item label="职称" prop="title">
        <el-input v-model="form.title" placeholder="如：主治医师" maxlength="30" clearable />
      </el-form-item>
      <el-form-item label="科室" prop="department">
        <el-input v-model="form.department" placeholder="所在科室" maxlength="50" clearable />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" clearable />
      </el-form-item>
      <el-form-item label="头像URL" prop="avatar">
        <el-input v-model="form.avatar" placeholder="可选，图片完整地址" maxlength="500" show-word-limit clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
      </el-form-item>
    </el-form>
    <el-divider />
    <h3>修改密码</h3>
    <el-form
      ref="pwdRef"
      :model="pwd"
      :rules="pwdRules"
      label-width="120px"
      style="max-width: 520px; margin-top: 16px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwd.oldPassword" type="password" show-password autocomplete="current-password" placeholder="请输入当前密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwd.newPassword" type="password" show-password autocomplete="new-password" placeholder="至少 6 位，建议字母+数字" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="newPassword2">
        <el-input v-model="pwd.newPassword2" type="password" show-password autocomplete="new-password" placeholder="再次输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="warning" :loading="savingPwd" @click="savePassword">修改密码</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useUserStore } from '../stores/user'
import { MOBILE_PATTERN } from '../utils/validators'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const savingProfile = ref(false)
const savingPwd = ref(false)
const profileRef = ref()
const pwdRef = ref()

const form = reactive({
  username: '',
  role: '',
  realName: '',
  title: '',
  department: '',
  phone: '',
  avatar: ''
})

const pwd = reactive({ oldPassword: '', newPassword: '', newPassword2: '' })

const roleLabel = computed(() => {
  if (form.role === 'ADMIN') return '系统管理员'
  if (form.role === 'DEPT_HEAD') return '科室负责人'
  if (form.role === 'DOCTOR') return '医生'
  return form.role || '—'
})

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: MOBILE_PATTERN, message: '请输入 11 位手机号', trigger: 'blur' }
  ]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ],
  newPassword2: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v !== pwd.newPassword) cb(new Error('两次输入的新密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function fetchProfile() {
  loading.value = true
  try {
    const data = await http.get('/me')
    Object.assign(form, data)
    user.setSession(user.token, data)
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  try {
    await profileRef.value?.validate()
  } catch {
    ElMessage.warning('请检查个人信息标红项')
    return
  }
  savingProfile.value = true
  try {
    await http.put('/me', {
      realName: form.realName,
      title: form.title,
      department: form.department,
      phone: form.phone,
      avatar: form.avatar
    })
    ElMessage.success('已保存')
    await fetchProfile()
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  try {
    await pwdRef.value?.validate()
  } catch {
    ElMessage.warning('请检查密码表单项')
    return
  }
  savingPwd.value = true
  try {
    await http.put('/me/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已更新，请重新登录')
    pwd.oldPassword = ''
    pwd.newPassword = ''
    pwd.newPassword2 = ''
    user.clear()
    router.push('/login')
  } finally {
    savingPwd.value = false
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
h3 {
  margin: 0;
  color: #1a5f7a;
}
</style>
