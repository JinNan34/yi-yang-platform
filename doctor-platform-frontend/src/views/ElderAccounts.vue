<template>
  <el-card>
    <div class="toolbar">
      <el-input
        v-model.trim="query.elderName"
        placeholder="按老人姓名模糊查询"
        clearable
        maxlength="50"
        show-word-limit
        class="name-filter"
        @clear="search"
      />
      <el-input-number
        v-model="query.elderId"
        :min="1"
        :step="1"
        controls-position="right"
        placeholder="老人 ID（可选）"
        class="num-filter"
      />
      <span class="hint">姓名、ID 可组合；均留空则查全部</span>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button v-if="isAdmin" type="success" @click="openEdit()">新增账户</el-button>
    </div>
    <el-alert
      class="mb"
      type="info"
      :closable="false"
      show-icon
      :title="alertTitle"
    />
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无账户数据"
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="elderName" label="老人姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="elderId" label="老人ID" width="90" />
      <el-table-column prop="accountNo" label="账户号" width="140" />
      <el-table-column prop="balance" label="余额" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="isAdmin" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="isAdmin" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="total, prev, pager, next"
      :total="table.total"
      v-model:current-page="page"
      :page-size="size"
      @current-change="load"
    />
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑老人账户' : '新增老人账户'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联老人" prop="elderId">
          <ElderSelect v-model="form.elderId" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="账户号" prop="accountNo">
          <el-input
            v-model="form.accountNo"
            placeholder="如 EA1，需与业务约定唯一"
            maxlength="32"
            show-word-limit
            :disabled="!!form.id"
            clearable
          />
          <div v-if="form.id" class="form-tip">账户号创建后不建议修改，如需调整请联系管理员流程。</div>
        </el-form-item>
        <el-form-item label="余额" prop="balance">
          <el-input-number v-model="form.balance" :min="0" :precision="2" :step="10" class="wfull" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import http from '../api/http'
import ElderSelect from '../components/ElderSelect.vue'
import { useUserStore } from '../stores/user'
import { required } from '../utils/validators'

const user = useUserStore()
const isAdmin = computed(() => user.profile?.role === 'ADMIN')
const alertTitle = computed(() =>
  isAdmin.value
    ? '新建老人档案时系统会自动开户。补建、编辑、删除账户仅管理员可操作；删除为彻底删除，删除后可再次为该老人新建账户。'
    : '新建老人档案时系统会自动开户。补建、编辑、删除账户仅系统管理员可操作；您可在此查询与浏览账户列表。'
)

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = 10
const query = reactive({ elderName: '', elderId: undefined })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, elderId: undefined, accountNo: '', balance: 0, status: 1 })

const rules = {
  elderId: [
    {
      required: true,
      validator: (_r, v, cb) => {
        if (v == null || v === '' || Number(v) < 1) cb(new Error('请选择关联老人'))
        else cb()
      },
      trigger: 'change'
    }
  ],
  accountNo: required('请填写账户号'),
  balance: [{ type: 'number', min: 0, message: '余额不能为负数', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/elder-accounts', {
      params: {
        page: page.value,
        size,
        elderName: query.elderName || undefined,
        elderId: query.elderId || undefined
      }
    })
    table.records = data.records || []
    table.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function resetForm() {
  formRef.value?.clearValidate?.()
  Object.assign(form, { id: null, elderId: query.elderId ?? undefined, accountNo: '', balance: 0, status: 1 })
}

function openEdit(row) {
  resetForm()
  if (row) Object.assign(form, row)
  visible.value = true
}

async function save() {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善账户信息')
    return
  }
  saving.value = true
  try {
    if (form.id) await http.put(`/elder-accounts/${form.id}`, form)
    else await http.post('/elder-accounts', form)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      '确定彻底删除该老人账户？删除后 elder_id 将释放，可再次新建；请确认账务已结清。',
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await http.delete(`/elder-accounts/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}
.hint {
  font-size: 12px;
  color: #909399;
}
.name-filter {
  width: 220px;
}
.num-filter {
  width: 168px;
}
.mb {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.wfull {
  width: 100%;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
