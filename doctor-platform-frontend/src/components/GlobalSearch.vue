<template>
  <div class="global-search" ref="searchRef">
    <el-input
      v-model="keyword"
      :placeholder="placeholder"
      class="search-input"
      size="small"
      clearable
      @focus="handleFocus"
      @input="handleInput"
      @clear="handleClear"
    >
      <template #prefix>
        <el-icon :size="18" class="search-icon"><Search /></el-icon>
      </template>
    </el-input>
    <Teleport to="body">
      <div v-if="showDropdown && filteredResults.length > 0" class="search-dropdown" :style="dropdownStyle">
        <div class="dropdown-header">搜索结果（{{ filteredResults.length }}条）</div>
        <el-scrollbar class="dropdown-list">
          <div
            v-for="item in filteredResults"
            :key="item.id"
            class="dropdown-item"
            @click="handleSelect(item)"
          >
            <div class="item-name">{{ item.name }}</div>
            <div class="item-info">
              <span v-if="item.phone" class="info-item">📱 {{ item.phone }}</span>
              <span v-if="item.idCard" class="info-item">🆔 {{ item.idCard }}</span>
            </div>
          </div>
        </el-scrollbar>
        <div v-if="filteredResults.length >= maxResults" class="dropdown-footer">仅显示前{{ maxResults }}条...</div>
      </div>
    </Teleport>
    <Teleport to="body">
      <div v-if="showDropdown" class="search-mask" @click="handleBlur" />
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import http from '../api/http'

const props = defineProps({
  placeholder: { type: String, default: '搜索老人姓名、电话、身份证...' }
})

const router = useRouter()
const searchRef = ref(null)
const keyword = ref('')
const showDropdown = ref(false)
const allResults = ref([])
const maxResults = 10

const filteredResults = computed(() => {
  if (!keyword.value.trim()) return []
  const kw = keyword.value.toLowerCase()
  return allResults.value
    .filter(item => 
      item.name.toLowerCase().includes(kw) ||
      item.phone?.includes(kw) ||
      item.idCard?.includes(kw)
    )
    .slice(0, maxResults)
})

const dropdownStyle = computed(() => {
  if (!searchRef.value) return {}
  const rect = searchRef.value.getBoundingClientRect()
  return {
    left: `${rect.left}px`,
    top: `${rect.bottom + 4}px`,
    width: `${rect.width}px`
  }
})

let searchTimer = null

async function handleInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (keyword.value.trim()) {
      try {
        const data = await http.get('/elders', {
          params: { name: keyword.value, size: 50 }
        })
        allResults.value = data.records || []
      } catch {
        allResults.value = []
      }
    }
  }, 300)
}

function handleFocus() {
  showDropdown.value = true
}

function handleBlur() {
  setTimeout(() => {
    showDropdown.value = false
  }, 200)
}

function handleClear() {
  allResults.value = []
  showDropdown.value = false
}

function handleSelect(item) {
  keyword.value = ''
  showDropdown.value = false
  router.push('/elders')
}

function handleKeydown(e) {
  if (e.key === 'Escape') {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (searchTimer) clearTimeout(searchTimer)
})

watch(keyword, (val) => {
  if (!val.trim()) {
    allResults.value = []
  }
})
</script>

<style scoped>
.global-search {
  position: relative;
  flex: 1;
  max-width: 400px;
  min-width: 200px;
}

.search-input {
  border-radius: 20px;
}

.search-icon {
  color: #909399;
}

.search-dropdown {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dropdown-header {
  padding: 8px 12px;
  font-size: 13px;
  color: #909399;
  border-bottom: 1px solid #ebeef5;
}

.dropdown-list {
  max-height: 300px;
}

.dropdown-item {
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: #f5f7fa;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.item-info {
  margin-top: 4px;
  display: flex;
  gap: 12px;
}

.info-item {
  font-size: 12px;
  color: #909399;
}

.dropdown-footer {
  padding: 8px 12px;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

.search-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
}
</style>