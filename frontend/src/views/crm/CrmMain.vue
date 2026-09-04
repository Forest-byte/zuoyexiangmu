<template>
  <div>
    <div class="page-title">CRM 管理</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 客户管理 -->
        <el-tab-pane label="客户管理" name="customer">
          <div class="toolbar">
            <div>
              <el-input v-model="cusQuery.keyword" placeholder="客户名称/编码" clearable style="width: 200px" />
              <el-select v-model="cusQuery.status" clearable placeholder="状态" style="width: 120px; margin-left: 8px">
                <el-option label="正常" value="正常" /><el-option label="停用" value="停用" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadCus">查询</el-button>
            </div>
            <el-button type="primary" @click="openCus()">新增客户</el-button>
          </div>
          <el-table :data="cusPage.list" border>
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="name" label="客户名称" min-width="160" />
            <el-table-column prop="contact" label="联系人" width="100" />
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="creditLimit" label="信用额度" width="110" align="right">
              <template #default="{ row }"><span class="money">{{ row.creditLimit }}</span></template>
            </el-table-column>
            <el-table-column prop="usedCredit" label="已用" width="100" align="right">
              <template #default="{ row }"><span>{{ row.usedCredit }}</span></template>
            </el-table-column>
            <el-table-column prop="approvalStatus" label="审批状态" width="100">
              <template #default="{ row }"><el-tag size="small" :type="row.approvalStatus === '已通过' ? 'success' : row.approvalStatus === '待审批' ? 'warning' : 'info'">{{ row.approvalStatus }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="290">
              <template #default="{ row }">
                <el-button size="small" @click="openCus(row)">编辑</el-button>
                <el-button size="small" type="primary" plain @click="submitCus(row)">提交</el-button>
                <el-button size="small" type="success" plain @click="approveCus(row, true)">通过</el-button>
                <el-button size="small" type="warning" plain @click="approveCus(row, false)">驳回</el-button>
                <el-button size="small" type="danger" @click="delCus(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination layout="total, prev, pager, next" :total="cusPage.total" :page-size="10" v-model:current-page="cusQuery.page" @current-change="loadCus" />
          </div>
        </el-tab-pane>

        <!-- 供应商 -->
        <el-tab-pane label="供应商管理" name="supplier">
          <div class="toolbar">
            <div><el-input v-model="supQuery.keyword" placeholder="供应商名称" clearable style="width: 200px" /><el-button type="primary" style="margin-left: 8px" @click="loadSup">查询</el-button></div>
            <el-button type="primary" @click="openSup()">新增供应商</el-button>
          </div>
          <el-table :data="supPage.list" border>
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="name" label="供应商名称" min-width="170" />
            <el-table-column prop="contact" label="联系人" width="100" />
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="payableAmount" label="应付金额" width="120" align="right">
              <template #default="{ row }"><span class="money">{{ row.payableAmount }}</span></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openSup(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delSup(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination layout="total, prev, pager, next" :total="supPage.total" :page-size="10" v-model:current-page="supQuery.page" @current-change="loadSup" />
          </div>
        </el-tab-pane>

        <!-- 伙伴分类 -->
        <el-tab-pane label="伙伴分类" name="category">
          <div class="toolbar"><el-button type="primary" @click="openCat()">新增分类</el-button></div>
          <el-table :data="catTree" row-key="id" :tree-props="{ children: 'children' }" border>
            <el-table-column prop="name" label="分类名称" min-width="200" />
            <el-table-column prop="kind" label="类型" width="120">
              <template #default="{ row }"><el-tag size="small" :type="row.kind === 'CUSTOMER' ? 'primary' : 'success'">{{ row.kind }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openCat(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delCat(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 跟进记录 -->
        <el-tab-pane label="跟进记录" name="follow">
          <div class="toolbar">
            <div>
              <el-select v-model="followCustomerId" filterable placeholder="选择客户" style="width: 220px" @change="loadFollows">
                <el-option v-for="c in allCustomers" :key="c.id" :label="`${c.name}(${c.code})`" :value="c.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="followDlg = true">新增跟进</el-button>
            </div>
          </div>
          <el-table :data="follows" border>
            <el-table-column prop="customerName" label="客户" min-width="140" />
            <el-table-column prop="content" label="跟进内容" min-width="260" show-overflow-tooltip />
            <el-table-column prop="recorder" label="记录人" width="110" />
            <el-table-column prop="recordTime" label="记录时间" width="170" />
            <el-table-column prop="nextTime" label="下次跟进" width="120" />
          </el-table>
        </el-tab-pane>

        <!-- 信用管理 -->
        <el-tab-pane label="信用管理" name="credit">
          <div class="toolbar">
            <div>
              <el-select v-model="creditCustomerId" filterable placeholder="选择客户" style="width: 220px" @change="loadCredit">
                <el-option v-for="c in allCustomers" :key="c.id" :label="`${c.name}(${c.code})`" :value="c.id" />
              </el-select>
            </div>
          </div>
          <template v-if="creditInfo">
            <el-descriptions :column="3" border style="margin-bottom: 16px">
              <el-descriptions-item label="客户">{{ creditInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="信用额度"><span class="money">{{ creditInfo.creditLimit }}</span></el-descriptions-item>
              <el-descriptions-item label="可用额度"><span class="green">{{ creditInfo.available }}</span></el-descriptions-item>
              <el-descriptions-item label="已用额度">{{ creditInfo.usedCredit }}</el-descriptions-item>
              <el-descriptions-item label="欠款金额"><span class="money">{{ creditInfo.debtAmount }}</span></el-descriptions-item>
              <el-descriptions-item label="操作"><el-button size="small" type="warning" @click="creditDlg = true">调整额度</el-button></el-descriptions-item>
            </el-descriptions>
            <el-table :data="creditInfo.logs" border max-height="320">
              <el-table-column prop="changeAmount" label="调整金额" width="120" align="right" />
              <el-table-column prop="reason" label="调整原因" min-width="220" />
              <el-table-column prop="operator" label="操作人" width="110" />
              <el-table-column prop="operateTime" label="时间" width="170" />
            </el-table>
          </template>
        </el-tab-pane>

        <!-- 往来对账 -->
        <el-tab-pane label="往来对账" name="reconcile">
          <el-tabs>
            <el-tab-pane label="应收明细">
              <div class="toolbar">
                <el-select v-model="arcCustomerId" filterable clearable placeholder="按客户筛选" style="width: 200px" @change="loadArc">
                  <el-option v-for="c in allCustomers" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
              </div>
              <el-table :data="arcPage.list" border>
                <el-table-column prop="refNo" label="来源单号" width="170" />
                <el-table-column prop="customerName" label="客户" min-width="140" />
                <el-table-column prop="amount" label="应收金额" width="120" align="right" />
                <el-table-column prop="received" label="已收" width="110" align="right" />
                <el-table-column prop="balance" label="余额" width="110" align="right"><template #default="{ row }"><span class="money">{{ row.balance }}</span></template></el-table-column>
                <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '已结清' ? 'success' : row.status === '部分' ? 'warning' : 'danger'">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column prop="dueDate" label="到期日" width="120" />
              </el-table>
              <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="arcPage.total" :page-size="10" v-model:current-page="arcQuery.page" @current-change="loadArc" /></div>
            </el-tab-pane>
            <el-tab-pane label="应付明细">
              <div class="toolbar">
                <el-select v-model="apSupplierId" filterable clearable placeholder="按供应商筛选" style="width: 200px" @change="loadAp">
                  <el-option v-for="s in allSuppliers" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
              </div>
              <el-table :data="apPage.list" border>
                <el-table-column prop="refNo" label="来源单号" width="170" />
                <el-table-column prop="supplierName" label="供应商" min-width="140" />
                <el-table-column prop="amount" label="应付金额" width="120" align="right" />
                <el-table-column prop="paid" label="已付" width="110" align="right" />
                <el-table-column prop="balance" label="余额" width="110" align="right"><template #default="{ row }"><span class="money">{{ row.balance }}</span></template></el-table-column>
                <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '已结清' ? 'success' : row.status === '部分' ? 'warning' : 'danger'">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column prop="dueDate" label="到期日" width="120" />
              </el-table>
              <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="apPage.total" :page-size="10" v-model:current-page="apQuery.page" @current-change="loadAp" /></div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 客户合并 -->
        <el-tab-pane label="客户合并" name="merge">
          <el-form label-width="120px" style="max-width: 520px; margin-top: 10px">
            <el-form-item label="从客户（被合并）">
              <el-select v-model="mergeFrom" filterable style="width: 100%"><el-option v-for="c in allCustomers" :key="c.id" :label="`${c.name}(${c.code})`" :value="c.id" /></el-select>
            </el-form-item>
            <el-form-item label="主客户（保留）">
              <el-select v-model="mergeTo" filterable style="width: 100%"><el-option v-for="c in allCustomers" :key="c.id" :label="`${c.name}(${c.code})`" :value="c.id" /></el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="doMerge">执行合并</el-button>
              <span style="margin-left: 12px; color: #909399; font-size: 12px">合并后从客户的单据/应收/跟进将转移到主客户，从客户停用</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 客户弹窗 -->
    <el-dialog v-model="cusDlg" :title="cusForm.id ? '编辑客户' : '新增客户'" width="520px">
      <el-form :model="cusForm" label-width="90px">
        <el-form-item label="编码"><el-input v-model="cusForm.code" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="cusForm.name" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="cusForm.contact" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="cusForm.phone" /></el-form-item>
        <el-form-item label="分类">
          <el-tree-select v-model="cusForm.categoryId" :data="catTree" :props="{ label: 'name', value: 'id' }" check-strictly clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="信用额度"><el-input-number v-model="cusForm.creditLimit" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="cusForm.address" /></el-form-item>
        <el-form-item label="客户级别">
          <el-select v-model="cusForm.level" style="width: 100%"><el-option label="普通" value="普通" /><el-option label="重要" value="重要" /><el-option label="VIP" value="VIP" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="cusDlg = false">取消</el-button><el-button type="primary" @click="saveCus">保存</el-button></template>
    </el-dialog>

    <!-- 供应商弹窗 -->
    <el-dialog v-model="supDlg" :title="supForm.id ? '编辑供应商' : '新增供应商'" width="520px">
      <el-form :model="supForm" label-width="90px">
        <el-form-item label="编码"><el-input v-model="supForm.code" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="supForm.name" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="supForm.contact" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="supForm.phone" /></el-form-item>
        <el-form-item label="分类">
          <el-tree-select v-model="supForm.categoryId" :data="catTree" :props="{ label: 'name', value: 'id' }" check-strictly clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="supForm.address" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="supDlg = false">取消</el-button><el-button type="primary" @click="saveSup">保存</el-button></template>
    </el-dialog>

    <!-- 分类弹窗 -->
    <el-dialog v-model="catDlg" :title="catForm.id ? '编辑分类' : '新增分类'" width="440px">
      <el-form :model="catForm" label-width="90px">
        <el-form-item label="分类名称"><el-input v-model="catForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="catForm.kind" style="width: 100%"><el-option label="客户" value="CUSTOMER" /><el-option label="供应商" value="SUPPLIER" /></el-select>
        </el-form-item>
        <el-form-item label="上级分类">
          <el-tree-select v-model="catForm.parentId" :data="catTree" :props="{ label: 'name', value: 'id' }" check-strictly clearable placeholder="无" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="catDlg = false">取消</el-button><el-button type="primary" @click="saveCat">保存</el-button></template>
    </el-dialog>

    <!-- 跟进弹窗 -->
    <el-dialog v-model="followDlg" title="新增跟进记录" width="480px">
      <el-form label-width="90px">
        <el-form-item label="客户">{{ currentFollowCusName }}</el-form-item>
        <el-form-item label="跟进内容"><el-input v-model="followForm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="下次跟进"><el-date-picker v-model="followForm.nextTime" type="date" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="followDlg = false">取消</el-button><el-button type="primary" @click="saveFollow">保存</el-button></template>
    </el-dialog>

    <!-- 信用调整弹窗 -->
    <el-dialog v-model="creditDlg" title="调整信用额度" width="440px">
      <el-form label-width="90px">
        <el-form-item label="新额度"><el-input-number v-model="creditForm.newLimit" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="调整原因"><el-input v-model="creditForm.reason" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="creditDlg = false">取消</el-button><el-button type="primary" @click="saveCredit">确认调整</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('customer')
const cusPage = ref({ total: 0, list: [] })
const cusQuery = ref({ keyword: '', status: '', page: 1 })
const supPage = ref({ total: 0, list: [] })
const supQuery = ref({ keyword: '', page: 1 })
const catTree = ref([])
const allCustomers = ref([]), allSuppliers = ref([])
const follows = ref([]), followCustomerId = ref(null)
const creditInfo = ref(null), creditCustomerId = ref(null)
const arcPage = ref({ total: 0, list: [] }), arcQuery = ref({ page: 1 }), arcCustomerId = ref(null)
const apPage = ref({ total: 0, list: [] }), apQuery = ref({ page: 1 }), apSupplierId = ref(null)
const mergeFrom = ref(null), mergeTo = ref(null)

const cusDlg = ref(false), supDlg = ref(false), catDlg = ref(false), followDlg = ref(false), creditDlg = ref(false)
const cusForm = ref({}), supForm = ref({}), catForm = ref({})
const followForm = ref({ content: '', nextTime: '' })
const creditForm = ref({ newLimit: 0, reason: '' })
const currentFollowCusName = computed(() => {
  const c = allCustomers.value.find(x => x.id === followCustomerId.value)
  return c ? c.name : ''
})

const loadCus = async () => { cusPage.value = await request.get('/crm/customers', { params: cusQuery.value }) }
const loadSup = async () => { supPage.value = await request.get('/crm/suppliers', { params: supQuery.value }) }
const loadCat = async () => { catTree.value = await request.get('/crm/categories/tree') }
const loadFollows = async () => { follows.value = followCustomerId.value ? await request.get('/crm/follows', { params: { customerId: followCustomerId.value } }) : [] }
const loadCredit = async () => { creditInfo.value = creditCustomerId.value ? await request.get('/crm/credit', { params: { customerId: creditCustomerId.value } }) : null }
const loadArc = async () => { arcPage.value = await request.get('/crm/arc', { params: { customerId: arcCustomerId.value, page: arcQuery.value.page } }) }
const loadAp = async () => { apPage.value = await request.get('/crm/ap', { params: { supplierId: apSupplierId.value, page: apQuery.value.page } }) }

onMounted(async () => {
  loadCus(); loadSup(); loadCat(); loadArc(); loadAp()
  allCustomers.value = await request.get('/crm/customers/all')
  allSuppliers.value = await request.get('/crm/suppliers/all')
})

function openCus(row) { cusForm.value = row ? { ...row } : { creditLimit: 10000, level: '普通' }; cusDlg.value = true }
async function saveCus() {
  await request.post('/crm/customers', cusForm.value); ElMessage.success('保存成功'); cusDlg.value = false; loadCus()
  allCustomers.value = await request.get('/crm/customers/all')
}
async function submitCus(row) { await request.post(`/crm/customers/${row.id}/submit`); ElMessage.success('已提交审批'); loadCus() }
async function approveCus(row, pass) {
  await request.post(`/crm/customers/${row.id}/approve`, { pass })
  ElMessage.success(pass ? '审批通过' : '已驳回'); loadCus()
}
async function delCus(row) {
  await ElMessageBox.confirm(`确认删除客户【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/crm/customers/${row.id}`); ElMessage.success('已删除'); loadCus()
}
function openSup(row) { supForm.value = row ? { ...row } : {}; supDlg.value = true }
async function saveSup() {
  await request.post('/crm/suppliers', supForm.value); ElMessage.success('保存成功'); supDlg.value = false; loadSup()
  allSuppliers.value = await request.get('/crm/suppliers/all')
}
async function delSup(row) {
  await ElMessageBox.confirm(`确认删除供应商【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/crm/suppliers/${row.id}`); ElMessage.success('已删除'); loadSup()
}
function openCat(row) { catForm.value = row ? { ...row } : { kind: 'CUSTOMER' }; catDlg.value = true }
async function saveCat() { await request.post('/crm/categories', catForm.value); ElMessage.success('保存成功'); catDlg.value = false; loadCat() }
async function delCat(row) {
  await ElMessageBox.confirm(`确认删除分类【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/crm/categories/${row.id}`); ElMessage.success('已删除'); loadCat()
}
async function saveFollow() {
  await request.post('/crm/follows', { customerId: followCustomerId.value, content: followForm.value.content, nextTime: followForm.value.nextTime })
  ElMessage.success('已记录'); followDlg.value = false; followForm.value = { content: '', nextTime: '' }; loadFollows()
}
async function saveCredit() {
  await request.post('/crm/credit/change', { customerId: creditCustomerId.value, newLimit: creditForm.value.newLimit, reason: creditForm.value.reason })
  ElMessage.success('额度已调整'); creditDlg.value = false; loadCredit(); loadCus()
}
async function doMerge() {
  if (!mergeFrom.value || !mergeTo.value) return ElMessage.warning('请选择主客户与从客户')
  await request.post('/crm/customers/merge', { fromId: mergeFrom.value, toId: mergeTo.value })
  ElMessage.success('合并成功'); loadCus(); allCustomers.value = await request.get('/crm/customers/all')
}
</script>
