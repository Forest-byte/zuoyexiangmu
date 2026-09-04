/**
 * 用户账号页面（角色权限-用户账号）
 */
window.UserPage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="登录名/员工姓名" clearable style="width:220px" @keyup.enter="search"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px">
        <el-option label="启用" :value="1"></el-option>
        <el-option label="停用" :value="0"></el-option>
      </el-select>
      <el-select v-if="hasPerm('P_ROLE')" v-model="query.roleId" placeholder="按角色筛选" clearable filterable style="width:180px">
        <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id"></el-option>
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_USER_ADD')" type="primary" @click="openCreate">新增账号</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column type="selection" width="45"></el-table-column>
      <el-table-column prop="username" label="登录名" width="140"></el-table-column>
      <el-table-column prop="employeeName" label="关联员工" width="120">
        <template #default="scope">{{ scope.row.employeeName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="departmentName" label="所属部门" width="140">
        <template #default="scope">{{ scope.row.departmentName || '-' }}</template>
      </el-table-column>
      <el-table-column label="角色" min-width="180">
        <template #default="scope">
          <el-tag v-for="n in scope.row.roleNames" :key="n" size="small" class="mr5">{{ n }}</el-tag>
          <span v-if="!scope.row.roleNames || scope.row.roleNames.length === 0" class="plain-text">-</span>
        </template>
      </el-table-column>
      <el-table-column label="内置" width="70" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.isBuiltin === 1" type="danger" size="small">内置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="tag-status">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最近登录" width="170">
        <template #default="scope">{{ scope.row.lastLoginTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button v-if="canEdit(scope.row)" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="canGrantRole(scope.row)" size="small" link type="primary" @click="openRoles(scope.row)">分配角色</el-button>
          <el-button v-if="hasPerm('B_USER_RESET')" size="small" link type="warning" @click="openReset(scope.row)">重置密码</el-button>
          <el-button v-if="canToggle(scope.row)" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="canDelete(scope.row)" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑账号' : '新增账号'" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="登录名" prop="username"><el-input v-model="form.username" maxlength="64"></el-input></el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="初始密码" prop="password"><el-input v-model="form.password" type="password" show-password maxlength="64"></el-input></el-form-item>
        <el-form-item v-if="canPickEmployee" label="关联员工">
          <el-select v-model="form.employeeId" placeholder="可选，一人一账号" clearable filterable style="width:100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="e.name + '（' + e.empNo + '）'" :value="e.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialog.visible" title="分配角色" width="460px">
      <p class="plain-text" style="margin-top:0">为账号「{{ roleDialog.username }}」分配角色（覆盖式保存）。</p>
      <el-select v-model="roleDialog.roleIds" multiple filterable style="width:100%" placeholder="请选择角色">
        <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName + '（' + r.roleCode + '）'" :value="r.id"></el-option>
      </el-select>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDialog.visible" title="重置密码" width="420px">
      <el-form :model="pwdDialog" :rules="pwdRules" ref="pwdRef" label-width="100px">
        <el-form-item label="登录名">
          <span>{{ pwdDialog.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdDialog.newPassword" type="password" show-password maxlength="64"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="savePwd">保存</el-button>
      </template>
    </el-dialog>
  </div>
  `,
  data() {
    return {
      rows: [],
      total: 0,
      loading: false,
      saving: false,
      roleSaving: false,
      pwdSaving: false,
      query: { pageNum: 1, pageSize: 20, keyword: '', status: null, roleId: null },
      roleOptions: [],
      employeeOptions: [],
      dialog: { visible: false, isEdit: false },
      form: this.blankForm(),
      roleDialog: { visible: false, id: null, username: '', roleIds: [] },
      pwdDialog: { visible: false, id: null, username: '', newPassword: '' },
      rules: {
        username: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
      },
      pwdRules: {
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
      }
    };
  },
  computed: {
    canPickEmployee() {
      return hasPerm('P_EMPLOYEE');
    }
  },
  created() {
    this.load();
    this.loadRoles();
    if (this.canPickEmployee) {
      this.loadEmployees();
    }
  },
  methods: {
    hasPerm: hasPerm,
    blankForm() {
      return { id: null, username: '', password: '', employeeId: null, status: 1 };
    },
    isBuiltin(row) {
      return row && row.isBuiltin === 1;
    },
    canEdit(row) {
      return hasPerm('B_USER_EDIT') && (!this.isBuiltin(row) || AppStore.user && AppStore.user.userId === row.id);
    },
    canToggle(row) {
      return hasPerm('B_USER_STATUS') && !this.isBuiltin(row);
    },
    canDelete(row) {
      return hasPerm('B_USER_DELETE') && !this.isBuiltin(row) && !(AppStore.user && AppStore.user.userId === row.id);
    },
    canGrantRole(row) {
      return hasPerm('B_USER_ROLE') && !this.isBuiltin(row);
    },
    load() {
      this.loading = true;
      Api.get('/api/users/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    loadRoles() {
      Api.get('/api/roles/list').then(list => {
        this.roleOptions = list || [];
      }).catch(() => { this.roleOptions = []; });
    },
    loadEmployees() {
      Api.get('/api/employees/list-working').then(list => {
        this.employeeOptions = list || [];
      }).catch(() => { this.employeeOptions = []; });
    },
    search() { this.query.pageNum = 1; this.load(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', status: null, roleId: null };
      this.load();
    },
    onPage(p) { this.query.pageNum = p; this.load(); },
    onSize(s) { this.query.pageSize = s; this.query.pageNum = 1; this.load(); },
    openCreate() {
      this.form = this.blankForm();
      this.dialog.isEdit = false;
      this.dialog.visible = true;
    },
    openEdit(row) {
      this.form = { id: row.id, username: row.username, password: '', employeeId: row.employeeId, status: row.status };
      this.dialog.isEdit = true;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        this.saving = true;
        const req = this.dialog.isEdit ? Api.put('/api/users', this.form) : Api.post('/api/users', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/users/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    openRoles(row) {
      this.roleDialog.id = row.id;
      this.roleDialog.username = row.username;
      this.roleDialog.roleIds = (row.roleIds || []).slice();
      this.roleDialog.visible = true;
    },
    saveRoles() {
      this.roleSaving = true;
      Api.put('/api/users/' + this.roleDialog.id + '/roles', { userId: this.roleDialog.id, roleIds: this.roleDialog.roleIds })
        .then(() => {
          this.$message.success('角色分配成功');
          this.roleDialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.roleSaving = false; });
    },
    openReset(row) {
      this.pwdDialog.id = row.id;
      this.pwdDialog.username = row.username;
      this.pwdDialog.newPassword = '';
      this.pwdDialog.visible = true;
    },
    savePwd() {
      this.$refs.pwdRef.validate(valid => {
        if (!valid) return;
        this.pwdSaving = true;
        Api.put('/api/users/' + this.pwdDialog.id + '/password', { newPassword: this.pwdDialog.newPassword })
          .then(() => {
            this.$message.success('密码已重置');
            this.pwdDialog.visible = false;
          }).catch(e => this.$message.error(e.message)).finally(() => { this.pwdSaving = false; });
      });
    },
    doDelete(row) {
      this.$confirm('确认删除账号「' + row.username + '」？删除后不可恢复。', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/users/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    }
  }
};
