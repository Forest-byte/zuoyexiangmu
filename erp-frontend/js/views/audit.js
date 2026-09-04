/**
 * 审计日志页面
 */
window.AuditPage = {
  template: `
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="模块/操作/操作人/对象类型" clearable style="width:220px" @keyup.enter="search"></el-input>
      <el-input v-model="query.username" placeholder="操作人" clearable style="width:140px" @keyup.enter="search"></el-input>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD HH:mm:ss" range-separator="至"
        start-placeholder="开始时间" end-placeholder="结束时间" style="width:280px"></el-date-picker>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="操作人" width="110">
        <template #default="scope">{{ scope.row.username || '-' }}</template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="100"></el-table-column>
      <el-table-column prop="actionType" label="操作类型" width="110"></el-table-column>
      <el-table-column prop="targetType" label="对象类型" width="110">
        <template #default="scope">{{ scope.row.targetType || '-' }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="对象ID" width="90" align="center">
        <template #default="scope">{{ scope.row.targetId != null ? scope.row.targetId : '-' }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="来源IP" width="140">
        <template #default="scope">{{ scope.row.ip || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="170"></el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="showDetail(scope.row)">明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
        :total="total" :page-size="query.pageSize" :page-sizes="[10, 20, 50]"
        @current-change="onPage" @size-change="onSize"></el-pagination>
    </div>

    <el-dialog v-model="detail.visible" title="审计明细" width="640px">
      <el-descriptions :column="2" border class="detail-desc">
        <el-descriptions-item label="ID">{{ detail.row.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.row.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.row.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detail.row.actionType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ detail.row.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象ID">{{ detail.row.targetId != null ? detail.row.targetId : '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源IP" :span="2">{{ detail.row.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.row.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <h4 v-if="detail.row.afterSnapshot" style="margin-bottom:6px">变更后快照</h4>
      <pre class="snapshot" v-if="detail.row.afterSnapshot">{{ pretty(detail.row.afterSnapshot) }}</pre>
      <h4 v-if="detail.row.beforeSnapshot" style="margin-bottom:6px">变更前快照</h4>
      <pre class="snapshot" v-if="detail.row.beforeSnapshot">{{ pretty(detail.row.beforeSnapshot) }}</pre>
    </el-dialog>
  </div>
  `,
  data() {
    return {
      rows: [],
      total: 0,
      loading: false,
      range: null,
      query: { pageNum: 1, pageSize: 20, keyword: '', username: '', startTime: null, endTime: null },
      detail: { visible: false, row: {} }
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      this.loading = true;
      Api.get('/api/audit/page', this.query).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
        this.$message.success('查询完成，共 ' + (data.total || 0) + ' 条');
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    buildParams() {
      const p = Object.assign({}, this.query);
      if (this.range && this.range.length === 2) {
        p.startTime = this.range[0];
        p.endTime = this.range[1];
      } else {
        delete p.startTime;
        delete p.endTime;
      }
      return p;
    },
    fetch() {
      this.loading = true;
      Api.get('/api/audit/page', this.buildParams()).then(data => {
        this.rows = data.list || [];
        this.total = data.total || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => { this.loading = false; });
    },
    search() { this.query.pageNum = 1; this.fetch(); },
    reset() {
      this.query = { pageNum: 1, pageSize: 20, keyword: '', username: '', startTime: null, endTime: null };
      this.range = null;
      this.fetch();
    },
    onPage(p) { this.query.pageNum = p; this.fetch(); },
    onSize(s) { this.query.pageSize = s; this.query.pageNum = 1; this.fetch(); },
    showDetail(row) {
      this.detail.row = row;
      this.detail.visible = true;
    },
    pretty(s) {
      if (!s) return '';
      try {
        return JSON.stringify(JSON.parse(s), null, 2);
      } catch (e) {
        return s;
      }
    }
  }
};
