/**
 * 角色维护页面
 */
window.RolePage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="角色编码/名称" clearable style="width:220px" @keyup.enter="search"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px">
        <el-option label="启用" :value="1"></el-option>
        <el-option label="停用" :value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_ROLE_ADD')" type="primary" @click="openCreate">新增角色</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="roleCode" label="角色编码" width="140"></el-table-column>
      <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column label="数据范围" width="130" align="center">
        <template #default="scope">{{ dataScopeLabel(scope.row.dataScope) }}</template>
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
      <el-table-column prop="userCount" label="用户数" width="80" align="center">
        <template #default="scope">{{ scope.row.userCount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="resourceCount" label="资源数" width="80" align="center">
        <template #default="scope">{{ scope.row.resourceCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button v-if="canGrant()" size="small" link type="primary" @click="goGrant(scope.row)">授权</el-button>
          <el-button v-if="hasPerm('B_ROLE_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_ROLE_COPY')" size="small" link type="primary" @click="openCopy(scope.row)">复制</el-button>
          <el-button v-if="hasPerm('B_ROLE_STATUS')" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="hasPerm('B_ROLE_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" maxlength="32"></el-input></el-form-item>
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" maxlength="64"></el-input></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" maxlength="256"></el-input></el-form-item>
        <el-form-item label="数据范围">
          <el-radio-group v-model="form.dataScope">
            <el-radio :label="1">全部数据</el-radio>
            <el-radio :label="2">本部门及子部门</el-radio>
            <el-radio :label="3">本部门</el-radio>
            <el-radio :label="4">本人</el-radio>
            <el-radio :label="5">本仓库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dialog.mode === 'copy'" label="复制并覆盖授权">：将以源角色为模板创建新角色并复制其功能授权，请在下方填写新编码与名称。</el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" maxlength="512"></el-input></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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
      query: { pageNum: 1, pageSize: 20, keyword: '', status: null },
      dialog: { visible: false, isEdit: false, mode: 'create', title: '新增角色' },
      form: this.blankForm(),
      scopeMaps: {
        1: '全部数据', 2: '本部门及子部门', 3: '本部门', 4: '本人', 5: '本仓库'
      },
      rules: {
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      }
    };
  },
  created() { this.load(); },
  methods: {
    hasPerm: hasPerm,
    canGrant() { return hasPerm('B_GRANT_FUNC') || hasPerm('B_GRANT_DATA') || hasPerm('B_GRANT_USER'); },
    dataScopeLabel(code) { return this.scopeMaps[code] || '-'; },
    blankForm() {
      return { id: null, roleCode: '', roleName: '', description: '', dataScope: 3, dataScopeIds: null, status: 1, remark: '' };
    },
    load() {
      this.loading = true;
      Api.get('/api/roles/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    search() { this.query.pageNum = 1; this.load(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', status: null };
      this.load();
    },
    onPage(p) { this.query.pageNum = p; this.load(); },
    onSize(s) { this.query.pageSize = s; this.query.pageNum = 1; this.load(); },
    openCreate() {
      this.form = this.blankForm();
      this.dialog.mode = 'create';
      this.dialog.isEdit = false;
      this.dialog.title = '新增角色';
      this.dialog.visible = true;
    },
    openEdit(row) {
      this.form = {
        id: row.id, roleCode: row.roleCode, roleName: row.roleName, description: row.description,
        dataScope: row.dataScope || 3, dataScopeIds: row.dataScopeIds, status: row.status, remark: row.remark
      };
      this.dialog.mode = 'edit';
      this.dialog.isEdit = true;
      this.dialog.title = '编辑角色';
      this.dialog.visible = true;
    },
    openCopy(row) {
      this.form = { id: null, roleCode: '', roleName: '', description: row.description, dataScope: row.dataScope, dataScopeIds: null, status: 1, remark: row.remark };
      this.dialog.mode = 'copy';
      this.dialog.isEdit = false;
      this.dialog.srcId = row.id;
      this.dialog.title = '复制角色：' + row.roleName;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        this.saving = true;
        let req;
        if (this.dialog.mode === 'copy') {
          req = Api.post('/api/roles/' + this.dialog.srcId + '/copy', this.form);
        } else if (this.dialog.isEdit) {
          req = Api.put('/api/roles', this.form);
        } else {
          req = Api.post('/api/roles', this.form);
        }
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/roles/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    doDelete(row) {
      this.$confirm('确认删除角色「' + row.roleName + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/roles/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    },
    goGrant(row) {
      location.hash = '#/role-grant?roleId=' + row.id;
    }
  }
};
