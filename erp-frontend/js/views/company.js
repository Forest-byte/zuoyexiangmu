/**
 * 公司信息页面
 */
window.CompanyPage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="编码/名称/信用代码" clearable style="width:220px" @keyup.enter="search"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px">
        <el-option label="启用" :value="1"></el-option>
        <el-option label="停用" :value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_COMPANY_EXPORT')" @click="doExport">导出</el-button>
      <el-button v-if="hasPerm('B_COMPANY_ADD')" type="primary" @click="openCreate">新增公司</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column type="selection" width="45"></el-table-column>
      <el-table-column prop="companyCode" label="公司编码" width="100"></el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="creditCode" label="统一社会信用代码" width="180"></el-table-column>
      <el-table-column prop="legalPerson" label="法定代表人" width="110"></el-table-column>
      <el-table-column prop="phone" label="联系电话" width="130"></el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="tag-status">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('B_COMPANY_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_COMPANY_STATUS')" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="hasPerm('B_COMPANY_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑公司' : '新增公司'" width="620px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="130px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="公司编码" prop="companyCode"><el-input v-model="form.companyCode" maxlength="32"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="公司名称" prop="companyName"><el-input v-model="form.companyName" maxlength="128"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="信用代码" prop="creditCode"><el-input v-model="form.creditCode" maxlength="18"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="法定代表人"><el-input v-model="form.legalPerson" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="注册地址"><el-input v-model="form.address" maxlength="256"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" maxlength="32"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" maxlength="128"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开户银行"><el-input v-model="form.bankName" maxlength="128"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="银行账号"><el-input v-model="form.bankAccount" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="512"></el-input></el-form-item></el-col>
        </el-row>
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
      dialog: { visible: false, isEdit: false },
      form: this.blankForm(),
      rules: {
        companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
        creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.load();
  },
  methods: {
    blankForm() {
      return {
        id: null, companyCode: '', companyName: '', creditCode: '', legalPerson: '',
        address: '', phone: '', email: '', bankName: '', bankAccount: '', status: 1, remark: ''
      };
    },
    load() {
      this.loading = true;
      Api.get('/api/companies/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).finally(() => { this.loading = false; });
    },
    search() { this.query.pageNum = 1; this.load(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', status: null };
      this.load();
    },
    onPage(p) { this.query.pageNum = p; this.load(); },
    onSize(s) { this.query.pageSize = s; this.query.pageNum = 1; this.load(); },
    hasPerm: hasPerm,
    openCreate() {
      this.form = this.blankForm();
      this.dialog.isEdit = false;
      this.dialog.visible = true;
    },
    openEdit(row) {
      this.form = Object.assign(this.blankForm(), row);
      this.dialog.isEdit = true;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        this.saving = true;
        const req = this.dialog.isEdit ? Api.put('/api/companies', this.form) : Api.post('/api/companies', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/companies/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    doDelete(row) {
      this.$confirm('确认删除公司「' + row.companyName + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/companies/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    },
    doExport() {
      downloadBlob('/api/companies/export', 'companies.csv').then(() => {
        this.$message.success('导出成功');
      }).catch(e => this.$message.error(e.message));
    }
  }
};
