<template>
  <div>
    <div class="page-title">财务管理</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 资金账户 -->
        <el-tab-pane label="资金账户" name="account">
          <div class="toolbar">
            <el-button type="primary" @click="openAccount()">新增账户</el-button>
          </div>
          <el-row :gutter="16">
            <el-col v-for="a in accounts" :key="a.id" :span="8" style="margin-bottom: 16px">
              <el-card shadow="hover">
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <div style="font-weight: 600; color: #1E3A6F">{{ a.name }}</div>
                  <div>
                    <el-button size="small" @click="openAccount(a)">编辑</el-button>
                    <el-button size="small" type="danger" @click="delAccount(a)">删除</el-button>
                  </div>
                </div>
                <div style="margin-top: 8px; color: #909399; font-size: 12px">类型：{{ a.accountType }} · {{ a.code }}</div>
                <div style="margin-top: 6px; font-size: 18px; font-weight: 700; color: #2E5DA8"><span class="money">{{ a.balance }}</span></div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 收款登记 -->
        <el-tab-pane label="收款登记" name="receipt">
          <div class="toolbar">
            <div>
              <el-input v-model="receiptQuery.keyword" placeholder="单号/往来单位" clearable style="width: 200px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadReceipts">查询</el-button>
            </div>
            <el-button type="primary" @click="openReceipt()">登记收款</el-button>
          </div>
          <el-table :data="receiptPage.list" border>
            <el-table-column prop="listNo" label="收款单号" width="170" />
            <el-table-column prop="partnerName" label="客户" min-width="140" />
            <el-table-column prop="accountName" label="收款账户" width="130" />
            <el-table-column prop="allMoney" label="金额" width="120" align="right"><template #default="{ row }"><span class="money">{{ row.allMoney }}</span></template></el-table-column>
            <el-table-column prop="payType" label="方式" width="100" />
            <el-table-column prop="receiptDate" label="日期" width="110" />
            <el-table-column prop="states" label="状态" width="90"><template #default="{ row }"><el-tag size="small" :type="row.states === '已核销' ? 'success' : 'primary'">{{ row.states }}</el-tag></template></el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="receiptPage.total" :page-size="10" v-model:current-page="receiptQuery.page" @current-change="loadReceipts" /></div>
        </el-tab-pane>

        <!-- 付款登记 -->
        <el-tab-pane label="付款登记" name="payment">
          <div class="toolbar">
            <div>
              <el-input v-model="payQuery.keyword" placeholder="单号/往来单位" clearable style="width: 200px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadPayments">查询</el-button>
            </div>
            <el-button type="primary" @click="openPayment()">登记付款</el-button>
          </div>
          <el-table :data="payPage.list" border>
            <el-table-column prop="listNo" label="付款单号" width="170" />
            <el-table-column prop="partnerName" label="供应商" min-width="140" />
            <el-table-column prop="accountName" label="付款账户" width="130" />
            <el-table-column prop="allMoney" label="金额" width="120" align="right"><template #default="{ row }"><span class="money">{{ row.allMoney }}</span></template></el-table-column>
            <el-table-column prop="payType" label="方式" width="100" />
            <el-table-column prop="receiptDate" label="日期" width="110" />
            <el-table-column prop="states" label="状态" width="90"><template #default="{ row }"><el-tag size="small" :type="row.states === '已核销' ? 'success' : 'primary'">{{ row.states }}</el-tag></template></el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="payPage.total" :page-size="10" v-model:current-page="payQuery.page" @current-change="loadPayments" /></div>
        </el-tab-pane>

        <!-- 内部转账 -->
        <el-tab-pane label="内部转账" name="transfer">
          <div class="toolbar"><el-button type="primary" @click="openTransfer()">申请转账</el-button></div>
          <el-table :data="transfers" border>
            <el-table-column prop="transferNo" label="转账单号" width="150" />
            <el-table-column prop="fromName" label="转出账户" width="140" />
            <el-table-column prop="toName" label="转入账户" width="140" />
            <el-table-column prop="amount" label="金额" width="120" align="right" />
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column prop="applyTime" label="申请时间" width="170" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '已通过' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.status === '待审核'" size="small" type="success" plain @click="approveTransfer(row, true)">通过</el-button>
                <el-button v-if="row.status === '待审核'" size="small" type="warning" plain @click="approveTransfer(row, false)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 账户流水 -->
        <el-tab-pane label="账户流水" name="log">
          <div class="toolbar">
            <div>
              <el-select v-model="logAccountId" clearable placeholder="选择账户" style="width: 200px">
                <el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadLogs">查询</el-button>
            </div>
          </div>
          <el-table :data="logPage.list" border>
            <el-table-column prop="changeTime" label="时间" width="170" />
            <el-table-column prop="accountName" label="账户" width="130" />
            <el-table-column prop="changeType" label="类型" width="110"><template #default="{ row }"><el-tag size="small" :type="row.changeType === '收款' ? 'success' : row.changeType === '付款' ? 'danger' : 'info'">{{ row.changeType }}</el-tag></template></el-table-column>
            <el-table-column prop="amount" label="金额" width="120" align="right" />
            <el-table-column prop="balance" label="余额" width="120" align="right" />
            <el-table-column prop="refNo" label="关联单号" min-width="160" />
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="logPage.total" :page-size="10" v-model:current-page="logQuery.page" @current-change="loadLogs" /></div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 账户弹窗 -->
    <el-dialog v-model="accountDlg" :title="accountForm.id ? '编辑账户' : '新增账户'" width="460px">
      <el-form :model="accountForm" label-width="90px">
        <el-form-item label="账户编码"><el-input v-model="accountForm.code" /></el-form-item>
        <el-form-item label="账户名称"><el-input v-model="accountForm.name" /></el-form-item>
        <el-form-item label="账户类型">
          <el-select v-model="accountForm.accountType" style="width: 100%"><el-option label="银行账户" value="银行账户" /><el-option label="现金账户" value="现金账户" /><el-option label="支付宝" value="支付宝" /><el-option label="微信" value="微信" /></el-select>
        </el-form-item>
        <el-form-item label="初始余额"><el-input-number v-model="accountForm.balance" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="accountDlg = false">取消</el-button><el-button type="primary" @click="saveAccount">保存</el-button></template>
    </el-dialog>

    <!-- 收款登记 -->
    <el-dialog v-model="receiptDlg" title="登记收款" width="560px">
      <el-form :model="receiptForm" label-width="100px">
        <el-form-item label="客户">
          <el-select v-model="receiptForm.partnerId" filterable style="width: 100%"><el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="收款账户">
          <el-select v-model="receiptForm.accountId" style="width: 100%"><el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        </el-form-item>
        <el-form-item label="收款金额"><el-input-number v-model="receiptForm.allMoney" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="收款方式">
          <el-select v-model="receiptForm.payType" style="width: 100%"><el-option label="银行转账" value="银行转账" /><el-option label="现金" value="现金" /><el-option label="支付宝" value="支付宝" /><el-option label="微信" value="微信" /></el-select>
        </el-form-item>
        <el-form-item label="收款日期"><el-date-picker v-model="receiptForm.receiptDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="关联销售单">
          <el-select v-model="receiptForm.ordersKey" filterable clearable style="width: 100%">
            <el-option v-for="o in saleOrders" :key="o.id" :label="`${o.orderNo}（${o.customerName}，应收 ${o.receivedAmount !== undefined ? (Number(o.allAmount) - Number(o.receivedAmount)).toFixed(2) : '-'}）`" :value="o.orderNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="receiptForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="receiptDlg = false">取消</el-button><el-button type="primary" @click="saveReceipt">登记</el-button></template>
    </el-dialog>

    <!-- 付款登记 -->
    <el-dialog v-model="paymentDlg" title="登记付款" width="560px">
      <el-form :model="paymentForm" label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="paymentForm.partnerId" filterable style="width: 100%"><el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" /></el-select>
        </el-form-item>
        <el-form-item label="付款账户">
          <el-select v-model="paymentForm.accountId" style="width: 100%"><el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        </el-form-item>
        <el-form-item label="付款金额"><el-input-number v-model="paymentForm.allMoney" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="paymentForm.payType" style="width: 100%"><el-option label="银行转账" value="银行转账" /><el-option label="现金" value="现金" /><el-option label="支付宝" value="支付宝" /><el-option label="微信" value="微信" /></el-select>
        </el-form-item>
        <el-form-item label="付款日期"><el-date-picker v-model="paymentForm.receiptDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="关联采购单">
          <el-select v-model="paymentForm.ordersKey" filterable clearable style="width: 100%">
            <el-option v-for="o in purchaseOrders" :key="o.id" :label="o.orderNo" :value="o.orderNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="paymentForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="paymentDlg = false">取消</el-button><el-button type="primary" @click="savePayment">登记</el-button></template>
    </el-dialog>

    <!-- 转账申请 -->
    <el-dialog v-model="transferDlg" title="申请内部转账" width="480px">
      <el-form :model="transferForm" label-width="90px">
        <el-form-item label="转出账户"><el-select v-model="transferForm.fromAccount" style="width: 100%"><el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item>
        <el-form-item label="转入账户"><el-select v-model="transferForm.toAccount" style="width: 100%"><el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item>
        <el-form-item label="转账金额"><el-input-number v-model="transferForm.amount" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="transferDlg = false">取消</el-button><el-button type="primary" @click="saveTransfer">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('account')
const accounts = ref([])
const accountDlg = ref(false), accountForm = ref({})
const customers = ref([]), suppliers = ref([]), saleOrders = ref([]), purchaseOrders = ref([])

// 账户
const loadAccounts = async () => { accounts.value = await request.get('/finance/accounts') }
function openAccount(row) { accountForm.value = row ? { ...row } : { accountType: '银行账户', balance: 0 }; accountDlg.value = true }
async function saveAccount() { await request.post('/finance/accounts', accountForm.value); ElMessage.success('保存成功'); accountDlg.value = false; loadAccounts() }
async function delAccount(row) {
  await ElMessageBox.confirm(`确认删除账户【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/finance/accounts/${row.id}`); ElMessage.success('已删除'); loadAccounts()
}

// 收款
const receiptPage = ref({ total: 0, list: [] }), receiptQuery = ref({ keyword: '', page: 1 })
const receiptDlg = ref(false), receiptForm = ref({})
const loadReceipts = async () => { receiptPage.value = await request.get('/finance/receipts', { params: receiptQuery.value }) }
function openReceipt() {
  const d = new Date()
  receiptForm.value = { receiptDate: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`, payType: '银行转账' }
  receiptDlg.value = true
}
async function saveReceipt() {
  await request.post('/finance/receipts', receiptForm.value)
  ElMessage.success('收款登记成功'); receiptDlg.value = false; loadReceipts(); loadAccounts()
}

// 付款
const payPage = ref({ total: 0, list: [] }), payQuery = ref({ keyword: '', page: 1 })
const paymentDlg = ref(false), paymentForm = ref({})
const loadPayments = async () => { payPage.value = await request.get('/finance/payments', { params: payQuery.value }) }
function openPayment() {
  const d = new Date()
  paymentForm.value = { receiptDate: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`, payType: '银行转账' }
  paymentDlg.value = true
}
async function savePayment() {
  await request.post('/finance/payments', paymentForm.value)
  ElMessage.success('付款登记成功'); paymentDlg.value = false; loadPayments(); loadAccounts()
}

// 转账
const transfers = ref([]), transferDlg = ref(false), transferForm = ref({})
const loadTransfers = async () => { transfers.value = await request.get('/finance/transfers') }
function openTransfer() { transferForm.value = {}; transferDlg.value = true }
async function saveTransfer() { await request.post('/finance/transfers', transferForm.value); ElMessage.success('转账申请已提交'); transferDlg.value = false; loadTransfers() }
async function approveTransfer(row, pass) {
  await request.post(`/finance/transfers/${row.id}/approve`, { pass, comment: pass ? '同意' : '驳回' })
  ElMessage.success(pass ? '已通过' : '已驳回'); loadTransfers(); loadAccounts()
}

// 账户流水
const logPage = ref({ total: 0, list: [] }), logQuery = ref({ page: 1 }), logAccountId = ref(null)
const loadLogs = async () => { logPage.value = await request.get('/finance/account-logs', { params: { accountId: logAccountId.value || null, page: logQuery.value.page } }) }

onMounted(async () => {
  loadAccounts(); loadReceipts(); loadPayments(); loadTransfers(); loadLogs()
  const [c, s, so, po] = await Promise.all([
    request.get('/crm/customers/all'), request.get('/crm/suppliers/all'),
    request.get('/sale/orders', { params: { page: 1 } }), request.get('/purchase/orders', { params: { page: 1 } })
  ])
  customers.value = c; suppliers.value = s; saleOrders.value = so.list; purchaseOrders.value = po.list
})
</script>
