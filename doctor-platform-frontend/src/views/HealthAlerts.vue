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
      <el-select v-model="query.status" clearable placeholder="处理状态" style="width: 130px">
        <el-option :value="0" label="待处理" />
        <el-option :value="1" label="已处理" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="success" @click="openEdit()">新增预警</el-button>
    </div>
    <el-table
      :data="table.records"
      border
      stripe
      v-loading="loading"
      empty-text="暂无预警记录"
    >
      <el-table-column label="序号" type="index" width="64" />
      <el-table-column prop="elderName" label="老人姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="alertType" label="类型" width="120" />
      <el-table-column prop="alertLevel" label="级别" width="90" />
      <el-table-column prop="message" label="内容" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '已处理' : '待处理' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="success" @click="handle(row)">标记处理</el-button>
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
      :title="form.id ? '编辑预警' : '新增预警'"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联老人" prop="elderId">
          <ElderSelect v-model="form.elderId" />
        </el-form-item>
        <el-form-item label="预警类型" prop="alertType">
          <el-input v-model="form.alertType" placeholder="如：血压异常、跌倒风险" maxlength="50" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="级别" prop="alertLevel">
          <el-select v-model="form.alertLevel" placeholder="请选择" class="wfull" clearable>
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="message">
          <el-input v-model="form.message" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="预警详情、触发条件等" />
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
const query = reactive({ elderName: '', elderId: undefined, status: null })
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, elderId: undefined, alertType: '', alertLevel: '', message: '' })

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
  alertType: required('请填写预警类型'),
  alertLevel: required('请选择级别', 'change'),
  message: required('请填写说明')
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/health-alerts', {
      params: {
        page: page.value,
        size,
        elderName: query.elderName || undefined,
        elderId: query.elderId || undefined,
        status: query.status
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
    alertType: '',
    alertLevel: '',
    message: ''
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
    ElMessage.warning('请完善预警信息')
    return
  }
  saving.value = true
  try {
    if (form.id) await http.put(`/health-alerts/${form.id}`, form)
    else await http.post('/health-alerts', { ...form, status: 0 })
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handle(row) {
  let remark = ''
  try {
    const { value } = await ElMessageBox.prompt('请填写处理说明（将写入处理备注）', '标记为已处理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '如：已电话随访、已通知家属',
      inputValue: '已随访处理',
      inputValidator: (v) => {
        if (v == null || !String(v).trim()) return '处理说明不能为空'
        return true
      }
    })
    remark = value?.trim() || ''
  } catch {
    return
  }
  await http.put(`/health-alerts/${row.id}/handle`, { handleRemark: remark })
  ElMessage.success('已标记处理')
  load()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该条预警？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/health-alerts/${row.id}`)
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
