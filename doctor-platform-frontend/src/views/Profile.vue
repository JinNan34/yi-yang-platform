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
      <el-form-item label="头像" prop="avatar">
        <div class="avatar-upload">
          <el-avatar :size="120" :src="avatarUrl" class="avatar-preview">
            <User />
          </el-avatar>
          <div class="upload-btn">
            <el-button size="small" type="primary" :loading="uploading" @click="triggerUpload">
              <Upload />
              {{ uploading ? '上传中...' : '更换头像' }}
            </el-button>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              class="file-input"
              @change="handleFileChange"
            />
          </div>
          <p class="upload-tip">支持 jpg、jpeg、png、gif、webp 格式，建议尺寸 120x120</p>
        </div>
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
import { User, Upload } from '@element-plus/icons-vue'
import http from '../api/http'
import { useUserStore } from '../stores/user'
import { MOBILE_PATTERN } from '../utils/validators'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const savingProfile = ref(false)
const savingPwd = ref(false)
const uploading = ref(false)
const profileRef = ref()
const pwdRef = ref()
const fileInputRef = ref()

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

const avatarUrl = computed(() => {
  if (!form.avatar) return ''
  if (form.avatar.startsWith('http')) return form.avatar
  return form.avatar
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

function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('请上传图片格式文件')
    return
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await fetch('/api/me/avatar', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + user.token
      },
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      form.avatar = result.data.url
      user.setSession(user.token, form)
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(result.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败：' + error.message)
  } finally {
    uploading.value = false
    event.target.value = ''
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

.avatar-upload {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.avatar-preview {
  border: 2px solid #e4e7ed;
  border-radius: 50%;
}

.upload-btn {
  margin-top: 8px;
  position: relative;
}

.file-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.upload-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
}
</style>