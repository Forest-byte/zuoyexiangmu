<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="hero">
      <div class="hero-orb hero-orb-1"></div>
      <div class="hero-orb hero-orb-2"></div>
      <div class="hero-content">
        <div class="hero-hello">{{ greeting }}，{{ user?.name || user?.username }}</div>
        <p>ERP 进销存一体化管理系统 · 7 大子系统 · 采购-销售-库存-财务全流程闭环</p>
      </div>
      <el-button size="large" class="hero-btn" @click="$router.push('/inventory')">
        进入业务中心<el-icon style="margin-left: 6px"><Right /></el-icon>
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-icon" :style="{ background: s.bg }">
          <el-icon :size="24"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
        <div class="stat-deco" :style="{ background: s.bg }"></div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-row">
      <div class="erp-card chart-card">
        <div class="chart-head">
          <span class="chart-title">近 30 天销售金额</span>
          <span class="chart-tag">销售趋势</span>
        </div>
        <div ref="saleChart" class="chart"></div>
      </div>
      <div class="erp-card chart-card">
        <div class="chart-head">
          <span class="chart-title">应收账龄分布</span>
          <span class="chart-tag gold">财务健康</span>
        </div>
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
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const stats = ref([
  { label: '客户总数', value: 0, icon: 'User', bg: 'linear-gradient(135deg,#3B6FF0,#6C93F5)' },
  { label: '供应商', value: 0, icon: 'Shop', bg: 'linear-gradient(135deg,#16A34A,#34C26E)' },
  { label: '商品档案', value: 0, icon: 'Goods', bg: 'linear-gradient(135deg,#E8B04B,#F2C979)' },
  { label: '库存记录', value: 0, icon: 'Box', bg: 'linear-gradient(135deg,#E5604F,#EE8A63)' }
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
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 38, 74, .92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      axisPointer: { type: 'line', lineStyle: { color: '#B9C6E2' } }
    },
    grid: { left: 50, right: 24, top: 30, bottom: 30 },
    xAxis: {
      type: 'category', data: [],
      axisLine: { lineStyle: { color: '#E5EAF3' } },
      axisTick: { show: false },
      axisLabel: { color: '#8A99B5' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F0F3F9' } },
      axisLabel: { color: '#8A99B5' }
    },
    series: [{
      type: 'line', data: [], smooth: true,
      symbol: 'circle', symbolSize: 7, showSymbol: false,
      lineStyle: { width: 3, color: '#3B6FF0' },
      itemStyle: { color: '#3B6FF0', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 111, 240, .28)' },
          { offset: 1, color: 'rgba(59, 111, 240, .02)' }
        ])
      }
    }]
  })
  ec2.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(22, 38, 74, .92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 }
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#54647E' } },
    color: ['#3B6FF0', '#E8B04B', '#EE8A63', '#E5604F'],
    series: [{
      type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: { formatter: '{b}\n{c}', color: '#54647E', fontSize: 12 },
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
.home { animation: home-in .35s ease; }
@keyframes home-in {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 欢迎横幅 ===== */
.hero {
  position: relative;
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
  padding: 30px 32px;
  border-radius: var(--erp-radius-lg);
  background:
    radial-gradient(600px 200px at 85% 20%, rgba(232, 176, 75, .25), transparent 60%),
    linear-gradient(120deg, #16264A 0%, #1E3A6F 55%, #2B4E8F 100%);
  color: #fff;
  overflow: hidden;
  box-shadow: 0 14px 36px rgba(22, 38, 74, .30);
}
.hero-orb { position: absolute; border-radius: 50%; filter: blur(50px); pointer-events: none; }
.hero-orb-1 {
  width: 260px; height: 260px; top: -110px; right: 18%;
  background: radial-gradient(circle, rgba(91, 140, 255, .5), transparent 65%);
}
.hero-orb-2 {
  width: 200px; height: 200px; bottom: -120px; left: 8%;
  background: radial-gradient(circle, rgba(232, 176, 75, .4), transparent 65%);
}
.hero-content { position: relative; }
.hero-hello { font-size: 24px; font-weight: 700; margin-bottom: 10px; letter-spacing: .5px; }
.hero-content p { color: #A9BCDC; font-size: 13px; letter-spacing: .3px; }
.hero-btn {
  position: relative;
  background: rgba(255, 255, 255, .14) !important;
  border: 1px solid rgba(255, 255, 255, .35) !important;
  color: #fff !important;
  font-weight: 600;
  border-radius: 10px;
  backdrop-filter: blur(6px);
  transition: all .25s ease;
}
.hero-btn:hover {
  background: rgba(255, 255, 255, .24) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, .25);
}

/* ===== 统计卡片 ===== */
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-bottom: 20px; }
.stat-card {
  position: relative;
  display: flex; align-items: center; gap: 16px;
  background: #fff;
  border-radius: var(--erp-radius-lg);
  border: 1px solid rgba(229, 234, 243, .8);
  box-shadow: var(--erp-shadow);
  padding: 22px;
  overflow: hidden;
  transition: all .25s ease;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--erp-shadow-hover);
}
.stat-icon {
  width: 54px; height: 54px; border-radius: 15px; flex-shrink: 0;
  color: #fff; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 16px rgba(23, 43, 99, .22), inset 0 1px 0 rgba(255, 255, 255, .3);
}
.stat-num { font-size: 28px; font-weight: 800; color: var(--erp-navy); line-height: 1.1; }
.stat-label { color: #7A8CA8; font-size: 13px; margin-top: 4px; }
.stat-deco {
  position: absolute; right: -28px; bottom: -28px;
  width: 92px; height: 92px; border-radius: 50%;
  opacity: .10;
  transition: opacity .25s ease;
}
.stat-card:hover .stat-deco { opacity: .18; }

/* ===== 图表 ===== */
.chart-row { display: grid; grid-template-columns: 3fr 2fr; gap: 18px; }
.chart-card .chart { height: 300px; }
.chart-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.chart-title { font-weight: 700; color: var(--erp-navy); font-size: 15px; }
.chart-tag {
  font-size: 11px; color: var(--erp-primary);
  background: var(--el-color-primary-light-9);
  padding: 3px 10px; border-radius: 20px; font-weight: 600;
}
.chart-tag.gold { color: #B07E1E; background: #FBF3E2; }
</style>
