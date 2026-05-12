<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon elders-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.elderCount }}</div>
          <div class="stat-label">老人总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon alert-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-value warning">{{ stats.alertCount }}</div>
          <div class="stat-label">待处理预警</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon followup-icon">📅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.followupCount }}</div>
          <div class="stat-label">即将随访</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon record-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.recordCount }}</div>
          <div class="stat-label">今日记录</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <el-card class="chart-card">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">健康趋势分析</span>
            <el-select v-model="chartElderId" class="elder-select" placeholder="选择老人">
              <el-option :value="null" label="全部老人统计" />
              <el-option v-for="e in elders" :key="e.id" :value="e.id" :label="e.name" />
            </el-select>
          </div>
        </template>
        <div ref="chartRef" class="chart-container"></div>
      </el-card>
    </div>

    <div class="bottom-section">
      <el-card class="list-card">
        <template #header>
          <span class="card-title">待处理预警</span>
          <el-button link type="primary" @click="goToAlerts">查看全部</el-button>
        </template>
        <div v-if="alerts.length === 0" class="empty-state">暂无待处理预警</div>
        <el-timeline v-else>
          <el-timeline-item
            v-for="alert in alerts"
            :key="alert.id"
            :color="getAlertColor(alert.alertLevel)"
          >
            <template #dot>
              <el-icon><Warning /></el-icon>
            </template>
            <div class="timeline-content">
              <div class="timeline-title">{{ alert.elderName }} - {{ alert.alertType }}</div>
              <div class="timeline-desc">{{ alert.message }}</div>
              <div class="timeline-meta">{{ alert.createdAt }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card class="list-card">
        <template #header>
          <span class="card-title">即将到期随访</span>
          <el-button link type="primary" @click="goToFollowups">查看全部</el-button>
        </template>
        <div v-if="followups.length === 0" class="empty-state">暂无即将到期的随访计划</div>
        <el-table v-else :data="followups" border>
          <el-table-column prop="elderName" label="老人" min-width="100" />
          <el-table-column prop="riskType" label="风险类型" />
          <el-table-column prop="nextFollowupDate" label="下次随访" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import http from '../api/http'

const router = useRouter()
const chartRef = ref(null)
let chartInstance = null

const stats = reactive({
  elderCount: 0,
  alertCount: 0,
  followupCount: 0,
  recordCount: 0
})

const elders = ref([])
const chartElderId = ref(null)
const alerts = ref([])
const followups = ref([])

async function loadStats() {
  try {
    const [elderData, alertData, followupData, recordData] = await Promise.all([
      http.get('/elders', { params: { size: 0 } }),
      http.get('/health-alerts', { params: { status: 0, size: 0 } }),
      http.get('/key-followups', { params: { size: 0 } }),
      http.get('/health-records', { params: { size: 0 } })
    ])
    stats.elderCount = elderData.total || 0
    stats.alertCount = alertData.total || 0
    stats.followupCount = followupData.total || 0
    stats.recordCount = recordData.total || 0
  } catch (err) {
    console.error('Failed to load stats:', err)
  }
}

async function loadElders() {
  try {
    const data = await http.get('/elders', { params: { size: 20 } })
    elders.value = data.records || []
  } catch (err) {
    console.error('Failed to load elders:', err)
  }
}

async function loadAlerts() {
  try {
    const data = await http.get('/health-alerts', { params: { status: 0, size: 5 } })
    alerts.value = data.records || []
  } catch (err) {
    console.error('Failed to load alerts:', err)
  }
}

async function loadFollowups() {
  try {
    const data = await http.get('/key-followups', { params: { size: 5 } })
    followups.value = data.records || []
  } catch (err) {
    console.error('Failed to load followups:', err)
  }
}

async function loadChartData() {
  if (!chartInstance) return
  
  try {
    const params = chartElderId.value 
      ? { elderId: chartElderId.value, size: 30 }
      : { size: 30 }
    const data = await http.get('/health-records', { params })
    
    const records = data.records || []
    const sortedRecords = records.sort((a, b) => new Date(a.recordTime) - new Date(b.recordTime))
    
    const dates = sortedRecords.map(r => {
      const dt = new Date(r.recordTime)
      return `${dt.getMonth() + 1}/${dt.getDate()}`
    })
    const systolicBp = sortedRecords.map(r => r.systolicBp || null)
    const diastolicBp = sortedRecords.map(r => r.diastolicBp || null)
    const bloodSugar = sortedRecords.map(r => r.bloodSugar || null)

    const option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#ebeef5',
        textStyle: { color: '#303133' }
      },
      legend: {
        data: ['收缩压', '舒张压', '血糖'],
        bottom: 10,
        textStyle: { fontSize: 12 }
      },
      grid: {
        left: '10%',
        right: '12%',
        top: '12%',
        bottom: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#ebeef5' } },
        axisLabel: { color: '#909399', fontSize: 12 }
      },
      yAxis: [
        {
          type: 'value',
          name: '血压(mmHg)',
          position: 'left',
          nameGap: 25,
          nameTextStyle: { fontSize: 12, color: '#909399' },
          axisLine: { show: false },
          axisLabel: { color: '#909399', fontSize: 11 },
          splitLine: { lineStyle: { color: '#f2f6fc' } }
        },
        {
          type: 'value',
          name: '血糖(mmol/L)',
          position: 'right',
          nameGap: 25,
          nameTextStyle: { fontSize: 12, color: '#909399' },
          axisLine: { show: false },
          axisLabel: { color: '#909399', fontSize: 11 },
          splitLine: { show: false }
        }
      ],
      series: [
        {
          name: '收缩压',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: systolicBp,
          lineStyle: { color: '#f56c6c', width: 2 },
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '舒张压',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: diastolicBp,
          lineStyle: { color: '#409eff', width: 2 },
          itemStyle: { color: '#409eff' }
        },
        {
          name: '血糖',
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: bloodSugar,
          lineStyle: { color: '#67c23a', width: 2 },
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    
    chartInstance.setOption(option)
  } catch (err) {
    console.error('Failed to load chart data:', err)
  }
}

function initChart() {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    loadChartData()
  }
}

function handleResize() {
  chartInstance?.resize()
}

function getAlertColor(level) {
  const colors = { HIGH: '#f56c6c', MEDIUM: '#e6a23c', LOW: '#67c23a' }
  return colors[level] || '#909399'
}

function goToAlerts() {
  router.push('/health-alerts')
}

function goToFollowups() {
  router.push('/key-followups')
}

watch(chartElderId, () => {
  nextTick(loadChartData)
})

onMounted(() => {
  loadStats()
  loadElders()
  loadAlerts()
  loadFollowups()
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, #1a5f7a 0%, #159895 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-value.warning {
  color: #ffd93d;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.chart-section {
  grid-column: 1 / -1;
}

.chart-card {
  min-height: 420px;
}

.chart-card :deep(.el-card__body) {
  height: 350px;
  padding: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-title {
  font-weight: 600;
  color: #303133;
}

.elder-select {
  width: 160px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 768px) {
  .bottom-section {
    grid-template-columns: 1fr;
  }
}

.list-card {
  max-height: 400px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 0;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-title {
  font-weight: 500;
  color: #303133;
}

.timeline-desc {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.timeline-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>