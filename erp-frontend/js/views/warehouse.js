/**
 * 仓库信息页面
 */
window.WarehousePage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="编码/名称/联系人" clearable style="width:220px" @keyup.enter="search"></el-input>
      <el-select v-model="query.whType" placeholder="仓库类型" clearable style="width:130px">
        <el-option v-for="t in whTypeOptions" :key="t.value" :label="t.label" :value="t.value"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px">
        <el-option label="启用" :value="1"></el-option>
        <el-option label="停用" :value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_WH_ADD')" type="primary" @click="openCreate">新增仓库</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="whCode" label="仓库编码" width="110"></el-table-column>
      <el-table-column prop="whName" label="仓库名称" min-width="150"></el-table-column>
      <el-table-column label="类型" width="110" align="center">
        <template #default="scope"><el-tag size="small">{{ whTypeLabel(scope.row.whType) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="managerName" label="负责人" width="100">
        <template #default="scope">{{ scope.row.managerName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="region" label="地区" width="150">
        <template #default="scope">{{ scope.row.region || '-' }}</template>
      </el-table-column>
      <el-table-column prop="contact" label="联系人" width="100">
        <template #default="scope">{{ scope.row.contact || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" width="130">
        <template #default="scope">{{ scope.row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="tag-status">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('B_WH_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_WH_STATUS')" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="hasPerm('B_WH_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑仓库' : '新增仓库'" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="仓库编码" prop="whCode"><el-input v-model="form.whCode" maxlength="32"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库名称" prop="whName"><el-input v-model="form.whName" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库类型" prop="whType">
            <el-select v-model="form.whType" style="width:100%">
              <el-option v-for="t in whTypeOptions" :key="t.value" :label="t.label" :value="t.value"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人">
            <el-select v-model="form.managerId" clearable filterable style="width:100%" placeholder="可选在职员工" @change="onManagerChange">
              <el-option v-for="e in managerOptions" :key="e.id" :label="e.name + '（' + e.empNo + '）'" :value="e.id"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所在地区"><el-input v-model="form.region" maxlength="128"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="详细地址"><el-input v-model="form.address" maxlength="256"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contact" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" maxlength="32"></el-input></el-form-item></el-col>
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
      query: { pageNum: 1, pageSize: 20, keyword: '', whType: null, status: null },
      dialog: { visible: false, isEdit: false },
      form: this.blankForm(),
      managerOptions: [],
      whTypeOptions: [
        { value: 1, label: '原材料仓' }, { value: 2, label: '成品仓' },
        { value: 3, label: '半成品仓' }, { value: 4, label: '退货仓' }, { value: 5, label: '其他' }
      ],
      rules: {
        whCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
        whName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
        whType: [{ required: true, message: '请选择仓库类型', trigger: 'change' }]
      }
    };
  },
  created() {
    this.load();
    this.loadManagers();
  },
  methods: {
    hasPerm: hasPerm,
    whTypeLabel(t) {
      const item = this.whTypeOptions.find(o => o.value === t);
      return item ? item.label : '-';
    },
    blankForm() {
      return { id: null, whCode: '', whName: '', whType: 1, managerId: null, region: '', address: '', contact: '', phone: '', status: 1, remark: '' };
    },
    load() {
      this.loading = true;
      Api.get('/api/warehouses/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    loadManagers() {
      Api.get('/api/employees/list-working').then(data => {
        this.managerOptions = data || [];
      }).catch(() => { this.managerOptions = []; });
    },
    onManagerChange(id) {
      const e = this.managerOptions.find(o => o.id === id);
      if (e) {
        if (!this.form.contact) this.form.contact = e.name;
        if (!this.form.phone) this.form.phone = e.mobileMasked || e.mobile || '';
      }
    },
    search() { this.query.pageNum = 1; this.load(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', whType: null, status: null };
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
        id: row.id, whCode: row.whCode, whName: row.whName, whType: row.whType, managerId: row.managerId,
        region: row.region, address: row.address, contact: row.contact, phone: row.phone,
        status: row.status, remark: row.remark
      };
      this.dialog.isEdit = true;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        this.saving = true;
        const req = this.dialog.isEdit ? Api.put('/api/warehouses', this.form) : Api.post('/api/warehouses', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/warehouses/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    doDelete(row) {
      this.$confirm('确认删除仓库「' + row.whName + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/warehouses/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    }
  }
};
