/**
 * 资源维护页面（菜单/页面/按钮/接口树）
 */
window.ResourcePage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="资源编码/名称" clearable style="width:200px" @keyup.enter="load"></el-input>
      <el-select v-model="query.resType" placeholder="资源类型" clearable style="width:120px">
        <el-option label="菜单" :value="1"></el-option>
        <el-option label="页面" :value="2"></el-option>
        <el-option label="按钮" :value="3"></el-option>
        <el-option label="接口" :value="4"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="query={keyword:'',resType:null,status:null};load()">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="hasPerm('B_RES_ADD')" type="primary" @click="openCreate(0)">新增顶级菜单</el-button>
    </div>

    <el-table :data="tree" v-loading="loading" border stripe row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
      <el-table-column prop="resName" label="资源名称" min-width="190"></el-table-column>
      <el-table-column prop="resCode" label="资源编码" width="160"></el-table-column>
      <el-table-column label="类型" width="80" align="center">
        <template #default="scope">
          <el-tag :type="typeTag(scope.row.resType)" size="small">{{ typeLabel(scope.row.resType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" width="130">
        <template #default="scope">{{ scope.row.path || '-' }}</template>
      </el-table-column>
      <el-table-column prop="httpMethod" label="方法" width="90" align="center">
        <template #default="scope">{{ scope.row.httpMethod || '-' }}</template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="70" align="center"></el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" class="tag-status">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPerm('B_RES_ADD')" size="small" link type="primary" @click="openCreate(scope.row.id)">新增子级</el-button>
          <el-button v-if="hasPerm('B_RES_EDIT')" size="small" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('B_RES_STATUS')" size="small" link :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button v-if="hasPerm('B_RES_DELETE')" size="small" link type="danger" @click="doDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="620px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="上级资源">
              <el-tree-select v-model="form.parentId" :data="parentOptions" check-strictly
                :render-after-expand="false" style="width:100%"
                :props="{ label: 'resName', value: 'id', children: 'children' }"
                placeholder="0=顶级（仅菜单）"></el-tree-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="资源类型" prop="resType">
            <el-select v-model="form.resType" style="width:100%" @change="onTypeChange">
              <el-option label="菜单" :value="1"></el-option>
              <el-option label="页面" :value="2"></el-option>
              <el-option label="按钮" :value="3"></el-option>
              <el-option label="接口" :value="4"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资源编码" prop="resCode"><el-input v-model="form.resCode" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资源名称" prop="resName"><el-input v-model="form.resName" maxlength="64"></el-input></el-form-item></el-col>
          <el-col :span="18"><el-form-item label="路径/URL" :prop="pathRequired ? 'path' : ''"><el-input v-model="form.path" maxlength="256" placeholder="如 /company"></el-input></el-form-item></el-col>
          <el-col :span="6"><el-form-item v-if="form.resType === 4" label="方法" prop="httpMethod">
            <el-select v-model="form.httpMethod" style="width:100%">
              <el-option label="GET" value="GET"></el-option>
              <el-option label="POST" value="POST"></el-option>
              <el-option label="PUT" value="PUT"></el-option>
              <el-option label="DELETE" value="DELETE"></el-option>
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排序号"><el-input-number v-model="form.sortNo" :min="0" :max="9999"></el-input-number></el-form-item></el-col>
          <el-col :span="12"><el-form-item v-if="form.resType === 1" label="菜单图标"><el-input v-model="form.icon" maxlength="64" placeholder="Element Plus 图标名"></el-input></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item></el-col>
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
      tree: [],
      loading: false,
      saving: false,
      query: { keyword: '', resType: null, status: null },
      dialog: { visible: false, title: '' },
      form: this.blankForm(),
      parentMap: {},
      rules: {
        resCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
        resName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
        resType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
      }
    };
  },
  computed: {
    pathRequired() {
      return this.form.resType === 1 || this.form.resType === 2 || this.form.resType === 4;
    },
    parentOptions() {
      const filter = (nodes, excludeId) => {
        const out = [];
        (nodes || []).forEach(n => {
          if (n.id === excludeId) return;
          const node = Object.assign({}, n, { children: filter(n.children, excludeId) });
          out.push(node);
        });
        return out;
      };
      return filter(this.tree, this.dialog.visible ? this.form.id : null);
    }
  },
  created() { this.load(); },
  methods: {
    hasPerm: hasPerm,
    typeLabel(t) { return { 1: '菜单', 2: '页面', 3: '按钮', 4: '接口' }[t] || '-'; },
    typeTag(t) { return { 1: '', 2: 'success', 3: 'warning', 4: 'info' }[t] || 'info'; },
    blankForm() {
      return { id: null, resCode: '', resName: '', resType: 1, parentId: 0, path: '', httpMethod: null, sortNo: 0, icon: '', status: 1 };
    },
    load() {
      this.loading = true;
      const params = {};
      if (this.query.keyword) params.keyword = this.query.keyword;
      if (this.query.resType != null) params.resType = this.query.resType;
      if (this.query.status != null) params.status = this.query.status;
      Api.get('/api/resources/tree', params).then(data => {
        this.tree = data || [];
        this.collectParentMap(this.tree);
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    collectParentMap(nodes) {
      (nodes || []).forEach(n => {
        this.parentMap[n.id] = n;
        this.collectParentMap(n.children || []);
      });
    },
    onTypeChange() {
      if (this.form.parentId == null) this.form.parentId = 0;
    },
    openCreate(parentId) {
      const parent = parentId ? this.parentMap[parentId] : null;
      let resType = 1;
      if (parent) {
        if (parent.resType === 1) resType = 2;
        else if (parent.resType === 2) resType = 3;
      }
      this.form = Object.assign(this.blankForm(), { parentId: parentId || 0, resType: resType });
      this.dialog.title = parent ? ('新增子级资源：' + parent.resName) : '新增顶级菜单';
      this.dialog.visible = true;
    },
    openEdit(row) {
      this.form = {
        id: row.id, resCode: row.resCode, resName: row.resName, resType: row.resType,
        parentId: row.parentId, path: row.path || '', httpMethod: row.httpMethod || null,
        sortNo: row.sortNo, icon: row.icon || '', status: row.status
      };
      this.dialog.title = '编辑资源：' + row.resName;
      this.dialog.visible = true;
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        if (this.pathRequired && !this.form.path) {
          this.$message.error('该类型资源必须填写路径/URL');
          return;
        }
        if (this.form.resType === 4 && !this.form.httpMethod) {
          this.$message.error('接口类型资源必须选择方法');
          return;
        }
        if (this.form.parentId == null) this.form.parentId = 0;
        this.saving = true;
        const req = this.form.id ? Api.put('/api/resources', this.form) : Api.post('/api/resources', this.form);
        req.then(() => {
          this.$message.success('保存成功');
          this.dialog.visible = false;
          this.load();
        }).catch(e => this.$message.error(e.message)).finally(() => { this.saving = false; });
      });
    },
    toggleStatus(row) {
      const next = row.status === 1 ? 0 : 1;
      Api.put('/api/resources/' + row.id + '/status', { status: next }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.load();
      }).catch(e => this.$message.error(e.message));
    },
    doDelete(row) {
      this.$confirm('确认删除资源「' + row.resName + '」？', '提示', { type: 'warning' }).then(() => {
        Api.del('/api/resources/' + row.id).then(() => {
          this.$message.success('删除成功');
          this.load();
        }).catch(e => this.$message.error(e.message));
      }).catch(() => {});
    }
  }
};
