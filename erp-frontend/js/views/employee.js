/**
 * 员工信息页面
 */
window.EmployeePage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="编号/姓名/手机号" clearable style="width:200px" @keyup.enter="search"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px">
        <el-option v-for="s in empStatusOptions" :key="s.value" :label="s.label" :value="s.value"></el-option>
      </el-select>
      <el-select v-if="hasPerm('P_DEPARTMENT')" v-model="query.departmentId" placeholder="部门" clearable filterable style="width:160px">
        <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_EMP_IMPORT')" @click="openImport">批量导入</el-button>
      <el-button v-if="hasPerm('B_EMP_ADD')" type="primary" @click="openCreate">新增员工</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="empNo" label="工号" width="100"></el-table-column>
      <el-table-column prop="name" label="姓名" width="110"></el-table-column>
      <el-table-column label="性别" width="60" align="center">
        <template #default="scope">{{ scope.row.gender === 0 ? '男' : (scope.row.gender === 1 ? '女' : '-') }}</template>
      </el-table-column>
      <el-table-column prop="departmentName" label="部门" width="130">
        <template #default="scope">{{ scope.row.departmentName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="position" label="岗位" width="110">
        <template #default="scope">{{ scope.row.position || '-' }}</template>
      </el-table-column>
      <el-table-column prop="level" label="职级" width="80">
        <template #default="scope">{{ scope.row.level || '-' }}</template>
      </el-table-column>
      <el-table-column prop="mobileMasked" label="手机号" width="140">
        <template #default="scope">{{ scope.row.mobileMasked || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)" size="small" class="tag-status">{{ empStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('B_EMP_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_EMP_LEAVE')" size="small" link type="warning" @click="doLeave(scope.row)">离职</el-button>
          <el-button v-if="hasPerm('B_EMP_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑员工' : '新增员工'" width="680px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="员工编号" prop="empNo"><el-input v-model="form.empNo" maxlength="32"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio :label="0">男</el-radio>
              <el-radio :label="1">女</el-radio>
            </el-radio-group>
          </el-form-item></el-col>
          <el-col :span="16"><el-form-item label="身份证号"><el-input v-model="form.idCard" maxlength="18" placeholder="选填，展示时脱敏"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.mobile" maxlength="20"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" maxlength="128"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属部门">
            <el-tree-select v-if="hasPerm('P_DEPARTMENT')" v-model="form.departmentId" :data="deptOptions" check-strictly
              :render-after-expand="false" style="width:100%" clearable
              :props="{ label: 'deptName', value: 'id', children: 'children' }" placeholder="选择部门"></el-tree-select>
            <el-input-number v-else v-model="form.departmentId" :min="0" style="width:100%"></el-input-number>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="岗位"><el-input v-model="form.position" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="职级"><el-input v-model="form.level" maxlength="32"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入职日期" prop="hireDate"><el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" style="width:100%"></el-date-picker></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在职状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option label="试用" :value="0"></el-option>
              <el-option label="在职" :value="1"></el-option>
            </el-select>
            <div class="plain-text">离职请使用「离职」操作</div>
          </el-form-item></el-col>
          <el-col v-if="hasPerm('P_USER')" :span="12"><el-form-item label="关联账号">
            <el-select v-model="form.userId" clearable filterable style="width:100%" placeholder="一人一账号，可留空">
              <el-option v-for="u in userOptions" :key="u.id" :label="u.username" :value="u.id"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="512"></el-input></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入员工" width="680px">
      <el-alert type="info" :closable="false" show-icon
        title="每行一条，逗号分隔：工号,姓名,性别(0男1女),身份证,手机,邮箱,部门ID,岗位,职级,入职日期(YYYY-MM-DD),状态(0试用1在职)" />
      <el-input v-model="importText" type="textarea" :rows="10" placeholder="E0007,孙八,0,,13800000007,,3,人事专员,P2,2026-01-01,1&#10;E0008,周九,1,330106199501011299,13800000008,," style="margin-top:12px"></el-input>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
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
      importing: false,
      importVisible: false,
      importText: '',
      query: { pageNum: 1, pageSize: 20, keyword: '', status: null, departmentId: null },
      dialog: { visible: false, isEdit: false },
      form: this.blankForm(),
      deptOptions: [],
      userOptions: [],
      empStatusOptions: [
        { value: 0, label: '试用' }, { value: 1, label: '在职' }, { value: 2, label: '离职' }
      ],
      rules: {
        empNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
      }
    };
  },
  created() {
    this.load();
    if (hasPerm('P_DEPARTMENT')) this.loadDepts();
    if (hasPerm('P_USER')) this.loadUsers();
  },
  methods: {
    hasPerm: hasPerm,
    empStatusLabel(s) {
      return { 0: '试用', 1: '在职', 2: '离职' }[s] || '-';
    },
    statusType(s) {
      if (s === 1) return 'success';
      if (s === 0) return 'warning';
      return 'info';
    },
    blankForm() {
      return {
        id: null, empNo: '', name: '', gender: 0, idCard: '', mobile: '', email: '',
        departmentId: null, position: '', level: '', hireDate: '', status: 1, userId: null, remark: ''
      };
    },
    load() {
      this.loading = true;
      Api.get('/api/employees/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    loadDepts() {
      Api.get('/api/departments/tree').then(data => { this.deptOptions = data || []; })
        .catch(() => { this.deptOptions = []; });
    },
    loadUsers() {
      Api.get('/api/users/page', { pageNum: 1, pageSize: 50 }).then(data => {
        this.userOptions = (data.list || []).filter(u => u.status === 1);
      }).catch(() => { this.userOptions = []; });
    },
    search() { this.query.pageNum = 1; this.load(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', status: null, departmentId: null };
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
      this.form = {
        id: row.id, empNo: row.empNo, name: row.name, gender: row.gender == null ? 0 : row.gender,
        idCard: row.idCard, mobile: row.mobile, email: row.email, departmentId: row.departmentId,
        position: row.position, level: row.level, hireDate: row.hireDate, status: row.status,
        userId: row.userId, remark: row.remark
      };
      this.dialog.isEdit = true;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        this.saving = true;
        const req = this.dialog.isEdit ? Api.put('/api/employees', this.form) : Api.post('/api/employees', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    doLeave(row) {
      this.$confirm('确认员工「' + row.name + '」办理离职？', '提示', { type: 'warning' }).then(() => {
        Api.put('/api/employees/' + row.id + '/leave').then(() => {
          this.$message.success('已办理离职');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    },
    doDelete(row) {
      this.$confirm('确认删除员工「' + row.name + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/employees/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    },
    openImport() {
      this.importText = '';
      this.importVisible = true;
    },
    doImport() {
      const lines = (this.importText || '').split(/\r?\n/).map(l => l.trim()).filter(Boolean);
      if (!lines.length) {
        this.$message.warning('请至少粘贴一行数据');
        return;
      }
      const forms = lines.map(l => {
        const c = l.split(',').map(s => s.trim());
        return {
          empNo: c[0] || '', name: c[1] || '', gender: c[2] !== '' && c[2] != null ? Number(c[2]) : null,
          idCard: c[3] || null, mobile: c[4] || null, email: c[5] || null,
          departmentId: c[6] !== '' && c[6] != null ? Number(c[6]) : null,
          position: c[7] || null, level: c[8] || null, hireDate: c[9] || null,
          status: c[10] !== '' && c[10] != null ? Number(c[10]) : 1, userId: null, remark: c[11] || null
        };
      });
      this.importing = true;
      Api.post('/api/employees/import', forms).then(res => {
        let msg = '共' + res.total + '条，成功' + res.success + '条，失败' + res.fail + '条';
        if (res.errors && res.errors.length) msg += '；' + res.errors.slice(0, 5).join('；');
        this.$message[res.fail ? 'warning' : 'success'](msg);
        this.importVisible = false;
        this.load();
      }).catch(e => this.$message.error(e.message)).finally(() => { this.importing = false; });
    }
  }
};
