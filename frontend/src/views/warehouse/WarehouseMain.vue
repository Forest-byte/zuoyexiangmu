<template>
  <div>
    <div class="page-title">仓储管理</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 入库单 -->
        <el-tab-pane label="入库单" name="inbound">
          <div class="toolbar">
            <div>
              <el-select v-model="inQuery.inType" clearable placeholder="入库类型" style="width: 150px">
                <el-option v-for="t in ['PURCHASE','MANUAL','TRANSFER']" :key="t" :label="t" :value="t" />
              </el-select>
              <el-input v-model="inQuery.keyword" placeholder="单号" clearable style="width: 160px; margin-left: 8px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadInbounds">查询</el-button>
            </div>
            <el-button type="primary" @click="manualInDlg = true">手工入库</el-button>
          </div>
          <el-table :data="inPage.list" border>
            <el-table-column prop="inNo" label="入库单号" width="160" />
            <el-table-column prop="inType" label="类型" width="100" />
            <el-table-column prop="srcNo" label="来源单号" min-width="160" />
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="inDate" label="日期" width="110" />
            <el-table-column prop="totalAmount" label="金额" width="110" align="right" />
            <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag size="small" type="success">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" @click="showInbound(row)">详情</el-button></template></el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="inPage.total" :page-size="10" v-model:current-page="inQuery.page" @current-change="loadInbounds" /></div>
        </el-tab-pane>

        <!-- 出库单 -->
        <el-tab-pane label="出库单" name="outbound">
          <div class="toolbar">
            <div>
              <el-select v-model="outQuery.outType" clearable placeholder="出库类型" style="width: 150px">
                <el-option v-for="t in ['SALE','MANUAL','TRANSFER']" :key="t" :label="t" :value="t" />
              </el-select>
              <el-input v-model="outQuery.keyword" placeholder="单号" clearable style="width: 160px; margin-left: 8px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadOutbounds">查询</el-button>
            </div>
            <el-button type="primary" @click="manualOutDlg = true">手工出库</el-button>
          </div>
          <el-table :data="outPage.list" border>
            <el-table-column prop="outNo" label="出库单号" width="160" />
            <el-table-column prop="outType" label="类型" width="100" />
            <el-table-column prop="srcNo" label="来源单号" min-width="160" />
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="outDate" label="日期" width="110" />
            <el-table-column prop="operator" label="经办人" width="100" />
            <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag size="small" type="success">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" @click="showOutbound(row)">详情</el-button></template></el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="outPage.total" :page-size="10" v-model:current-page="outQuery.page" @current-change="loadOutbounds" /></div>
        </el-tab-pane>

        <!-- 库存统计 -->
        <el-tab-pane label="库存统计" name="stock">
          <div class="toolbar">
            <div>
              <el-select v-model="stockQuery.warehouseId" clearable placeholder="仓库" style="width: 160px">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
              <el-input v-model="stockQuery.keyword" placeholder="商品名/编码" clearable style="width: 180px; margin-left: 8px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadStocks">查询</el-button>
              <el-button type="warning" style="margin-left: 8px" @click="lowStockDlg = true">低库存预警</el-button>
            </div>
          </div>
          <el-table :data="stockPage.list" border>
            <el-table-column prop="warehouseName" label="仓库" width="130" />
            <el-table-column prop="goodsCode" label="商品编码" width="110" />
            <el-table-column prop="goodsName" label="商品名称" min-width="160" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="quantity" label="库存数量" width="110" align="right">
              <template #default="{ row }"><span :class="row.quantity <= row.lowLimit ? 'money' : ''">{{ row.quantity }}</span></template>
            </el-table-column>
            <el-table-column prop="lowLimit" label="下限" width="90" align="right" />
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="stockPage.total" :page-size="10" v-model:current-page="stockQuery.page" @current-change="loadStocks" /></div>
        </el-tab-pane>

        <!-- 库存流水 -->
        <el-tab-pane label="库存流水" name="stocklog">
          <div class="toolbar">
            <div>
              <el-select v-model="logQuery.goodsId" filterable clearable placeholder="商品" style="width: 200px">
                <el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadLogs">查询</el-button>
            </div>
          </div>
          <el-table :data="logPage.list" border>
            <el-table-column prop="changeTime" label="时间" width="170" />
            <el-table-column prop="goodsName" label="商品" min-width="150" />
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="changeType" label="变动类型" width="120" />
            <el-table-column prop="changeQty" label="变动数量" width="100" align="right" />
            <el-table-column prop="beforeQty" label="变动前" width="90" align="right" />
            <el-table-column prop="afterQty" label="变动后" width="90" align="right" />
            <el-table-column prop="refNo" label="来源单号" min-width="160" />
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="logPage.total" :page-size="10" v-model:current-page="logQuery.page" @current-change="loadLogs" /></div>
        </el-tab-pane>

        <!-- 盘点管理 -->
        <el-tab-pane label="盘点管理" name="check">
          <div class="toolbar">
            <div>
              <el-select v-model="checkQuery.status" clearable placeholder="状态" style="width: 140px">
                <el-option label="待盘点" value="待盘点" /><el-option label="已盘点" value="已盘点" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadChecks">查询</el-button>
            </div>
            <el-button type="primary" @click="createCheck">新建盘点</el-button>
          </div>
          <el-table :data="checkPage.list" border>
            <el-table-column prop="checkNo" label="盘点单号" width="160" />
            <el-table-column prop="warehouseName" label="仓库" width="130" />
            <el-table-column prop="checkDate" label="盘点日期" width="120" />
            <el-table-column prop="checker" label="盘点人" width="100" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '已盘点' ? 'success' : 'warning'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button size="small" @click="openCheckDetail(row)">处理</el-button></template></el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="checkPage.total" :page-size="10" v-model:current-page="checkQuery.page" @current-change="loadChecks" /></div>
        </el-tab-pane>

        <!-- 货物转接（调拨） -->
        <el-tab-pane label="货物转接" name="transfer">
          <div class="toolbar">
            <div>
              <el-select v-model="transferQuery.status" clearable placeholder="状态" style="width: 140px">
                <el-option label="待审核" value="待审核" /><el-option label="已通过" value="已通过" /><el-option label="已驳回" value="已驳回" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadTransfers">查询</el-button>
            </div>
            <el-button type="primary" @click="openTransfer()">申请调拨</el-button>
          </div>
          <el-table :data="transferPage.list" border>
            <el-table-column prop="transferNo" label="调拨单号" width="150" />
            <el-table-column prop="fromName" label="调出仓库" width="130" />
            <el-table-column prop="toName" label="调入仓库" width="130" />
            <el-table-column prop="goodsName" label="商品" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="90" align="right" />
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '已通过' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="success" plain @click="approveTransfer(row, true)">通过</el-button>
                <el-button size="small" type="warning" plain @click="approveTransfer(row, false)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="transferPage.total" :page-size="10" v-model:current-page="transferQuery.page" @current-change="loadTransfers" /></div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 手工入库 -->
    <el-dialog v-model="manualInDlg" title="手工入库" width="680px">
      <el-form label-width="90px">
        <el-form-item label="入库仓库">
          <el-select v-model="manualInForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="manualInForm.inType" style="width: 100%"><el-option label="期初入库" value="INIT" /><el-option label="退补入库" value="REFUND" /><el-option label="盘盈入库" value="GAIN" /><el-option label="其他入库" value="MANUAL" /></el-select>
        </el-form-item>
        <el-form-item label="明细">
          <el-table :data="manualInForm.items" border size="small" max-height="240">
            <el-table-column label="商品" width="220"><template #default="{ row }"><el-select v-model="row.goodsId" filterable size="small" style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select></template></el-table-column>
            <el-table-column label="数量" width="140"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.001" :precision="3" size="small" style="width: 100%" /></template></el-table-column>
            <el-table-column label="单价" width="130"><template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" style="width: 100%" /></template></el-table-column>
            <el-table-column width="60"><template #default="{ $index }"><el-button size="small" type="danger" @click="manualInForm.items.splice($index, 1)">删</el-button></template></el-table-column>
          </el-table>
          <el-button size="small" type="primary" plain style="margin-top: 6px" @click="manualInForm.items.push({ goodsId: null, quantity: 1, price: 0 })">+ 添加</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="manualInDlg = false">取消</el-button><el-button type="primary" @click="doManualIn">确认入库</el-button></template>
    </el-dialog>

    <!-- 手工出库 -->
    <el-dialog v-model="manualOutDlg" title="手工出库" width="680px">
      <el-form label-width="90px">
        <el-form-item label="出库仓库">
          <el-select v-model="manualOutForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="manualOutForm.outType" style="width: 100%"><el-option label="领用出库" value="USE" /><el-option label="报损出库" value="LOSS" /><el-option label="盘亏出库" value="SHORT" /><el-option label="其他出库" value="MANUAL" /></el-select>
        </el-form-item>
        <el-form-item label="明细">
          <el-table :data="manualOutForm.items" border size="small" max-height="240">
            <el-table-column label="商品" width="220"><template #default="{ row }"><el-select v-model="row.goodsId" filterable size="small" style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select></template></el-table-column>
            <el-table-column label="数量" width="140"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.001" :precision="3" size="small" style="width: 100%" /></template></el-table-column>
            <el-table-column width="60"><template #default="{ $index }"><el-button size="small" type="danger" @click="manualOutForm.items.splice($index, 1)">删</el-button></template></el-table-column>
          </el-table>
          <el-button size="small" type="primary" plain style="margin-top: 6px" @click="manualOutForm.items.push({ goodsId: null, quantity: 1 })">+ 添加</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="manualOutDlg = false">取消</el-button><el-button type="primary" @click="doManualOut">确认出库</el-button></template>
    </el-dialog>

    <!-- 低库存预警 -->
    <el-dialog v-model="lowStockDlg" title="低库存预警" width="720px">
      <el-table :data="lowStocks" border size="small">
        <el-table-column prop="warehouseName" label="仓库" width="130" />
        <el-table-column prop="goodsCode" label="编码" width="110" />
        <el-table-column prop="goodsName" label="商品" min-width="150" />
        <el-table-column prop="quantity" label="库存" width="90" align="right" />
        <el-table-column prop="lowLimit" label="下限" width="90" align="right" />
      </el-table>
      <template #footer><el-button type="primary" @click="lowStockDlg = false">关闭</el-button></template>
    </el-dialog>

    <!-- 入库详情 -->
    <el-dialog v-model="inDetailDlg" :title="`入库单 ${inDetail?.inNo || ''}`" width="720px">
      <template v-if="inDetail">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 10px">
          <el-descriptions-item label="类型">{{ inDetail.inType }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ inDetail.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ inDetail.inDate }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="inDetail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="150" />
          <el-table-column prop="quantity" label="数量" width="100" align="right" />
          <el-table-column prop="price" label="单价" width="100" align="right" />
          <el-table-column prop="amount" label="金额" width="110" align="right" />
        </el-table>
      </template>
    </el-dialog>

    <!-- 出库详情 -->
    <el-dialog v-model="outDetailDlg" :title="`出库单 ${outDetail?.outNo || ''}`" width="720px">
      <template v-if="outDetail">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 10px">
          <el-descriptions-item label="类型">{{ outDetail.outType }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ outDetail.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ outDetail.outDate }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="outDetail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="150" />
          <el-table-column prop="quantity" label="数量" width="100" align="right" />
        </el-table>
      </template>
    </el-dialog>

    <!-- 盘点处理 -->
    <el-dialog v-model="checkDetailDlg" :title="`盘点单 ${checkDetail?.checkNo || ''}`" width="760px">
      <template v-if="checkDetail">
        <el-table :data="checkDetail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="160" />
          <el-table-column prop="bookQty" label="账面数量" width="100" align="right" />
          <el-table-column label="实盘数量" width="140"><template #default="{ row }"><el-input-number v-model="row.realQty" :min="0" :precision="3" size="small" style="width: 100%" /></template></el-table-column>
          <el-table-column prop="diffQty" label="盈亏" width="100" align="right"><template #default="{ row }"><span :class="row.diffQty < 0 ? 'money' : 'green'">{{ row.diffQty }}</span></template></el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="checkDetailDlg = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">提交盘点结果</el-button>
      </template>
    </el-dialog>

    <!-- 调拨申请 -->
    <el-dialog v-model="transferDlg" title="申请调拨" width="520px">
      <el-form :model="transferForm" label-width="90px">
        <el-form-item label="调出仓库"><el-select v-model="transferForm.fromWarehouse" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="调入仓库"><el-select v-model="transferForm.toWarehouse" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select></el-form-item>
        <el-form-item label="商品"><el-select v-model="transferForm.goodsId" filterable style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="transferForm.quantity" :min="0.001" :precision="3" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="transferDlg = false">取消</el-button><el-button type="primary" @click="doTransfer">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('inbound')
const warehouses = ref([]), allGoods = ref([])

// 入库
const inPage = ref({ total: 0, list: [] }), inQuery = ref({ inType: '', keyword: '', page: 1 })
const manualInDlg = ref(false), manualInForm = ref({ items: [] })
const inDetailDlg = ref(false), inDetail = ref(null)
const loadInbounds = async () => { inPage.value = await request.get('/wms/inbounds', { params: inQuery.value }) }
function showInbound(row) { inDetail.value = null; inDetailDlg.value = true; setTimeout(async () => { inDetail.value = await request.get(`/wms/inbounds/${row.id}`) }, 50) }
async function doManualIn() {
  await request.post('/wms/inbounds/manual', { ...manualInForm.value, inType: manualInForm.value.inType, items: manualInForm.value.items.filter(i => i.goodsId).map(i => ({ goodsId: i.goodsId, quantity: i.quantity, price: i.price })) })
  ElMessage.success('入库成功'); manualInDlg.value = false; loadInbounds()
}

// 出库
const outPage = ref({ total: 0, list: [] }), outQuery = ref({ outType: '', keyword: '', page: 1 })
const manualOutDlg = ref(false), manualOutForm = ref({ items: [] })
const outDetailDlg = ref(false), outDetail = ref(null)
const loadOutbounds = async () => { outPage.value = await request.get('/wms/outbounds', { params: outQuery.value }) }
function showOutbound(row) { outDetail.value = null; outDetailDlg.value = true; setTimeout(async () => { outDetail.value = await request.get(`/wms/outbounds/${row.id}`) }, 50) }
async function doManualOut() {
  await request.post('/wms/outbounds/manual', { ...manualOutForm.value, outType: manualOutForm.value.outType, items: manualOutForm.value.items.filter(i => i.goodsId).map(i => ({ goodsId: i.goodsId, quantity: i.quantity })) })
  ElMessage.success('出库成功'); manualOutDlg.value = false; loadOutbounds()
}

// 库存
const stockPage = ref({ total: 0, list: [] }), stockQuery = ref({ warehouseId: null, keyword: '', page: 1 })
const lowStocks = ref([]), lowStockDlg = ref(false)
const loadStocks = async () => { stockPage.value = await request.get('/wms/stocks', { params: stockQuery.value }) }
const loadLow = async () => { lowStocks.value = await request.get('/wms/stocks/low'); lowStockDlg.value = true }

// 流水
const logPage = ref({ total: 0, list: [] }), logQuery = ref({ goodsId: null, page: 1 })
const loadLogs = async () => { logPage.value = await request.get('/wms/stock-logs', { params: logQuery.value }) }

// 盘点
const checkPage = ref({ total: 0, list: [] }), checkQuery = ref({ status: '', page: 1 })
const checkDetailDlg = ref(false), checkDetail = ref(null)
const loadChecks = async () => { checkPage.value = await request.get('/wms/checks', { params: checkQuery.value }) }
async function createCheck() {
  const { value } = await ElMessageBox.prompt('请选择盘点仓库 ID', '新建盘点单', { inputValue: warehouses.value[0]?.id || 1 })
  await request.post('/wms/checks', { warehouseId: Number(value) })
  ElMessage.success('盘点单已创建'); loadChecks()
}
async function openCheckDetail(row) {
  checkDetail.value = await request.get(`/wms/checks/${row.id}`)
  checkDetail.value.items = checkDetail.value.items.map(i => ({ ...i, realQty: i.realQty ?? i.bookQty }))
  checkDetailDlg.value = true
}
async function submitCheck() {
  await request.post(`/wms/checks/${checkDetail.value.id}/submit`, { items: checkDetail.value.items })
  ElMessage.success('盘点完成'); checkDetailDlg.value = false; loadChecks(); loadStocks()
}

// 调拨
const transferPage = ref({ total: 0, list: [] }), transferQuery = ref({ status: '', page: 1 })
const transferDlg = ref(false), transferForm = ref({})
const loadTransfers = async () => { transferPage.value = await request.get('/wms/transfers', { params: transferQuery.value }) }
function openTransfer() { transferForm.value = {}; transferDlg.value = true }
async function doTransfer() { await request.post('/wms/transfers', transferForm.value); ElMessage.success('调拨申请已提交'); transferDlg.value = false; loadTransfers() }
async function approveTransfer(row, pass) {
  await request.post(`/wms/transfers/${row.id}/approve`, { pass, comment: pass ? '同意' : '驳回' })
  ElMessage.success(pass ? '已通过' : '已驳回'); loadTransfers()
}

onMounted(async () => {
  loadInbounds(); loadOutbounds(); loadStocks(); loadLogs(); loadChecks(); loadTransfers()
  const [w, g] = await Promise.all([request.get('/base/warehouses'), request.get('/goods/all')])
  warehouses.value = w; allGoods.value = g
})
</script>
