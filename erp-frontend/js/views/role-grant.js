/**
 * 角色授权页面（功能权限 / 数据权限 / 用户分配）
 * 支持从角色列表页跳转：hash = #/role-grant?roleId=3
 */
window.RoleGrantPage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-select v-model="roleId" placeholder="请选择角色" filterable style="width:260px" @change="onRoleChange">
        <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName + '（' + r.roleCode + '）'" :value="r.id"></el-option>
      </el-select>
      <span v-if="!roleId" class="plain-text">请先选择要授权的角色</span>
    </div>

    <el-tabs v-model="tab" v-if="roleId" @tab-change="onTabChange">
      <el-tab-pane label="功能权限" name="func">
        <div v-if="!hasPerm('B_GRANT_FUNC')" class="plain-text">无功能权限授权权限（B_GRANT_FUNC）</div>
        <template v-else>
          <div class="tree-box" v-loading="funcLoading">
            <el-tree ref="resTree" :data="resTree" show-checkbox node-key="id" default-expand-all
              :props="{ label: 'resName', children: 'children' }"></el-tree>
          </div>
          <div class="tree-actions">
            <el-button @click="clearFunc">清空选择</el-button>
            <el-button type="primary" :loading="funcSaving" @click="saveFunc">保存功能授权</el-button>
          </div>
        </template>
      </el-tab-pane>

      <el-tab-pane label="数据权限" name="data">
        <div v-if="!hasPerm('B_GRANT_DATA')" class="plain-text">无数据权限设置权限（B_GRANT_DATA）</div>
        <template v-else>
          <el-form label-width="120px" style="max-width:720px">
            <el-form-item label="数据范围">
              <el-radio-group v-model="dataScope">
                <el-radio :label="1">全部数据</el-radio>
                <el-radio :label="2">本部门及子部门</el-radio>
                <el-radio :label="3">本部门</el-radio>
                <el-radio :label="4">本人</el-radio>
                <el-radio :label="5">本仓库</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="dataScope === 2 && canSeeDept" label="部门明细">
              <div class="tree-box">
                <el-tree ref="deptTree" :data="deptTree" show-checkbox node-key="id"
                  :props="{ label: 'deptName', children: 'children' }"></el-tree>
              </div>
            </el-form-item>
            <el-form-item v-if="dataScope === 5 && canSeeWarehouse" label="仓库明细">
              <el-select v-model="warehouseIds" multiple filterable placeholder="请选择仓库" style="width:100%">
                <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.whName + '（' + w.whCode + '）'" :value="w.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="dataSaving" @click="saveData">保存数据权限</el-button>
            </el-form-item>
          </el-form>
        </template>
      </el-tab-pane>

      <el-tab-pane label="用户分配" name="user">
        <div v-if="!hasPerm('B_GRANT_USER')" class="plain-text">无用户分配权限（B_GRANT_USER）</div>
        <template v-else>
          <div class="toolbar">
            <el-input v-model="userKeyword" placeholder="按登录名/员工姓名筛选" clearable style="width:220px" @keyup.enter="loadUsers"></el-input>
            <el-button type="primary" @click="loadUsers">查询</el-button>
          </div>
          <el-select v-model="selectedUserIds" multiple filterable placeholder="请选择该角色下的用户（覆盖式）" style="width:100%" :loading="userLoading">
            <el-option v-for="u in userOptions" :key="u.id" :label="u.username + '（' + (u.employeeName || '无关联员工') + '）'" :value="u.id"></el-option>
          </el-select>
          <div class="tree-actions">
            <el-button @click="selectedUserIds = []">清空选择</el-button>
            <el-button type="primary" :loading="userSaving" @click="saveUsers">保存用户分配</el-button>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
  `,
  data() {
    return {
      roleId: null,
      tab: 'func',
      roleOptions: [],
      resTree: [],
      funcLoading: false,
      funcSaving: false,
      dataScope: 3,
      warehouseIds: [],
      warehouseOptions: [],
      deptTree: [],
      dataSaving: false,
      userKeyword: '',
      userOptions: [],
      selectedUserIds: [],
      userLoading: false,
      userSaving: false
    };
  },
  computed: {
    canSeeDept() {
      return hasPerm('P_DEPARTMENT');
    },
    canSeeWarehouse() {
      return hasPerm('P_WAREHOUSE');
    }
  },
  created() {
    this.loadRoles();
    this.resolveHashRole();
    if (this.canSeeDept) {
      this.loadDeptTree();
    }
    if (this.canSeeWarehouse) {
      this.loadWarehouses();
    }
  },
  methods: {
    hasPerm: hasPerm,
    resolveHashRole() {
      const m = location.hash.match(/roleId=(\d+)/);
      if (m) {
        this.roleId = parseInt(m[1], 10);
      }
    },
    loadRoles() {
      Api.get('/api/roles/list').then(list => {
        this.roleOptions = list || [];
      }).catch(e => this.$message.error(e.message));
    },
    loadDeptTree() {
      Api.get('/api/departments/tree').then(tree => {
        this.deptTree = tree || [];
      }).catch(() => { this.deptTree = []; });
    },
    loadWarehouses() {
      Api.get('/api/warehouses/list').then(list => {
        this.warehouseOptions = list || [];
      }).catch(() => { this.warehouseOptions = []; });
    },
    onRoleChange() {
      this.tab = 'func';
      this.loadRoleDetail();
    },
    onTabChange(name) {
      if (name === 'data') {
        this.loadRoleDetail();
      } else if (name === 'user') {
        this.loadUsers();
      } else if (name === 'func') {
        this.loadRoleDetail();
      }
    },
    loadRoleDetail() {
      if (!this.roleId) return;
      Api.get('/api/roles/' + this.roleId).then(role => {
        // 功能权限树回显
        const checked = role.resourceIds || [];
        if (this.tab === 'func') {
          this.resTree = [];
          this.funcLoading = true;
          Api.get('/api/resources/tree').then(tree => {
            this.resTree = tree || [];
            this.$nextTick(() => {
              const ref = this.$refs.resTree;
              if (ref) {
                ref.setCheckedKeys(checked);
              }
            });
          }).catch(e => this.$message.error(e.message)).finally(() => { this.funcLoading = false; });
        }
        // 数据权限回显
        this.dataScope = role.dataScope || 3;
        this.warehouseIds = (role.dataScopeIds || '').split(',').filter(s => s && s.length > 0).map(s => parseInt(s, 10));
        this.$nextTick(() => {
          if (this.$refs.deptTree) {
            this.$refs.deptTree.setCheckedKeys(this.warehouseIds);
          }
        });
      }).catch(e => this.$message.error(e.message));
    },
    clearFunc() {
      if (this.$refs.resTree) {
        this.$refs.resTree.setCheckedKeys([]);
      }
    },
    saveFunc() {
      if (!this.roleId || !this.$refs.resTree) return;
      this.funcSaving = true;
      const checked = this.$refs.resTree.getCheckedKeys() || [];
      const half = this.$refs.resTree.getHalfCheckedKeys() || [];
      const resourceIds = checked.concat(half);
      Api.put('/api/role-grant/resources', { roleId: this.roleId, resourceIds: resourceIds })
        .then(() => {
          this.$message.success('功能授权已保存');
        }).catch(e => this.$message.error(e.message)).finally(() => { this.funcSaving = false; });
    },
    saveData() {
      if (!this.roleId) return;
      let dataScopeIds = null;
      if (this.dataScope === 2) {
        const ids = this.$refs.deptTree ? (this.$refs.deptTree.getCheckedKeys() || []) : [];
        const half = this.$refs.deptTree ? (this.$refs.deptTree.getHalfCheckedKeys() || []) : [];
        dataScopeIds = Array.from(new Set(ids.concat(half))).join(',') || null;
      } else if (this.dataScope === 5) {
        dataScopeIds = (this.warehouseIds || []).join(',') || null;
      }
      this.dataSaving = true;
      Api.put('/api/role-grant/data-scope', { roleId: this.roleId, dataScope: this.dataScope, dataScopeIds: dataScopeIds })
        .then(() => {
          this.$message.success('数据权限已保存');
        }).catch(e => this.$message.error(e.message)).finally(() => { this.dataSaving = false; });
    },
    loadUsers() {
      if (!this.roleId) return;
      this.userLoading = true;
      const params = { pageNum: 1, pageSize: 200, roleId: this.roleId };
      if (this.userKeyword) {
        params.keyword = this.userKeyword;
      }
      Promise.all([
        Api.get('/api/users/page', params),
        Api.get('/api/users/page', { pageNum: 1, pageSize: 200, keyword: this.userKeyword || undefined })
      ]).then(([assigned, all]) => {
        // 回显已分配用户：该角色下用户集合（由 roleId 过滤接口返回）
        this.selectedUserIds = (assigned.list || []).map(u => u.id);
        // 可选用户全集（用于下拉展示）
        this.userOptions = (all.list || []).filter(u => u.isBuiltin !== 1 || true);
      }).catch(e => this.$message.error(e.message)).finally(() => { this.userLoading = false; });
    },
    saveUsers() {
      if (!this.roleId) return;
      this.userSaving = true;
      Api.put('/api/role-grant/users', { roleId: this.roleId, userIds: this.selectedUserIds })
        .then(() => {
          this.$message.success('用户分配已保存');
          this.loadUsers();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.userSaving = false; });
    }
  }
};
