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
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openEdit()">新增随访计划</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无随访计划"
    >
      <el-table-column label="序号" type="index" width="64" />
      <el-table-column prop="elderName" label="老人姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="riskType" label="风险类型" width="120" />
      <el-table-column prop="followupCycleDays" label="周期(天)" width="100" />
      <el-table-column prop="nextFollowupDate" label="下次随访" width="120" />
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
      :title="form.id ? '编辑随访计划' : '新增随访计划'"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="关联老人" prop="elderId">
          <ElderSelect v-model="form.elderId" />
        </el-form-item>
        <el-form-item label="风险类型" prop="riskType">
          <el-input v-model="form.riskType" placeholder="如：高血压、糖尿病、术后随访" maxlength="50" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="随访周期(天)" prop="followupCycleDays">
          <el-input-number v-model="form.followupCycleDays" :min="1" :max="365" :step="1" class="wfull" />
        </el-form-item>
        <el-form-item label="下次随访日期" prop="nextFollowupDate">
          <el-date-picker v-model="form.nextFollowupDate" type="date" value-format="YYYY-MM-DD" class="wfull" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="随访要点、注意事项" />
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
import { required } from '../utils/validators'

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
  riskType: '',
  followupCycleDays: 30,
  nextFollowupDate: '',
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
  riskType: required('请填写风险类型'),
  followupCycleDays: [
    { required: true, message: '请填写随访周期', trigger: 'change' },
    { type: 'number', min: 1, max: 365, message: '周期建议在 1～365 天', trigger: 'change' }
  ],
  nextFollowupDate: required('请选择下次随访日期', 'change')
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/key-followups', {
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
    riskType: '',
    followupCycleDays: 30,
    nextFollowupDate: '',
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
    ElMessage.warning('请完善随访计划信息')
    return
  }
  saving.value = true
  try {
    if (form.id) await http.put(`/key-followups/${form.id}`, form)
    else await http.post('/key-followups', form)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该随访计划？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/key-followups/${row.id}`)
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
</style>
