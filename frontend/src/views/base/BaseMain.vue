<template>
  <div>
    <div class="page-title">基础维护</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 地区 -->
        <el-tab-pane label="地区管理" name="region">
          <div class="toolbar">
            <el-button type="primary" @click="openRegion()">新增地区</el-button>
          </div>
          <el-table :data="regions" row-key="id" :tree-props="{ children: 'children' }" border>
            <el-table-column prop="name" label="地区名称" min-width="200" />
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openRegion(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delRegion(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 分公司 -->
        <el-tab-pane label="分公司管理" name="company">
          <div class="toolbar">
            <el-button type="primary" @click="openCompany()">新增分公司</el-button>
          </div>
          <el-table :data="companies" border>
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="name" label="公司名称" min-width="200" />
            <el-table-column prop="regionName" label="所属地区" width="140" />
            <el-table-column prop="contact" label="联系人" width="110" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openCompany(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delCompany(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 部门 -->
        <el-tab-pane label="部门管理" name="dept">
          <div class="toolbar">
            <div>
              <el-select v-model="deptQuery.companyId" placeholder="按公司筛选" clearable style="width: 200px" @change="loadDepts">
                <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </div>
            <el-button type="primary" @click="openDept()">新增部门</el-button>
          </div>
          <el-table :data="depts" border>
            <el-table-column prop="name" label="部门名称" min-width="160" />
            <el-table-column prop="companyName" label="所属公司" min-width="160" />
            <el-table-column prop="manager" label="负责人" width="110" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openDept(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delDept(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 员工 -->
        <el-tab-pane label="员工管理" name="employee">
          <div class="toolbar">
            <div>
              <el-input v-model="empQuery.keyword" placeholder="姓名/工号" clearable style="width: 200px" @change="loadEmps" />
              <el-select v-model="empQuery.deptId" placeholder="部门" clearable style="width: 160px; margin-left: 8px" @change="loadEmps">
                <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadEmps">查询</el-button>
            </div>
            <el-button type="primary" @click="openEmp()">新增员工</el-button>
          </div>
          <el-table :data="empPage.list" border>
            <el-table-column prop="code" label="工号" width="110" />
            <el-table-column prop="name" label="姓名" width="110" />
            <el-table-column prop="gender" label="性别" width="80" />
            <el-table-column prop="deptName" label="部门" min-width="130" />
            <el-table-column prop="position" label="岗位" min-width="120" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openEmp(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delEmp(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination layout="total, prev, pager, next" :total="empPage.total" :page-size="10"
              v-model:current-page="empQuery.page" @current-change="loadEmps" />
          </div>
        </el-tab-pane>

        <!-- 仓库 -->
        <el-tab-pane label="仓库管理" name="warehouse">
          <div class="toolbar">
            <div>
              <el-input v-model="whQuery.keyword" placeholder="仓库名称" clearable style="width: 200px" @change="loadWarehouses" />
              <el-button type="primary" style="margin-left: 8px" @click="loadWarehouses">查询</el-button>
            </div>
            <el-button type="primary" @click="openWh()">新增仓库</el-button>
          </div>
          <el-table :data="warehouses" border>
            <el-table-column prop="code" label="编码" width="110" />
            <el-table-column prop="name" label="仓库名称" min-width="160" />
            <el-table-column prop="manager" label="负责人" width="110" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="address" label="地址" min-width="200" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openWh(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delWh(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 地区弹窗 -->
    <el-dialog v-model="regionDlg" :title="regionForm.id ? '编辑地区' : '新增地区'" width="420px">
      <el-form :model="regionForm" label-width="80px">
        <el-form-item label="上级地区">
          <el-tree-select v-model="regionForm.parentId" :data="regionSelect" :props="{ label: 'name', value: 'id' }" check-strictly clearable placeholder="无（顶级）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地区名称"><el-input v-model="regionForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="regionForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="regionDlg = false">取消</el-button><el-button type="primary" @click="saveRegion">保存</el-button></template>
    </el-dialog>

    <!-- 分公司弹窗 -->
    <el-dialog v-model="companyDlg" :title="companyForm.id ? '编辑分公司' : '新增分公司'" width="480px">
      <el-form :model="companyForm" label-width="80px">
        <el-form-item label="编码"><el-input v-model="companyForm.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="companyForm.name" /></el-form-item>
        <el-form-item label="所属地区">
          <el-tree-select v-model="companyForm.regionId" :data="regionSelect" :props="{ label: 'name', value: 'id' }" check-strictly clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="companyForm.contact" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="companyForm.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="companyForm.address" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="companyDlg = false">取消</el-button><el-button type="primary" @click="saveCompany">保存</el-button></template>
    </el-dialog>

    <!-- 部门弹窗 -->
    <el-dialog v-model="deptDlg" :title="deptForm.id ? '编辑部门' : '新增部门'" width="460px">
      <el-form :model="deptForm" label-width="80px">
        <el-form-item label="部门名称"><el-input v-model="deptForm.name" /></el-form-item>
        <el-form-item label="所属公司">
          <el-select v-model="deptForm.companyId" style="width: 100%"><el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="deptForm.manager" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="deptForm.phone" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="deptDlg = false">取消</el-button><el-button type="primary" @click="saveDept">保存</el-button></template>
    </el-dialog>

    <!-- 员工弹窗 -->
    <el-dialog v-model="empDlg" :title="empForm.id ? '编辑员工' : '新增员工'" width="520px">
      <el-form :model="empForm" label-width="80px">
        <el-form-item label="工号"><el-input v-model="empForm.code" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="empForm.name" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="empForm.gender" style="width: 100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="empForm.deptId" style="width: 100%"><el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" /></el-select>
        </el-form-item>
        <el-form-item label="岗位"><el-input v-model="empForm.position" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="empForm.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="empForm.roleIds" multiple style="width: 100%"><el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="empDlg = false">取消</el-button><el-button type="primary" @click="saveEmp">保存</el-button></template>
    </el-dialog>

    <!-- 仓库弹窗 -->
    <el-dialog v-model="whDlg" :title="whForm.id ? '编辑仓库' : '新增仓库'" width="480px">
      <el-form :model="whForm" label-width="80px">
        <el-form-item label="编码"><el-input v-model="whForm.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="whForm.name" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="whForm.manager" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="whForm.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="whForm.address" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="whDlg = false">取消</el-button><el-button type="primary" @click="saveWh">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('region')
// 列表数据
const regions = ref([])
const companies = ref([])
const depts = ref([])
const warehouses = ref([])
const roles = ref([])
const empPage = ref({ total: 0, list: [] })
const empQuery = ref({ keyword: '', deptId: null, page: 1 })
const whQuery = ref({ keyword: '' })
const deptQuery = ref({ companyId: null })
const regionSelect = ref([])

const regionDlg = ref(false), companyDlg = ref(false), deptDlg = ref(false), empDlg = ref(false), whDlg = ref(false)
const regionForm = ref({}), companyForm = ref({}), deptForm = ref({}), empForm = ref({}), whForm = ref({})

const loadAll = async () => {
  const [r, c, d, w, rolesData] = await Promise.all([
    request.get('/base/regions/tree'),
    request.get('/base/companies'),
    request.get('/base/depts'),
    request.get('/base/warehouses'),
    request.get('/permission/roles')
  ])
  regions.value = r; regionSelect.value = r
  companies.value = c; depts.value = d; warehouses.value = w; roles.value = rolesData
}
const loadDepts = async () => { depts.value = await request.get('/base/depts', { params: { companyId: deptQuery.value.companyId } }) }
const loadEmps = async () => { empPage.value = await request.get('/base/employees', { params: empQuery.value }) }
const loadWarehouses = async () => { warehouses.value = await request.get('/base/warehouses', { params: whQuery.value }) }

onMounted(() => { loadAll(); loadEmps() })

function openRegion(row) { regionForm.value = row ? { ...row } : { parentId: null, sort: 0 }; regionDlg.value = true }
async function saveRegion() {
  await request.post('/base/regions', regionForm.value)
  ElMessage.success('保存成功'); regionDlg.value = false; loadAll()
}
async function delRegion(row) {
  await ElMessageBox.confirm(`确认删除地区【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/base/regions/${row.id}`); ElMessage.success('已删除'); loadAll()
}
function openCompany(row) { companyForm.value = row ? { ...row } : {}; companyDlg.value = true }
async function saveCompany() {
  await request.post('/base/companies', companyForm.value); ElMessage.success('保存成功'); companyDlg.value = false; loadAll()
}
async function delCompany(row) {
  await ElMessageBox.confirm(`确认删除公司【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/base/companies/${row.id}`); ElMessage.success('已删除'); loadAll()
}
function openDept(row) { deptForm.value = row ? { ...row } : {}; deptDlg.value = true }
async function saveDept() {
  await request.post('/base/depts', deptForm.value); ElMessage.success('保存成功'); deptDlg.value = false; loadDepts()
}
async function delDept(row) {
  await ElMessageBox.confirm(`确认删除部门【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/base/depts/${row.id}`); ElMessage.success('已删除'); loadDepts()
}
function openEmp(row) { empForm.value = row ? { ...row, roleIds: row.roleIds ? row.roleIds.split(',').map(Number) : [] } : { gender: '男' }; empDlg.value = true }
async function saveEmp() {
  await request.post('/base/employees', empForm.value); ElMessage.success('保存成功'); empDlg.value = false; loadEmps()
}
async function delEmp(row) {
  await ElMessageBox.confirm(`确认删除员工【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/base/employees/${row.id}`); ElMessage.success('已删除'); loadEmps()
}
function openWh(row) { whForm.value = row ? { ...row } : {}; whDlg.value = true }
async function saveWh() {
  await request.post('/base/warehouses', whForm.value); ElMessage.success('保存成功'); whDlg.value = false; loadWarehouses()
}
async function delWh(row) {
  await ElMessageBox.confirm(`确认删除仓库【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/base/warehouses/${row.id}`); ElMessage.success('已删除'); loadWarehouses()
}
</script>
