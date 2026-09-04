/**
 * 部门信息页面（树形表格）
 */
window.DepartmentPage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="部门名称/编码" clearable style="width:220px" @keyup.enter="load"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="keyword='';load()">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_DEPT_ADD')" type="primary" @click="openCreate">新增部门</el-button>
    </div>

    <el-table :data="tree" v-loading="loading" border stripe row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
      <el-table-column prop="deptName" label="部门名称" min-width="220"></el-table-column>
      <el-table-column prop="deptCode" label="部门编码" width="130"></el-table-column>
      <el-table-column prop="sort" label="排序" width="70" align="center"></el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="tag-status">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('B_DEPT_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_DEPT_STATUS')" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="hasPerm('B_DEPT_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑部门' : '新增部门'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="上级部门">
          <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly
            :render-after-expand="false" style="width:100%"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="不选则为顶级部门"></el-tree-select>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" maxlength="64"></el-input></el-form-item>
        <el-form-item label="部门编码" prop="deptCode"><el-input v-model="form.deptCode" maxlength="32"></el-input></el-form-item>
        <el-form-item label="显示排序"><el-input-number v-model="form.sort" :min="0" :max="9999"></el-input-number></el-form-item>
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
  </div>
  `,
  data() {
    return {
      tree: [],
      loading: false,
      saving: false,
      keyword: '',
      dialog: { visible: false, isEdit: false },
      form: { id: null, parentId: 0, deptName: '', deptCode: '', sort: 0, status: 1 },
      rules: {
        deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
        deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
      }
    };
  },
  computed: {
    parentOptions() {
      // 编辑时移除自身分支，避免将部门挂到自己下面
      const filter = (nodes, excludeId) => {
        const out = [];
        (nodes || []).forEach(n => {
          if (n.id === excludeId) return;
          const node = Object.assign({}, n, { children: filter(n.children, excludeId) });
          out.push(node);
        });
        return out;
      };
      return filter(this.tree, this.dialog.isEdit ? this.form.id : null);
    }
  },
  created() { this.load(); },
  methods: {
    hasPerm: hasPerm,
    load() {
      this.loading = true;
      Api.get('/api/departments/tree', { keyword: this.keyword || undefined, status: undefined })
        .then(data => { this.tree = data || []; })
        .catch(e => this.$message.error(e.message))
        .finally(() => { this.loading = false; });
    },
    openCreate(parentId) {
      this.form = { id: null, parentId: parentId || 0, deptName: '', deptCode: '', sort: 0, status: 1 };
      this.dialog.isEdit = false;
      this.dialog.visible = true;
    },
    openEdit(row) {
      this.form = {
        id: row.id, parentId: row.parentId, deptName: row.deptName,
        deptCode: row.deptCode, sort: row.sort == null ? 0 : row.sort, status: row.status
      };
      this.dialog.isEdit = true;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        if (this.form.parentId == null) this.form.parentId = 0;
        this.saving = true;
        const req = this.dialog.isEdit ? Api.put('/api/departments', this.form) : Api.post('/api/departments', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/departments/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    doDelete(row) {
      this.$confirm('确认删除部门「' + row.deptName + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/departments/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    }
  }
};
