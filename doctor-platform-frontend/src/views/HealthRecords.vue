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
        :controls="true"
        controls-position="right"
        placeholder="老人 ID（可选）"
        class="num-filter"
      />
      <span class="hint">姓名、ID 可单独或组合使用；均留空则查全部</span>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openEdit()">新增记录</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无体征记录，请选择老人或新增一条"
    >
      <el-table-column label="序号" type="index" width="64" :index="rowIndex" />
      <el-table-column prop="id" label="记录ID" width="88" />
      <el-table-column prop="elderName" label="老人姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="elderId" label="老人ID" width="90" />
      <el-table-column label="血压" width="120">
        <template #default="{ row }">{{ row.systolicBp ?? '—' }}/{{ row.diastolicBp ?? '—' }}</template>
      </el-table-column>
      <el-table-column prop="bloodSugar" label="血糖" width="80" />
      <el-table-column prop="heartRate" label="心率" width="70" />
      <el-table-column prop="recordTime" label="记录时间" width="170" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
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
      :title="form.id ? '编辑健康记录' : '新增健康记录'"
      width="600px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联老人" prop="elderId">
          <ElderSelect v-model="form.elderId" />
        </el-form-item>
        <el-form-item label="收缩压" prop="systolicBp">
          <el-input-number v-model="form.systolicBp" :min="0" :max="300" class="wfull" placeholder="mmHg" />
        </el-form-item>
        <el-form-item label="舒张压" prop="diastolicBp">
          <el-input-number v-model="form.diastolicBp" :min="0" :max="200" class="wfull" placeholder="mmHg" />
        </el-form-item>
        <el-form-item label="血糖" prop="bloodSugar">
          <el-input-number v-model="form.bloodSugar" :min="0" :max="50" :step="0.1" class="wfull" placeholder="mmol/L 可选" />
        </el-form-item>
        <el-form-item label="心率" prop="heartRate">
          <el-input-number v-model="form.heartRate" :min="0" :max="250" class="wfull" placeholder="次/分 可选" />
        </el-form-item>
        <el-form-item label="体温" prop="temperature">
          <el-input-number v-model="form.temperature" :min="30" :max="45" :step="0.1" class="wfull" placeholder="℃ 可选" />
        </el-form-item>
        <el-form-item label="体重" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :max="300" :step="0.1" class="wfull" placeholder="kg 可选" />
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker
            v-model="form.recordTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="wfull"
            placeholder="默认可留空由系统填当前时间"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="体征说明、用药情况等" />
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
import { createRowIndexGetter } from '../utils/listTable'
import { bloodPressurePair } from '../utils/validators'

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = 10
const rowIndex = createRowIndexGetter(page, size)
const query = reactive({ elderName: '', elderId: undefined })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  elderId: undefined,
  systolicBp: null,
  diastolicBp: null,
  bloodSugar: null,
  heartRate: null,
  temperature: null,
  weight: null,
  recordTime: '',
  remark: ''
})

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
  systolicBp: [{ validator: bloodPressurePair(form), trigger: 'blur' }],
  diastolicBp: [{ validator: bloodPressurePair(form), trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/health-records', {
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
  Object.assign(form, {
    id: null,
    elderId: query.elderId ?? undefined,
    systolicBp: null,
    diastolicBp: null,
    bloodSugar: null,
    heartRate: null,
    temperature: null,
    weight: null,
    recordTime: '',
    remark: ''
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
    ElMessage.warning('请检查表单：关联老人与血压是否合理')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (payload.recordTime && typeof payload.recordTime === 'string' && !payload.recordTime.includes('T')) {
      payload.recordTime = payload.recordTime.replace(' ', 'T')
    }
    if (form.id) await http.put(`/health-records/${form.id}`, payload)
    else await http.post('/health-records', payload)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该条健康记录？此操作不可恢复。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/health-records/${row.id}`)
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
  align-items: center;
  flex-wrap: wrap;
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
</style>
