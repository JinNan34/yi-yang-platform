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
      <el-button type="success" @click="openEdit()">新增评估</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无健康评估记录"
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="elderName" label="老人姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="elderId" label="老人ID" width="90" />
      <el-table-column prop="score" label="评分" width="80" />
      <el-table-column prop="conclusion" label="结论" show-overflow-tooltip />
      <el-table-column prop="assessmentTime" label="评估时间" width="170" />
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
      :title="form.id ? '编辑健康评估' : '新增健康评估'"
      width="600px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联老人" prop="elderId">
          <ElderSelect v-model="form.elderId" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :step="1" class="wfull" placeholder="0～100" />
          <div class="form-tip">可按院内量表规则填写总分</div>
        </el-form-item>
        <el-form-item label="结论" prop="conclusion">
          <el-input v-model="form.conclusion" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="综合结论与建议" />
        </el-form-item>
        <el-form-item label="评估时间" prop="assessmentTime">
          <el-date-picker
            v-model="form.assessmentTime"
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
import ElderSelect from '../components/ElderSelect.vue'
import { required } from '../utils/validators'

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = 10
const query = reactive({ elderName: '', elderId: undefined })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, elderId: undefined, score: null, conclusion: '', assessmentTime: '' })

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
  score: [
    { required: true, message: '请填写评分', trigger: 'change' },
    { type: 'number', min: 0, max: 100, message: '评分范围为 0～100', trigger: 'change' }
  ],
  conclusion: required('请填写评估结论')
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/health-assessments', {
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
  Object.assign(form, { id: null, elderId: query.elderId ?? undefined, score: null, conclusion: '', assessmentTime: '' })
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
    ElMessage.warning('请完善评估信息')
    return
  }
  saving.value = true
  try {
    if (form.id) await http.put(`/health-assessments/${form.id}`, form)
    else await http.post('/health-assessments', form)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该条健康评估？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/health-assessments/${row.id}`)
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
}
</style>
