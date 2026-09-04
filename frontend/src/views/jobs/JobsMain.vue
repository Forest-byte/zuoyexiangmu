<template>
  <div>
    <div class="page-title">定时任务</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 任务列表 -->
        <el-tab-pane label="任务列表" name="task">
          <div class="toolbar"><el-button type="primary" @click="openTask()">新增任务</el-button></div>
          <el-table :data="tasks" border>
            <el-table-column prop="jobCode" label="任务编码" width="130" />
            <el-table-column prop="jobName" label="任务名称" min-width="160" />
            <el-table-column prop="cronExpr" label="Cron 表达式" width="140" />
            <el-table-column prop="jobGroup" label="任务组" width="100" />
            <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
            <el-table-column prop="enabled" label="启用" width="90">
              <template #default="{ row }"><el-tag size="small" :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="lastRunTime" label="上次执行" width="160" />
            <el-table-column prop="nextRunTime" label="下次执行" width="160" />
            <el-table-column label="操作" width="230">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="runTask(row)">立即执行</el-button>
                <el-button size="small" @click="toggleTask(row)">{{ row.enabled === 1 ? '停用' : '启用' }}</el-button>
                <el-button size="small" @click="openTask(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delTask(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 任务日志 -->
        <el-tab-pane label="任务日志" name="log">
          <div class="toolbar">
            <div>
              <el-select v-model="logJobId" clearable placeholder="按任务筛选" style="width: 200px">
                <el-option v-for="t in tasks" :key="t.id" :label="t.jobName" :value="t.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadLogs">查询</el-button>
            </div>
          </div>
          <el-table :data="logPage.list" border>
            <el-table-column prop="jobName" label="任务" min-width="140" />
            <el-table-column prop="status" label="结果" width="90"><template #default="{ row }"><el-tag size="small" :type="row.status === 'SUCCESS' ? 'success' : 'danger'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column prop="detail" label="执行信息" min-width="240" show-overflow-tooltip />
            <el-table-column prop="startTime" label="开始时间" width="170" />
            <el-table-column prop="costMs" label="耗时(ms)" width="100" align="right" />
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="logPage.total" :page-size="10" v-model:current-page="logQuery.page" @current-change="loadLogs" /></div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="taskDlg" :title="taskForm.id ? '编辑任务' : '新增任务'" width="520px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务编码"><el-input v-model="taskForm.jobCode" placeholder="如 REPORT_SNAPSHOT" /></el-form-item>
        <el-form-item label="任务名称"><el-input v-model="taskForm.jobName" /></el-form-item>
        <el-form-item label="Cron 表达式"><el-input v-model="taskForm.cronExpr" placeholder="如 0 0 8 * * ?" /></el-form-item>
        <el-form-item label="任务组"><el-input v-model="taskForm.jobGroup" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="taskForm.description" type="textarea" /></el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="taskForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="taskDlg = false">取消</el-button><el-button type="primary" @click="saveTask">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('task')
const tasks = ref([])
const logPage = ref({ total: 0, list: [] }), logQuery = ref({ page: 1 }), logJobId = ref(null)
const taskDlg = ref(false), taskForm = ref({})

const loadTasks = async () => { tasks.value = await request.get('/job/tasks') }
const loadLogs = async () => { logPage.value = await request.get('/job/logs', { params: { jobId: logJobId.value || null, page: logQuery.value.page } }) }

function openTask(row) { taskForm.value = row ? { ...row } : { enabled: 1 }; taskDlg.value = true }
async function saveTask() { await request.post('/job/tasks', taskForm.value); ElMessage.success('保存成功'); taskDlg.value = false; loadTasks() }
async function runTask(row) {
  const res = await request.post(`/job/tasks/${row.id}/run`)
  ElMessage.success(res?.message || '执行完成'); loadTasks()
}
async function toggleTask(row) {
  await request.post(`/job/tasks/${row.id}/toggle`, { enabled: row.enabled === 1 ? 0 : 1 })
  ElMessage.success('状态已更新'); loadTasks()
}
async function delTask(row) {
  await ElMessageBox.confirm(`确认删除任务【${row.jobName}】？`, '提示', { type: 'warning' })
  await request.delete(`/job/tasks/${row.id}`); ElMessage.success('已删除'); loadTasks()
}

onMounted(() => { loadTasks(); loadLogs() })
</script>
