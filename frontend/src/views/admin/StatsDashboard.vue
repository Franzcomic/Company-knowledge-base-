<script setup lang="ts">
// 统计看板（阶段5 P2，仅 SYS_ADMIN）：数据卡片 + ECharts 图表 + 时间范围筛选
// 接口契约：GET /api/stats/{overview,hot-questions,hot-kb,departments,unsolved}
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getStatsOverview,
  getHotQuestions,
  getHotKb,
  getDepartmentStats,
  getUnsolvedQuestions,
  type StatsRange,
  type StatsOverview,
  type HotQuestion,
  type HotKb,
  type DepartmentStat
} from '@/api/stats'

echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const loading = ref(false)
const range = ref<StatsRange>('7d')
const rangeOptions: { value: StatsRange; label: string }[] = [
  { value: '7d', label: '近 7 天' },
  { value: '30d', label: '近 30 天' },
  { value: '90d', label: '近 90 天' },
  { value: 'all', label: '全部' }
]

const overview = ref<StatsOverview | null>(null)
const hotQuestions = ref<HotQuestion[]>([])
const hotKb = ref<HotKb[]>([])
const departments = ref<DepartmentStat[]>([])
const unsolved = ref<string[]>([])

// 图表容器
const trendRef = ref<HTMLDivElement>()
const hotQRef = ref<HTMLDivElement>()
const hotKbRef = ref<HTMLDivElement>()
const deptRef = ref<HTMLDivElement>()
const charts: echarts.ECharts[] = []

function fmtPct(v?: number | null) {
  return v == null ? '—' : `${(v * 100).toFixed(1)}%`
}
function fmtMs(ms?: number | null) {
  if (ms == null) return '—'
  return ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${Math.round(ms)}ms`
}

/** 初始化四个图表实例 */
function initCharts() {
  const mounts = [trendRef.value, hotQRef.value, hotKbRef.value, deptRef.value]
  for (const el of mounts) {
    if (el) charts.push(echarts.init(el))
  }
}

function renderCharts() {
  const ov = overview.value
  // 1) 每日问题趋势（折线）
  const trendDates = ov?.trend.map((t) => t.date) ?? []
  const trendTotals = ov?.trend.map((t) => t.total) ?? []
  charts[0]?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', boundaryGap: false, data: trendDates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '问题数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        areaStyle: { opacity: 0.15 },
        data: trendTotals
      }
    ]
  })

  // 2) 热门问题（横向条形）
  charts[1]?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 20, right: 40, top: 10, bottom: 10, containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      inverse: true,
      data: hotQuestions.value.map((h) => (h.question.length > 14 ? h.question.slice(0, 14) + '…' : h.question))
    },
    series: [
      {
        name: '提问次数',
        type: 'bar',
        barMaxWidth: 18,
        label: { show: true, position: 'right' },
        data: hotQuestions.value.map((h) => h.count)
      }
    ]
  })

  // 3) 热门知识库（横向条形）
  charts[2]?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 20, right: 40, top: 10, bottom: 10, containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      inverse: true,
      data: hotKb.value.map((k) => (k.kbName.length > 12 ? k.kbName.slice(0, 12) + '…' : k.kbName))
    },
    series: [
      {
        name: '被引用次数',
        type: 'bar',
        barMaxWidth: 18,
        itemStyle: { color: '#67c23a' },
        label: { show: true, position: 'right' },
        data: hotKb.value.map((k) => k.count)
      }
    ]
  })

  // 4) 部门分布（环形饼图）
  charts[3]?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        name: '部门分布',
        type: 'pie',
        radius: ['38%', '62%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b} {c}' },
        data: departments.value.map((d) => ({ name: d.deptName, value: d.count }))
      }
    ]
  })
}

function handleResize() {
  for (const c of charts) c?.resize()
}

async function fetchAll() {
  loading.value = true
  try {
    const [ov, hq, hk, dept, uns] = await Promise.all([
      getStatsOverview(range.value),
      getHotQuestions(range.value),
      getHotKb(range.value),
      getDepartmentStats(range.value),
      getUnsolvedQuestions(range.value)
    ])
    overview.value = ov
    hotQuestions.value = hq
    hotKb.value = hk
    departments.value = dept
    unsolved.value = uns
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  initCharts()
  await fetchAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  for (const c of charts) c.dispose()
})
</script>

<template>
  <div v-loading="loading" class="stats-page">
    <el-card shadow="never" class="stats-toolbar">
      <div class="toolbar-inner">
        <span class="page-title">统计看板</span>
        <div class="toolbar-right">
          <el-tag type="info" size="small" effect="plain">仅系统管理员可见</el-tag>
          <el-radio-group v-model="range" size="small" @change="fetchAll">
            <el-radio-button v-for="opt in rangeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 指标卡片 -->
    <el-row :gutter="12" class="stat-cards">
      <el-col :span="4" v-for="card in [
        { label: '问题总数', value: String(overview?.total ?? 0), color: '#409eff' },
        { label: '已回答', value: String(overview?.answered ?? 0), color: '#67c23a' },
        { label: '拒答', value: String(overview?.notAnswered ?? 0), color: '#e6a23c' },
        { label: '命中率', value: fmtPct(overview?.hitRate), color: '#909399' },
        { label: '平均响应', value: fmtMs(overview?.avgResponseMs), color: '#f56c6c' },
        { label: '满意率', value: fmtPct(overview?.satisfaction), color: '#6f7ad3' }
      ]" :key="card.label">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <el-card shadow="never" class="chart-card">
      <template #header>每日问题趋势</template>
      <div ref="trendRef" class="chart chart-trend" />
    </el-card>

    <!-- 热门问题 / 热门知识库 -->
    <el-row :gutter="12" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>热门问题 TOP{{ hotQuestions.length || 10 }}</template>
          <div ref="hotQRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>热门知识库 TOP{{ hotKb.length || 10 }}</template>
          <div ref="hotKbRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 部门分布 / 未解决问题 -->
    <el-row :gutter="12" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>部门分布</template>
          <div ref="deptRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>未解决问题（拒答 TOP{{ unsolved.length || 10 }}）</template>
          <el-table :data="unsolved.map((q, i) => ({ index: i + 1, question: q }))" size="small">
            <el-table-column prop="index" label="#" width="50" align="center" />
            <el-table-column prop="question" label="问题原文" show-overflow-tooltip />
            <template #empty>
              <el-empty description="暂无未解决问题" :image-size="60" />
            </template>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stats-toolbar :deep(.el-card__body) {
  padding: 12px 16px;
}
.toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-weight: 600;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-cards {
  margin-bottom: 0;
}
.stat-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 14px 16px;
  text-align: center;
}
.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}
.chart-row {
  margin-bottom: 0;
}
.chart-card :deep(.el-card__body) {
  padding: 8px;
}
.chart {
  width: 100%;
  height: 300px;
}
.chart-trend {
  height: 260px;
}
</style>
