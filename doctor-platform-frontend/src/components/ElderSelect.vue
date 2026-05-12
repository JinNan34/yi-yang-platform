<template>
  <el-select
    :model-value="modelValue"
    class="elder-select"
    filterable
    remote
    clearable
    reserve-keyword
    :disabled="disabled"
    :loading="loading"
    placeholder="输入姓名搜索并选择老人"
    :remote-method="remoteSearch"
    @update:model-value="onChange"
  >
    <el-option v-for="e in options" :key="e.id" :label="formatLabel(e)" :value="e.id" />
  </el-select>
</template>

<script setup>
import { ref, watch } from 'vue'
import http from '../api/http'

const props = defineProps({
  modelValue: { type: [Number, String], default: undefined },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const options = ref([])
const loading = ref(false)

function formatLabel(e) {
  return e.name || '未命名'
}

async function remoteSearch(query) {
  const q = (query || '').trim()
  if (!q) {
    options.value = []
    return
  }
  loading.value = true
  try {
    const data = await http.get('/elders', { params: { page: 1, size: 30, name: q } })
    options.value = data.records || []
  } finally {
    loading.value = false
  }
}

async function ensureCurrentInOptions() {
  const id = props.modelValue
  if (id == null || id === '') return
  const numId = Number(id)
  if (Number.isNaN(numId)) return
  try {
    const e = await http.get(`/elders/${numId}`)
    if (e && !options.value.some((o) => o.id === e.id)) {
      options.value = [e, ...options.value]
    }
  } catch {
    /* 忽略：老人已删除等 */
  }
}

function onChange(val) {
  emit('update:modelValue', val === '' || val === undefined ? undefined : val)
}

watch(
  () => props.modelValue,
  () => {
    ensureCurrentInOptions()
  },
  { immediate: true }
)
</script>

<style scoped>
.elder-select {
  width: 100%;
}
</style>
