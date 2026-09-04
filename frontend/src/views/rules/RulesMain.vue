<template>
  <div>
    <div class="page-title">公共规则</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 车辆 -->
        <el-tab-pane label="车辆管理" name="vehicle">
          <div class="toolbar"><el-button type="primary" @click="open('vehicle')">新增车辆</el-button></div>
          <el-table :data="vehicles" border>
            <el-table-column prop="plateNo" label="车牌号" width="140" />
            <el-table-column prop="vehicleType" label="车型" width="120" />
            <el-table-column prop="driver" label="司机" width="110" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }"><el-tag size="small" :type="row.status === '空闲' ? 'success' : row.status === '在途' ? 'warning' : 'info'">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('vehicle', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('vehicles', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 会议室 -->
        <el-tab-pane label="会议室" name="meeting">
          <div class="toolbar"><el-button type="primary" @click="open('meeting')">新增会议室</el-button></div>
          <el-table :data="meetings" border>
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="location" label="位置" min-width="160" />
            <el-table-column prop="capacity" label="容量" width="90" />
            <el-table-column prop="equipment" label="设备" min-width="180" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('meeting', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('meetings', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 系统参数 -->
        <el-tab-pane label="系统参数" name="param">
          <div class="toolbar"><el-button type="primary" @click="open('param')">新增参数</el-button></div>
          <el-table :data="params" border>
            <el-table-column prop="paramKey" label="参数键" min-width="160" />
            <el-table-column prop="paramValue" label="参数值" min-width="180" />
            <el-table-column prop="remark" label="说明" min-width="200" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('param', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('params', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 数据字典 -->
        <el-tab-pane label="数据字典" name="dict">
          <div class="toolbar">
            <div>
              <el-select v-model="dictFilter" clearable placeholder="按字典类型" style="width: 200px" @change="loadDicts">
                <el-option v-for="t in dictTypes" :key="t" :label="t" :value="t" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="open('dict')">新增字典</el-button>
            </div>
          </div>
          <el-table :data="dicts" border>
            <el-table-column prop="dictType" label="字典类型" min-width="130" />
            <el-table-column prop="dictLabel" label="字典标签" min-width="140" />
            <el-table-column prop="dictValue" label="字典值" width="120" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('dict', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('dicts', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 编码规则 -->
        <el-tab-pane label="编码规则" name="coderule">
          <div class="toolbar"><el-button type="primary" @click="open('coderule')">新增规则</el-button></div>
          <el-table :data="codeRules" border>
            <el-table-column prop="docType" label="单据类型" width="120" />
            <el-table-column prop="prefix" label="前缀" width="100" />
            <el-table-column prop="format" label="格式模板" min-width="160" />
            <el-table-column prop="currentSeq" label="当前流水" width="100" />
            <el-table-column prop="remark" label="说明" min-width="160" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('coderule', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('code-rules', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 审批规则 -->
        <el-tab-pane label="审批规则" name="approval">
          <div class="toolbar"><el-button type="primary" @click="open('approval')">新增规则</el-button></div>
          <el-table :data="approvalRules" border>
            <el-table-column prop="docType" label="单据类型" width="140" />
            <el-table-column prop="roleCode" label="审批角色" width="160" />
            <el-table-column prop="level" label="审批层级" width="100" />
            <el-table-column prop="remark" label="说明" min-width="200" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="open('approval', row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del('approval-rules', row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 审计查询 -->
        <el-tab-pane label="审计查询" name="audit">
          <div class="toolbar">
            <div>
              <el-input v-model="auditQuery.operator" placeholder="操作人" clearable style="width: 140px" />
              <el-input v-model="auditQuery.action" placeholder="操作类型" clearable style="width: 160px; margin-left: 8px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadAudits">查询</el-button>
            </div>
          </div>
          <el-table :data="auditPage.list" border>
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="action" label="操作类型" width="160" />
            <el-table-column prop="target" label="操作对象" min-width="180" />
            <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <div class="pagination-bar">
            <el-pagination layout="total, prev, pager, next" :total="auditPage.total" :page-size="10"
              v-model:current-page="auditQuery.page" @current-change="loadAudits" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 通用弹窗 -->
    <el-dialog v-model="dlg" :title="form.id ? '编辑' : '新增'" width="480px">
      <el-form :model="form" label-width="90px">
        <template v-if="dlgType === 'vehicle'">
          <el-form-item label="车牌号"><el-input v-model="form.plateNo" /></el-form-item>
          <el-form-item label="车型"><el-input v-model="form.vehicleType" /></el-form-item>
          <el-form-item label="司机"><el-input v-model="form.driver" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="状态"><el-select v-model="form.status" style="width: 100%"><el-option label="空闲" value="空闲" /><el-option label="在途" value="在途" /><el-option label="维修" value="维修" /></el-select></el-form-item>
        </template>
        <template v-else-if="dlgType === 'meeting'">
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
          <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
          <el-form-item label="设备"><el-input v-model="form.equipment" /></el-form-item>
        </template>
        <template v-else-if="dlgType === 'param'">
          <el-form-item label="参数键"><el-input v-model="form.paramKey" /></el-form-item>
          <el-form-item label="参数值"><el-input v-model="form.paramValue" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="form.remark" /></el-form-item>
        </template>
        <template v-else-if="dlgType === 'dict'">
          <el-form-item label="字典类型"><el-input v-model="form.dictType" /></el-form-item>
          <el-form-item label="字典标签"><el-input v-model="form.dictLabel" /></el-form-item>
          <el-form-item label="字典值"><el-input v-model="form.dictValue" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        </template>
        <template v-else-if="dlgType === 'coderule'">
          <el-form-item label="单据类型"><el-input v-model="form.docType" placeholder="如 PO/SO/IN/OUT" /></el-form-item>
          <el-form-item label="前缀"><el-input v-model="form.prefix" /></el-form-item>
          <el-form-item label="格式模板"><el-input v-model="form.format" placeholder="{prefix}{yyyyMMdd}{seq}" /></el-form-item>
          <el-form-item label="当前流水"><el-input-number v-model="form.currentSeq" :min="0" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="form.remark" /></el-form-item>
        </template>
        <template v-else-if="dlgType === 'approval'">
          <el-form-item label="单据类型"><el-select v-model="form.docType" style="width: 100%"><el-option v-for="o in ['PURCHASE','SALE','TRANSFER','RETURN','FUND']" :key="o" :label="o" :value="o" /></el-select></el-form-item>
          <el-form-item label="审批角色"><el-input v-model="form.roleCode" placeholder="如 ROLE_PURCHASE" /></el-form-item>
          <el-form-item label="审批层级"><el-input-number v-model="form.level" :min="1" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="form.remark" /></el-form-item>
        </template>
      </el-form>
      <template #footer><el-button @click="dlg = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('vehicle')
const vehicles = ref([]), meetings = ref([]), params = ref([])
const dicts = ref([]), dictTypes = ref([]), dictFilter = ref('')
const codeRules = ref([]), approvalRules = ref([])
const auditPage = ref({ total: 0, list: [] })
const auditQuery = ref({ operator: '', action: '', page: 1 })

const dlg = ref(false), dlgType = ref(''), form = ref({})
const urlMap = {
  vehicle: '/config/vehicles', meeting: '/config/meetings', param: '/config/params',
  dict: '/config/dicts', coderule: '/config/code-rules', approval: '/config/approval-rules'
}

const loadAll = async () => {
  const [v, m, p, cr, ar] = await Promise.all([
    request.get('/config/vehicles'), request.get('/config/meetings'), request.get('/config/params'),
    request.get('/config/code-rules'), request.get('/config/approval-rules')
  ])
  vehicles.value = v; meetings.value = m; params.value = p; codeRules.value = cr; approvalRules.value = ar
}
const loadDicts = async () => {
  dicts.value = await request.get('/config/dicts', { params: { dictType: dictFilter.value } })
}
const loadAudits = async () => { auditPage.value = await request.get('/config/audit-logs', { params: auditQuery.value }) }

onMounted(async () => {
  loadAll(); loadDicts(); loadAudits()
  dictTypes.value = await request.get('/config/dicts/types')
})

function open(type, row) {
  dlgType.value = type
  form.value = row ? { ...row } : (type === 'approval' ? { level: 1 } : type === 'coderule' ? { currentSeq: 0 } : {})
  dlg.value = true
}
async function save() {
  await request.post(urlMap[dlgType.value], form.value)
  ElMessage.success('保存成功'); dlg.value = false
  loadAll(); if (dlgType.value === 'dict') loadDicts()
}
async function del(url, id) {
  await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
  await request.delete(`/config/${url}/${id}`)
  ElMessage.success('已删除'); loadAll(); if (url === 'dicts') loadDicts()
}
</script>
