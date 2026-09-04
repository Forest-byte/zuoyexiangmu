<template>
  <div>
    <div class="page-title">业务报表</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 采购汇总 -->
        <el-tab-pane label="采购汇总" name="purchase">
          <div class="toolbar">
            <div><el-date-picker v-model="purchaseR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadPurchase">查询</el-button></div>
            <el-button @click="downloadCsv(purchaseRows, '采购汇总')">导出 CSV</el-button>
          </div>
          <el-table :data="purchaseRows" border>
            <el-table-column prop="goodsName" label="商品" min-width="160" />
            <el-table-column prop="dimName" label="供应商" min-width="140" />
            <el-table-column prop="qty" label="采购数量" width="110" align="right" />
            <el-table-column prop="amount" label="采购金额" width="130" align="right"><template #default="{ row }"><span class="money">{{ row.amount }}</span></template></el-table-column>
            <el-table-column prop="avgPrice" label="均价" width="100" align="right" />
          </el-table>
        </el-tab-pane>

        <!-- 销售汇总 -->
        <el-tab-pane label="销售汇总" name="sale">
          <div class="toolbar">
            <div><el-date-picker v-model="saleR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadSale">查询</el-button></div>
            <el-button @click="downloadCsv(saleRows, '销售汇总')">导出 CSV</el-button>
          </div>
          <el-table :data="saleRows" border>
            <el-table-column prop="goodsName" label="商品" min-width="160" />
            <el-table-column prop="dimName" label="客户" min-width="140" />
            <el-table-column prop="qty" label="销售数量" width="110" align="right" />
            <el-table-column prop="amount" label="销售金额" width="130" align="right"><template #default="{ row }"><span class="money">{{ row.amount }}</span></template></el-table-column>
            <el-table-column prop="discount" label="折扣" width="100" align="right" />
          </el-table>
        </el-tab-pane>

        <!-- 利润报表 -->
        <el-tab-pane label="利润报表" name="profit">
          <div class="toolbar">
            <div><el-date-picker v-model="profitR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadProfit">查询</el-button></div>
            <el-button @click="downloadCsv(profitRows, '利润报表')">导出 CSV</el-button>
          </div>
          <el-table :data="profitRows" border>
            <el-table-column prop="goodsCode" label="编码" width="100" />
            <el-table-column prop="goodsName" label="商品" min-width="160" />
            <el-table-column prop="qty" label="销量" width="90" align="right" />
            <el-table-column prop="saleAmount" label="销售额" width="120" align="right" />
            <el-table-column prop="costAmount" label="成本" width="120" align="right" />
            <el-table-column prop="profit" label="毛利" width="120" align="right"><template #default="{ row }"><span class="money">{{ row.profit }}</span></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 应收账龄 -->
        <el-tab-pane label="应收账龄" name="aging">
          <div class="toolbar"><el-button type="primary" @click="loadAging">刷新</el-button></div>
          <div ref="agingRef" style="height: 300px"></div>
          <el-table :data="agingList" border style="margin-top: 12px">
            <el-table-column prop="label" label="账龄区间" min-width="140" />
            <el-table-column prop="amount" label="金额" width="140" align="right"><template #default="{ row }"><span class="money">{{ row.amount }}</span></template></el-table-column>
            <el-table-column prop="ratio" label="占比" width="100" align="right" />
          </el-table>
        </el-tab-pane>

        <!-- 客户贡献 -->
        <el-tab-pane label="客户贡献" name="customer">
          <div class="toolbar"><el-date-picker v-model="cR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadCustomer">查询</el-button></div>
          <el-table :data="cRows" border>
            <el-table-column prop="name" label="客户" min-width="160" />
            <el-table-column prop="orderCount" label="订单数" width="90" align="right" />
            <el-table-column prop="amount" label="销售额" width="130" align="right" />
            <el-table-column prop="profit" label="贡献毛利" width="130" align="right"><template #default="{ row }"><span class="money">{{ row.profit }}</span></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 供应商贡献 -->
        <el-tab-pane label="供应商贡献" name="supplier">
          <div class="toolbar"><el-date-picker v-model="sR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadSupplier">查询</el-button></div>
          <el-table :data="sRows" border>
            <el-table-column prop="name" label="供应商" min-width="160" />
            <el-table-column prop="orderCount" label="采购单数" width="100" align="right" />
            <el-table-column prop="amount" label="采购金额" width="130" align="right" />
          </el-table>
        </el-tab-pane>

        <!-- 库存预警 -->
        <el-tab-pane label="库存预警" name="lowstock">
          <div class="toolbar">
            <el-select v-model="lowWarehouseId" clearable placeholder="仓库" style="width: 160px"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
            <el-button type="primary" style="margin-left: 8px" @click="loadLowStock">查询</el-button>
          </div>
          <el-table :data="lowRows" border>
            <el-table-column prop="warehouseName" label="仓库" width="130" />
            <el-table-column prop="goodsCode" label="编码" width="100" />
            <el-table-column prop="goodsName" label="商品" min-width="160" />
            <el-table-column prop="quantity" label="当前库存" width="110" align="right" />
            <el-table-column prop="lowLimit" label="安全下限" width="110" align="right" />
            <el-table-column prop="gapQty" label="缺口" width="100" align="right"><template #default="{ row }"><span class="money">{{ row.gapQty }}</span></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 进销存周转 -->
        <el-tab-pane label="进销存周转" name="turnover">
          <div class="toolbar"><el-date-picker v-model="tR.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /><el-button type="primary" style="margin-left: 8px" @click="loadTurnover">查询</el-button></div>
          <el-table :data="tRows" border>
            <el-table-column prop="goodsCode" label="编码" width="100" />
            <el-table-column prop="goodsName" label="商品" min-width="160" />
            <el-table-column prop="outQty" label="期间出库" width="110" align="right" />
            <el-table-column prop="curStock" label="当前库存" width="110" align="right" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const tab = ref('purchase')
const warehouses = ref([])
const purchaseR = ref({ range: null }), saleR = ref({ range: null }), profitR = ref({ range: null })
const cR = ref({ range: null }), sR = ref({ range: null }), tR = ref({ range: null })
const purchaseRows = ref([]), saleRows = ref([]), profitRows = ref([])
const agingList = ref([]), cRows = ref([]), sRows = ref([]), lowRows = ref([]), tRows = ref([])
const lowWarehouseId = ref(null)
const agingRef = ref(null)
let chart = null

const params = range => range && range[0] ? { startDate: range[0], endDate: range[1] } : {}

const loadPurchase = async () => { purchaseRows.value = await request.get('/report/purchase', { params: params(purchaseR.value.range) }) }
const loadSale = async () => { saleRows.value = await request.get('/report/sale', { params: params(saleR.value.range) }) }
const loadProfit = async () => { profitRows.value = await request.get('/report/profit', { params: params(profitR.value.range) }) }
const loadCustomer = async () => { cRows.value = await request.get('/report/customer-contribution', { params: params(cR.value.range) }) }
const loadSupplier = async () => { sRows.value = await request.get('/report/supplier-contribution', { params: params(sR.value.range) }) }
const loadLowStock = async () => { lowRows.value = await request.get('/report/low-stock', { params: { warehouseId: lowWarehouseId.value || null } }) }
const loadTurnover = async () => { tRows.value = await request.get('/report/turnover', { params: params(tR.value.range) }) }

const loadAging = async () => {
  const data = await request.get('/report/aging')
  const arc = data.arc || {}
  const list = [
    { label: '30天内', amount: arc.age30 || 0 },
    { label: '31-60天', amount: arc.age60 || 0 },
    { label: '61-90天', amount: arc.age90 || 0 },
    { label: '90天以上', amount: arc.ageOver || 0 }
  ]
  const total = list.reduce((s, x) => s + Number(x.amount), 0) || 1
  agingList.value = list.map(x => ({ ...x, ratio: (Number(x.amount) / total * 100).toFixed(1) + '%' }))
  if (chart) chart.dispose()
  if (!agingRef.value) return
  chart = echarts.init(agingRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#1E3A6F', '#2E5DA8', '#5B8DEF', '#C7D6F2'],
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '44%'],
      label: { formatter: '{b}: {d}%' },
      data: list.filter(x => Number(x.amount) > 0).map(x => ({ name: x.label, value: Number(x.amount) }))
    }]
  })
}

function downloadCsv(rows, name) {
  if (!rows.length) return
  const headers = Object.keys(rows[0])
  const lines = [headers.join(','), ...rows.map(r => headers.map(h => `"${r[h] ?? ''}"`).join(','))]
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${name}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

const onResize = () => chart && chart.resize()
onMounted(async () => {
  loadPurchase(); loadSale(); loadProfit(); loadCustomer(); loadSupplier(); loadLowStock(); loadTurnover(); loadAging()
  warehouses.value = await request.get('/base/warehouses')
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); chart && chart.dispose() })
</script>
