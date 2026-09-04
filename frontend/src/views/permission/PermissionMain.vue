<template>
  <div>
    <div class="page-title">角色权限</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 角色维护 -->
        <el-tab-pane label="角色维护" name="role">
          <div class="toolbar"><el-button type="primary" @click="openRole()">新增角色</el-button></div>
          <el-table :data="roles" border>
            <el-table-column prop="roleCode" label="角色编码" width="160" />
            <el-table-column prop="name" label="角色名称" min-width="160" />
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column label="授权" width="120">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="openGrant(row)">权限</el-button>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openRole(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 资源维护 -->
        <el-tab-pane label="资源维护" name="resource">
          <div class="toolbar"><el-button type="primary" @click="openRes()">新增资源</el-button></div>
          <el-table :data="resTree" row-key="id" :tree-props="{ children: 'children' }" border>
            <el-table-column prop="name" label="资源名称" min-width="180" />
            <el-table-column prop="code" label="权限码" min-width="180" />
            <el-table-column prop="type" label="类型" width="90">
              <template #default="{ row }"><el-tag size="small" :type="row.type === 'menu' ? 'primary' : row.type === 'button' ? 'success' : 'warning'">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openRes(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delRes(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 用户授权 -->
        <el-tab-pane label="用户授权" name="grant">
          <div class="toolbar">
            <div>
              <el-input v-model="userQuery.keyword" placeholder="用户名/姓名" clearable style="width: 200px" />
              <el-button type="primary" style="margin-left: 8px" @click="loadUsers">查询</el-button>
            </div>
          </div>
          <el-table :data="userPage.list" border>
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="roleCode" label="主角色" width="130" />
            <el-table-column label="授权角色" min-width="220">
              <template #default="{ row }">
                <el-tag v-for="r in row.roles" :key="r.id" size="small" style="margin-right: 6px">{{ r.name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="openUserGrant(row)">授权</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination layout="total, prev, pager, next" :total="userPage.total" :page-size="10"
              v-model:current-page="userQuery.page" @current-change="loadUsers" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 角色弹窗 -->
    <el-dialog v-model="roleDlg" :title="roleForm.id ? '编辑角色' : '新增角色'" width="440px">
      <el-form :model="roleForm" label-width="90px">
        <el-form-item label="角色编码"><el-input v-model="roleForm.roleCode" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="roleForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="roleForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="roleDlg = false">取消</el-button><el-button type="primary" @click="saveRole">保存</el-button></template>
    </el-dialog>

    <!-- 资源弹窗 -->
    <el-dialog v-model="resDlg" :title="resForm.id ? '编辑资源' : '新增资源'" width="440px">
      <el-form :model="resForm" label-width="90px">
        <el-form-item label="父级资源">
          <el-tree-select v-model="resForm.parentId" :data="resSelect" :props="{ label: 'name', value: 'id' }" check-strictly clearable placeholder="无（顶级）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="资源名称"><el-input v-model="resForm.name" /></el-form-item>
        <el-form-item label="权限码"><el-input v-model="resForm.code" placeholder="如 inventory:goods" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="resForm.type" style="width: 100%">
            <el-option label="系统" value="system" /><el-option label="模块" value="module" /><el-option label="菜单" value="menu" /><el-option label="按钮" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="resForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="resDlg = false">取消</el-button><el-button type="primary" @click="saveRes">保存</el-button></template>
    </el-dialog>

    <!-- 角色授权弹窗 -->
    <el-dialog v-model="grantDlg" :title="`角色权限：${grantRole?.name}`" width="560px">
      <el-tree ref="grantTree" :data="resTree" show-checkbox node-key="id" :props="{ label: 'name', children: 'children' }" default-expand-all />
      <template #footer>
        <el-button @click="grantDlg = false">取消</el-button>
        <el-button type="primary" @click="saveGrant">保存授权</el-button>
      </template>
    </el-dialog>

    <!-- 用户授权弹窗 -->
    <el-dialog v-model="userGrantDlg" :title="`用户授权：${grantUser?.name}`" width="460px">
      <el-checkbox-group v-model="userGrantRoleIds">
        <el-checkbox v-for="r in roles" :key="r.id" :value="r.id" :label="r.name" style="margin-right: 16px" />
      </el-checkbox-group>
      <template #footer>
        <el-button @click="userGrantDlg = false">取消</el-button>
        <el-button type="primary" @click="saveUserGrant">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('role')
const roles = ref([])
const resTree = ref([])
const resSelect = ref([])
const userPage = ref({ total: 0, list: [] })
const userQuery = ref({ keyword: '', page: 1 })

const roleDlg = ref(false), resDlg = ref(false), grantDlg = ref(false), userGrantDlg = ref(false)
const roleForm = ref({}), resForm = ref({})
const grantTree = ref(null), grantRole = ref(null)
const grantUser = ref(null), userGrantRoleIds = ref([])

const loadRoles = async () => { roles.value = await request.get('/permission/roles') }
const loadRes = async () => { resTree.value = await request.get('/permission/resources/tree'); resSelect.value = resTree.value }
const loadUsers = async () => {
  const p = await request.get('/permission/users', { params: userQuery.value })
  p.list = await Promise.all(p.list.map(async u => {
    u.roles = await request.get(`/permission/users/${u.id}/roles`)
    return u
  }))
  userPage.value = p
}
onMounted(() => { loadRoles(); loadRes(); loadUsers() })

function openRole(row) { roleForm.value = row ? { ...row } : {}; roleDlg.value = true }
async function saveRole() {
  await request.post('/permission/roles', roleForm.value); ElMessage.success('保存成功'); roleDlg.value = false; loadRoles()
}
async function delRole(row) {
  await ElMessageBox.confirm(`确认删除角色【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/permission/roles/${row.id}`); ElMessage.success('已删除'); loadRoles()
}
function openRes(row) { resForm.value = row ? { ...row } : { type: 'menu', sort: 0 }; resDlg.value = true }
async function saveRes() {
  await request.post('/permission/resources', resForm.value); ElMessage.success('保存成功'); resDlg.value = false; loadRes()
}
async function delRes(row) {
  await ElMessageBox.confirm(`确认删除资源【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/permission/resources/${row.id}`); ElMessage.success('已删除'); loadRes()
}
async function openGrant(row) {
  grantRole.value = row
  grantDlg.value = true
  await new Promise(r => setTimeout(r, 50))
  const ids = await request.get(`/permission/roles/${row.id}/resources`)
  grantTree.value.setCheckedKeys(ids)
}
async function saveGrant() {
  const resourceIds = [...grantTree.value.getCheckedKeys(), ...grantTree.value.getHalfCheckedKeys()]
  await request.post(`/permission/roles/${grantRole.value.id}/resources`, { resourceIds })
  ElMessage.success('授权已保存'); grantDlg.value = false
}
async function openUserGrant(row) {
  grantUser.value = row
  const rs = await request.get(`/permission/users/${row.id}/roles`)
  userGrantRoleIds.value = rs.map(r => r.id)
  userGrantDlg.value = true
}
async function saveUserGrant() {
  await request.post(`/permission/users/${grantUser.value.id}/roles`, { roleIds: userGrantRoleIds.value })
  ElMessage.success('授权已保存'); userGrantDlg.value = false; loadUsers()
}
</script>
