<template>
  <el-card>
    <div class="toolbar">
      <el-input-number
        v-model="query.elderId"
        :min="1"
        :step="1"
        controls-position="right"
        placeholder="老人 ID"
        class="num-filter"
      />
      <span class="hint">留空查全部</span>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openEdit()">新增干预记录</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无干预记录"
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="elderId" label="老人ID" width="90" />
      <el-table-column prop="followupId" label="随访计划ID" width="120" />
      <el-table-column prop="interventionType" label="干预类型" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="interventionTime" label="时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
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
      :title="form.id ? '编辑干预记录' : '新增干预记录'"
      width="600px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="老人ID" prop="elderId">
          <el-input-number v-model="form.elderId" :min="1" :step="1" class="wfull" />
        </el-form-item>
        <el-form-item label="随访计划ID" prop="followupId">
          <el-input-number v-model="form.followupId" :min="0" :step="1" class="wfull" placeholder="0 表示暂不关联计划" />
          <div class="form-tip">有重点随访计划时可填写对应 ID；无则填 0</div>
        </el-form-item>
        <el-form-item label="干预类型" prop="interventionType">
          <el-input v-model="form.interventionType" placeholder="如：用药调整、康复指导、转诊建议" maxlength="50" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="干预内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="具体措施与医嘱说明" />
        </el-form-item>
        <el-form-item label="干预时间" prop="interventionTime">
          <el-date-picker
            v-model="form.interventionTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="wfull"
            placeholder="留空则默认当前时间"
          />
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
import { required } from '../utils/validators'

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = 10
const query = reactive({ elderId: undefined })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  elderId: undefined,
  followupId: null,
  interventionType: '',
  content: '',
  interventionTime: ''
})

const rules = {
  elderId: [
    { required: true, message: '请填写老人 ID', trigger: 'change' },
    { type: 'number', min: 1, message: '老人 ID 须大于 0', trigger: 'change' }
  ],
  interventionType: required('请填写干预类型'),
  content: required('请填写干预内容')
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/followup-interventions', {
      params: { page: page.value, size, elderId: query.elderId || undefined }
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
  Object.assign(form, {
    id: null,
    elderId: query.elderId ?? undefined,
    followupId: null,
    interventionType: '',
    content: '',
    interventionTime: ''
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
    ElMessage.warning('请完善干预记录')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (payload.followupId === 0 || payload.followupId === undefined || payload.followupId === null) {
      delete payload.followupId
    }
    if (form.id) await http.put(`/followup-interventions/${form.id}`, payload)
    else await http.post('/followup-interventions', payload)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该条干预记录？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/followup-interventions/${row.id}`)
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
.num-filter {
  width: 160px;
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
  line-height: 1.4;
  margin-top: 4px;
}
</style>
