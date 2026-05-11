<template>
  <el-card>
    <div class="toolbar">
      <el-input
        v-model="query.name"
        placeholder="按姓名模糊查询"
        clearable
        style="width: 220px"
        maxlength="50"
        show-word-limit
        @clear="search"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openEdit()">新增老人</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无数据，可先新增老人档案"
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" min-width="90" />
      <el-table-column prop="idCard" label="身份证" width="180" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="address" label="地址" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
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
      :title="form.id ? '编辑老人档案' : '新增老人档案'"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="与身份证一致为佳" maxlength="30" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="身份证" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18 位，末位可为 X" maxlength="18" clearable />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="wfull"
            :disabled-date="birthDisabledFuture"
          />
        </el-form-item>
        <el-form-item label="本人电话" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" clearable />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="现居住地址" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="紧急联系人" prop="emergencyContact">
          <el-input v-model="form.emergencyContact" placeholder="家属姓名" maxlength="30" clearable />
        </el-form-item>
        <el-form-item label="紧急联系电话" prop="emergencyPhone">
          <el-input v-model="form.emergencyPhone" placeholder="11 位手机号" maxlength="11" clearable />
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
import { reactive, ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import http from '../api/http'
import { required, optionalMobile, idCard18, MOBILE_PATTERN } from '../utils/validators'

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = 10
const query = reactive({ name: '' })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  idCard: '',
  gender: 1,
  birthDate: '',
  phone: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: ''
})

const rules = {
  name: required('请输入姓名'),
  idCard: [{ validator: idCard18, trigger: 'blur' }],
  birthDate: required('请选择出生日期', 'change'),
  phone: [
    { required: true, message: '请输入本人电话', trigger: 'blur' },
    { pattern: MOBILE_PATTERN, message: '请输入 11 位手机号', trigger: 'blur' }
  ],
  emergencyPhone: [{ validator: optionalMobile, trigger: 'blur' }]
}

function birthDisabledFuture(d) {
  return d.getTime() > Date.now()
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/elders', { params: { page: page.value, size, name: query.name || undefined } })
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
  Object.assign(form, {
    id: null,
    name: '',
    idCard: '',
    gender: 1,
    birthDate: '',
    phone: '',
    address: '',
    emergencyContact: '',
    emergencyPhone: ''
  })
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
    ElMessage.warning('请检查表单标红项')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await http.put(`/elders/${form.id}`, form)
    } else {
      await http.post('/elders', form)
    }
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」的档案？删除后关联数据请谨慎处理。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await http.delete(`/elders/${row.id}`)
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
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
  display: flex;
}
.wfull {
  width: 100%;
}
</style>
