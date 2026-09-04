/**
 * 应用入口：登录页 + 主布局（侧边菜单/顶栏）+ Hash 路由 + RBAC 页面守卫
 * 路由表：hash 路径 -> 组件名（与后端资源 path 一一对应）
 */
(function () {
  const ROUTES = {
    '/company': 'CompanyPage',
    '/department': 'DepartmentPage',
    '/employee': 'EmployeePage',
    '/role': 'RolePage',
    '/resource': 'ResourcePage',
    '/warehouse': 'WarehousePage',
    '/user': 'UserPage',
    '/role-grant': 'RoleGrantPage',
    '/audit': 'AuditPage'
  };

  const App = Vue.createApp({
    template: `
    <div>
      <div v-if="!authed" class="login-wrap">
        <div class="login-card">
          <div class="login-title">企业ERP管理系统</div>
          <el-form :model="login" :rules="loginRules" ref="loginRef" @keyup.enter="doLogin">
            <el-form-item prop="username">
              <el-input v-model="login.username" placeholder="登录名" prefix-icon="User" size="large"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="login.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password></el-input>
            </el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="logging" @click="doLogin">登 录</el-button>
          </el-form>
          <div class="login-tip">
            内置账号：admin / zhangsan / lisi<br>默认密码：Admin123456
          </div>
        </div>
      </div>

      <div v-else class="layout" style="height:100vh;display:flex;flex-direction:column">
        <el-container style="height:100%">
          <el-aside width="220px" class="app-sider">
            <el-menu :default-active="activePath" @select="onMenuSelect" style="border-right:none">
              <el-sub-menu v-for="m in topMenus" :key="m.id" :index="'m' + m.id">
                <template #title>
                  <el-icon v-if="m.icon"><component :is="iconOf(m.icon)"></component></el-icon>
                  <span>{{ m.resName }}</span>
                </template>
                <el-menu-item v-for="c in pageChildren(m)" :key="c.id" :index="c.path">
                  <el-icon v-if="c.icon"><component :is="iconOf(c.icon)"></component></el-icon>
                  <span>{{ c.resName }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-for="m in topPageMenus" :key="m.id" :index="m.path">
                <el-icon v-if="m.icon"><component :is="iconOf(m.icon)"></component></el-icon>
                <span>{{ m.resName }}</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <el-container>
            <el-header class="app-header">
              <div class="brand">企业ERP管理系统</div>
              <div class="user-area">
                <span>你好，{{ AppStore.user.username }}</span>
                <el-tag v-if="AppStore.user.superAdmin" size="small" type="danger">超级管理员</el-tag>
                <el-dropdown @command="onUserMenu">
                  <span style="color:#fff;cursor:pointer;font-size:14px">▾ 个人中心</span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="password">修改密码</el-dropdown-item>
                      <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </el-header>
            <el-main class="app-main">
              <component v-if="currentView" :is="currentView"></component>
              <el-empty v-else description="无可访问页面" />
            </el-main>
          </el-container>
        </el-container>
      </div>

      <el-dialog v-model="pwdDialog.visible" title="修改密码" width="420px">
        <el-form :model="pwdDialog.form" :rules="pwdRules" ref="pwdRef" label-width="100px">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwdDialog.form.oldPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdDialog.form.newPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirm">
            <el-input v-model="pwdDialog.form.confirm" type="password" show-password></el-input>
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
        authed: false,
        logging: false,
        activePath: '/',
        pwdSaving: false,
        login: { username: '', password: '' },
        loginRules: {
          username: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
          password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
        },
        pwdDialog: { visible: false, form: { oldPassword: '', newPassword: '', confirm: '' } },
        pwdRules: {
          oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
          newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
          confirm: [{
            validator: (rule, value, cb) => {
              if (!value) return cb(new Error('请再次输入新密码'));
              if (value !== this.pwdDialog.form.newPassword) return cb(new Error('两次输入的新密码不一致'));
              cb();
            }, trigger: 'blur'
          }]
        }
      };
    },
    computed: {
      currentView() {
        if (!this.authed || !AppStore.loaded) return null;
        return ROUTES[this.activePath] || ROUTES[this.pathName()] || null;
      },
      topMenus() {
        return (AppStore.menus || []).filter(m => m.resType === 1);
      },
      topPageMenus() {
        return (AppStore.menus || []).filter(m => m.resType === 2);
      }
    },
    created() {
      this.bootstrap();
      window.addEventListener('hashchange', () => {
        this.syncPath();
      });
    },
    methods: {
      bootstrap() {
        // 根路径始终展示登录界面（显式登录，不自动跳转进系统）
        if (this.pathName() === '/') {
          this.authed = false;
          return;
        }
        if (AppStore.token) {
          this.loadMe();
        } else {
          this.authed = false;
        }
      },
      loadMe() {
        Api.get('/api/auth/me').then(user => {
          AppStore.user = user;
          AppStore.menus = user.menus || [];
          AppStore.permissions = user.permissions;
          AppStore.loaded = true;
          this.authed = true;
          this.guard();
        }).catch(() => {
          this.onLogout(false);
        });
      },
      doLogin() {
        this.$refs.loginRef.validate(valid => {
          if (!valid) return;
          this.logging = true;
          Api.post('/api/auth/login', {
            username: this.login.username.trim(),
            password: this.login.password
          }).then(data => {
            localStorage.setItem('erp_token', data.token);
            AppStore.token = data.token;
            this.loadMe();
          }).catch(e => {
            this.$message.error(e.message || '登录失败');
          }).finally(() => { this.logging = false; });
        });
      },
      /** 路由守卫：按菜单可见性校验当前 hash 指向的页面 */
      guard() {
        const paths = collectPaths(AppStore.menus, []);
        const name = this.pathName();
        if (name && paths.indexOf(name) >= 0) {
          this.activePath = name;
          return;
        }
        const allowedPaths = Object.keys(ROUTES).filter(p => paths.indexOf(p) >= 0);
        if (allowedPaths.length > 0) {
          location.hash = '#' + allowedPaths[0];
          return;
        }
        this.activePath = '/';
      },
      syncPath() {
        // 回到根路径时展示登录界面
        if (this.pathName() === '/') {
          this.authed = false;
          this.activePath = '/';
          return;
        }
        if (!this.authed) return;
        const name = this.pathName();
        if (name && ROUTES[name]) {
          this.guard();
        }
      },
      pathName() {
        let h = location.hash || '#/';
        h = h.replace(/^#/, '');
        const q = h.indexOf('?');
        if (q >= 0) h = h.substring(0, q);
        return h || '/';
      },
      pageChildren(m) {
        return (m.children || []).filter(c => c.resType === 2 && c.path);
      },
      onMenuSelect(index) {
        if (index && index.indexOf('/') === 0) {
          location.hash = '#' + index;
        }
      },
      iconOf(name) {
        const icons = window.ElementPlusIconsVue || {};
        return icons[name] || null;
      },
      onUserMenu(cmd) {
        if (cmd === 'logout') {
          this.onLogout(true);
        } else if (cmd === 'password') {
          this.pwdDialog.form = { oldPassword: '', newPassword: '', confirm: '' };
          this.pwdDialog.visible = true;
        }
      },
      savePwd() {
        this.$refs.pwdRef.validate(valid => {
          if (!valid) return;
          this.pwdSaving = true;
          Api.post('/api/auth/password', {
            oldPassword: this.pwdDialog.form.oldPassword,
            newPassword: this.pwdDialog.form.newPassword
          }).then(() => {
            this.$message.success('密码修改成功，请重新登录');
            this.pwdDialog.visible = false;
            this.onLogout(false);
          }).catch(e => this.$message.error(e.message)).finally(() => { this.pwdSaving = false; });
        });
      },
      onLogout(remote) {
        if (remote) {
          Api.post('/api/auth/logout').catch(() => {});
        }
        localStorage.removeItem('erp_token');
        AppStore.token = '';
        AppStore.user = null;
        AppStore.menus = [];
        AppStore.permissions = null;
        AppStore.loaded = false;
        this.authed = false;
        this.activePath = '/';
        location.hash = '#/login';
      }
    }
  });

  // 全局组件注册
  for (const key in window) {
    if (/Page$/.test(key)) {
      App.component(key, window[key]);
    }
  }
  for (const key in (window.ElementPlusIconsVue || {})) {
    App.component(key, window.ElementPlusIconsVue[key]);
  }

  App.use(ElementPlus);
  App.mount('#app');
})();
