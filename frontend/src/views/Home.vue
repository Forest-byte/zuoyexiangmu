<template>
  <div class="home">
    <div class="hero erp-card">
      <div>
        <h2>欢迎回来，{{ user?.name || user?.username }}</h2>
        <p>ERP 进销存一体化管理系统 · 7 大子系统 · 采购-销售-库存-财务全流程闭环</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/inventory')">进入业务中心</el-button>
    </div>

    <div class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card erp-card">
        <div class="stat-icon" :style="{ background: s.bg }">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div>
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="chart-row">
      <div class="erp-card chart-card">
        <div class="chart-title">近 30 天销售金额</div>
        <div ref="saleChart" class="chart"></div>
      </div>
      <div class="erp-card chart-card">
        <div class="chart-title">应收账龄分布</div>
        <div ref="agingChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import request from '@/api/request'

const store = useUserStore()
const user = computed(() => store.user)
const stats = ref([
  { label: '客户总数', value: 0, icon: 'User', bg: 'linear-gradient(135deg,#2E6BE6,#5A8DF0)' },
  { label: '供应商', value: 0, icon: 'Shop', bg: 'linear-gradient(135deg,#18A058,#2BB673)' },
  { label: '商品档案', value: 0, icon: 'Goods', bg: 'linear-gradient(135deg,#E8B04B,#F0C36D)' },
  { label: '库存记录', value: 0, icon: 'Box', bg: 'linear-gradient(135deg,#E06C4F,#EE8A63)' }
])
const saleChart = ref(null)
const agingChart = ref(null)
let ec1 = null, ec2 = null

onMounted(async () => {
  try {
    const [cus, sup, goods, stocks] = await Promise.all([
      request.get('/crm/customers/all'),
      request.get('/crm/suppliers/all'),
      request.get('/goods/all'),
      request.get('/wms/stocks?page=1&pageSize=1')
    ])
    stats.value[0].value = cus.length
    stats.value[1].value = sup.length
    stats.value[2].value = goods.length
    stats.value[3].value = stocks.total
  } catch (e) { /* 权限不足时保持 0 */ }
  initCharts()
})

function initCharts() {
  ec1 = echarts.init(saleChart.value)
  ec2 = echarts.init(agingChart.value)
  ec1.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar', data: [],
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4A82EE' }, { offset: 1, color: '#2E6BE6' }]), borderRadius: [6, 6, 0, 0] }
    }]
  })
  ec2.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['42%', '68%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}: {c}' },
      data: []
    }]
  })
  loadReport()
}

async function loadReport() {
  try {
    const end = new Date()
    const start = new Date(end.getTime() - 29 * 86400000)
    const fmt = d => d.toISOString().slice(0, 10)
    const sale = await request.get('/report/sale', { params: { startDate: fmt(start), endDate: fmt(end) } })
    if (Array.isArray(sale) && sale.length) {
      ec1.setOption({ xAxis: { data: sale.map(r => r.dimName || r.goodsName) }, series: [{ data: sale.map(r => Number(r.amount).toFixed(2)) }] })
    }
  } catch (e) { /* 忽略 */ }
  try {
    const aging = await request.get('/report/aging')
    const arc = aging.arc || {}
    const data = [
      { name: '30天内', value: Number(arc.age30 || 0) },
      { name: '31-60天', value: Number(arc.age60 || 0) },
      { name: '61-90天', value: Number(arc.age90 || 0) },
      { name: '90天以上', value: Number(arc.ageOver || 0) }
    ]
    if (data.some(d => d.value > 0)) ec2.setOption({ series: [{ data }] })
  } catch (e) { /* 忽略 */ }
}

function onResize() { ec1 && ec1.resize(); ec2 && ec2.resize() }
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  ec1 && ec1.dispose()
  ec2 && ec2.dispose()
})
window.addEventListener('resize', onResize)
</script>

<style scoped>
.hero { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.hero h2 { font-size: 20px; color: #16345E; margin-bottom: 8px; }
.hero p { color: #7A8CA8; }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 18px; }
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 14px; color: #fff; display: flex; align-items: center; justify-content: center; }
.stat-num { font-size: 26px; font-weight: 700; color: #16345E; }
.stat-label { color: #7A8CA8; font-size: 13px; }
.chart-row { display: grid; grid-template-columns: 3fr 2fr; gap: 16px; }
.chart-card .chart { height: 300px; }
.chart-title { font-weight: 600; color: #16345E; margin-bottom: 8px; }
</style>
