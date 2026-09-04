<template>
  <div class="home">
    <div class="top-row">
      <!-- 欢迎面板 -->
      <div class="hero">
        <div class="hero-orb hero-orb-1"></div>
        <div class="hero-orb hero-orb-2"></div>
        <div class="hero-rings"></div>
        <div class="hero-content">
          <div class="hero-tag">WORKBENCH</div>
          <div class="hero-hello">{{ greeting }}，{{ user?.name || user?.username }}</div>
          <p>ERP 进销存一体化管理系统 · 7 大子系统 · 采购-销售-库存-财务全流程闭环</p>
          <el-button size="large" class="hero-btn" @click="$router.push('/inventory')">
            进入业务中心<el-icon style="margin-left: 6px"><Right /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 统计 2x2 -->
      <div class="stat-grid">
        <div v-for="s in stats" :key="s.label" class="stat-card">
          <div class="stat-icon" :style="{ background: s.bg, boxShadow: s.glow }">
            <el-icon :size="22"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-row">
      <div class="erp-card chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">近 30 天销售金额</div>
            <div class="chart-sub">SALES TREND</div>
          </div>
          <span class="chart-tag">销售趋势</span>
        </div>
        <div ref="saleChart" class="chart"></div>
      </div>
      <div class="erp-card chart-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">应收账龄分布</div>
            <div class="chart-sub">RECEIVABLE AGING</div>
          </div>
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
  { label: '客户总数', value: 0, icon: 'User', bg: 'linear-gradient(135deg,#2563EB,#3B82F6)', glow: '0 0 20px rgba(59,130,246,.4)' },
  { label: '供应商', value: 0, icon: 'Shop', bg: 'linear-gradient(135deg,#B45309,#F59E0B)', glow: '0 0 20px rgba(245,158,11,.35)' },
  { label: '商品档案', value: 0, icon: 'Goods', bg: 'linear-gradient(135deg,#7C5CE8,#A78BFA)', glow: '0 0 20px rgba(167,139,250,.35)' },
  { label: '库存记录', value: 0, icon: 'Box', bg: 'linear-gradient(135deg,#D95F72,#F58E9F)', glow: '0 0 20px rgba(245,142,159,.35)' }
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

const axisCommon = {
  axisLine: { lineStyle: { color: 'rgba(255,255,255,.08)' } },
  axisTick: { show: false },
  axisLabel: { color: '#64748F' }
}
const tooltipCommon = {
  backgroundColor: 'rgba(20, 31, 26, .95)',
  borderColor: 'rgba(59,130,246,.25)',
  borderWidth: 1,
  textStyle: { color: '#E5EDF9', fontSize: 12 },
  extraCssText: 'border-radius:10px;box-shadow:0 10px 30px rgba(0,0,0,.5);'
}

function initCharts() {
  ec1 = echarts.init(saleChart.value)
  ec2 = echarts.init(agingChart.value)
  ec1.setOption({
    tooltip: { trigger: 'axis', ...tooltipCommon, axisPointer: { type: 'line', lineStyle: { color: 'rgba(59,130,246,.4)' } } },
    grid: { left: 50, right: 24, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: [], ...axisCommon },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,.05)' } },
      axisLabel: { color: '#64748F' }
    },
    series: [{
      type: 'line', data: [], smooth: true,
      symbol: 'circle', symbolSize: 7, showSymbol: false,
      lineStyle: { width: 3, color: '#3B82F6', shadowColor: 'rgba(59,130,246,.5)', shadowBlur: 12 },
      itemStyle: { color: '#3B82F6', borderColor: '#0A0E17', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, .30)' },
          { offset: 1, color: 'rgba(59, 130, 246, .01)' }
        ])
      }
    }]
  })
  ec2.setOption({
    tooltip: { trigger: 'item', ...tooltipCommon },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#8A9BB5' } },
    color: ['#3B82F6', '#F59E0B', '#22D3EE', '#F58E9F'],
    series: [{
      type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'],
      itemStyle: { borderRadius: 8, borderColor: '#111827', borderWidth: 3 },
      label: { formatter: '{b}\n{c}', color: '#8A9BB5', fontSize: 12 },
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

/* ===== 顶部：欢迎面板 + 统计 ===== */
.top-row {
  display: grid;
  grid-template-columns: 5fr 4fr;
  gap: 16px;
  margin-bottom: 16px;
}

/* 欢迎面板 */
.hero {
  position: relative;
  padding: 34px 36px;
  border-radius: 20px;
  background:
    radial-gradient(500px 260px at 90% 0%, rgba(59, 130, 246, .16), transparent 60%),
    radial-gradient(400px 220px at 0% 100%, rgba(245, 158, 11, .10), transparent 55%),
    linear-gradient(150deg, #121C30 0%, #0D1424 100%);
  border: 1px solid rgba(59, 130, 246, .16);
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, .4);
  display: flex;
  align-items: center;
}
.hero-orb { position: absolute; border-radius: 50%; filter: blur(46px); pointer-events: none; }
.hero-orb-1 {
  width: 220px; height: 220px; top: -90px; right: 12%;
  background: radial-gradient(circle, rgba(59, 130, 246, .4), transparent 65%);
}
.hero-orb-2 {
  width: 170px; height: 170px; bottom: -90px; left: 10%;
  background: radial-gradient(circle, rgba(245, 158, 11, .3), transparent 65%);
}
.hero-rings {
  position: absolute; right: -70px; top: 50%; transform: translateY(-50%);
  width: 260px; height: 260px; border-radius: 50%;
  border: 1px solid rgba(59, 130, 246, .18);
  box-shadow: 0 0 0 40px rgba(59, 130, 246, .04), 0 0 0 90px rgba(59, 130, 246, .02);
  pointer-events: none;
}
.hero-content { position: relative; }
.hero-tag {
  display: inline-block;
  font-size: 10px; font-weight: 700; letter-spacing: 3px;
  color: #3B82F6;
  border: 1px solid rgba(59, 130, 246, .35);
  background: rgba(59, 130, 246, .08);
  padding: 4px 12px; border-radius: 999px;
  margin-bottom: 16px;
}
.hero-hello { font-size: 26px; font-weight: 800; color: #F0F5FC; letter-spacing: 1px; margin-bottom: 12px; }
.hero-content p { color: #7E8FA8; font-size: 13px; letter-spacing: .5px; margin-bottom: 24px; }
.hero-btn {
  background: var(--erp-primary-grad) !important;
  border: none !important;
  color: #081226 !important;
  font-weight: 700;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(59, 130, 246, .35);
  transition: all .25s ease;
}
.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(59, 130, 246, .5);
}

/* 统计 2x2 */
.stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(170deg, var(--erp-panel-2) 0%, var(--erp-panel) 100%);
  border-radius: 18px;
  border: 1px solid var(--erp-border);
  box-shadow: var(--erp-shadow);
  padding: 20px;
  transition: all .25s ease;
}
.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(59, 130, 246, .22);
  box-shadow: var(--erp-shadow-hover);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 14px; flex-shrink: 0;
  color: #081226; display: flex; align-items: center; justify-content: center;
}
.stat-num { font-size: 25px; font-weight: 800; color: #F0F5FC; line-height: 1.1; }
.stat-label { color: #64748F; font-size: 12.5px; margin-top: 4px; letter-spacing: .5px; }

/* ===== 图表 ===== */
.chart-row { display: grid; grid-template-columns: 3fr 2fr; gap: 16px; }
.chart-card .chart { height: 300px; }
.chart-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 10px; }
.chart-title { font-weight: 700; color: #EDF2FA; font-size: 15px; letter-spacing: .5px; }
.chart-sub { font-size: 10px; color: #46536B; letter-spacing: 2px; margin-top: 4px; }
.chart-tag {
  font-size: 11px; color: #3B82F6;
  background: rgba(59, 130, 246, .10);
  border: 1px solid rgba(59, 130, 246, .25);
  padding: 4px 12px; border-radius: 999px; font-weight: 600;
}
.chart-tag.gold {
  color: #F59E0B;
  background: rgba(245, 158, 11, .08);
  border-color: rgba(245, 158, 11, .25);
}
</style>
