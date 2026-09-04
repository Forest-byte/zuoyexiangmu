<template>
  <div class="layout">
    <!-- 侧边栏：深蓝渐变 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="logo">
        <div class="logo-badge">ERP</div>
        <div v-if="!collapsed" class="logo-text">
          <div class="logo-title">进销存一体化系统</div>
          <div class="logo-sub">ERP Management Platform</div>
        </div>
      </div>
      <nav class="nav">
        <div v-for="m in menus" :key="m.path" class="nav-item">
          <router-link :to="m.path" class="nav-link" :class="{ active: $route.path === m.path }">
            <el-icon class="nav-icon"><component :is="m.icon" /></el-icon>
            <span v-if="!collapsed">{{ m.title }}</span>
          </router-link>
        </div>
      </nav>
      <div class="collapse-btn" @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <span class="crumb">{{ currentTitle }}</span>
        </div>
        <div class="topbar-right">
          <span class="welcome">欢迎，{{ user?.name || user?.username }}</span>
          <el-dropdown @command="onCommand">
            <span class="avatar">{{ avatarText }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>

    <!-- 修改密码 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="changePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const collapsed = ref(false)
const pwdVisible = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '' })

const user = computed(() => store.user)

const menus = [
  { path: '/home', title: '工作台', icon: 'Odometer' },
  { path: '/base', title: '基础维护', icon: 'OfficeBuilding' },
  { path: '/permission', title: '角色权限', icon: 'Lock' },
  { path: '/rules', title: '公共规则', icon: 'SetUp' },
  { path: '/crm', title: 'CRM管理', icon: 'User' },
  { path: '/inventory', title: '进销存', icon: 'ShoppingCart' },
  { path: '/warehouse', title: '仓储管理', icon: 'Box' },
  { path: '/reports', title: '业务报表', icon: 'DataAnalysis' },
  { path: '/jobs', title: '定时任务', icon: 'AlarmClock' },
  { path: '/finance', title: '财务管理', icon: 'Money' }
]

const titleMap = Object.fromEntries(menus.map(m => [m.path, m.title]))
const currentTitle = computed(() => titleMap[route.path] || '工作台')
const avatarText = computed(() => {
  const n = user.value?.name || user.value?.username || 'U'
  return n.charAt(0).toUpperCase()
})

onMounted(async () => {
  try {
    await store.fetchInfo()
  } catch (e) { /* 已由拦截器处理 */ }
})

async function onCommand(cmd) {
  if (cmd === 'logout') {
    store.logout()
    router.push('/login')
  } else if (cmd === 'password') {
    pwdForm.value = { oldPassword: '', newPassword: '' }
    pwdVisible.value = true
  }
}

async function changePwd() {
  try {
    await request.post('/auth/password', pwdForm.value)
    ElMessage.success('密码修改成功，请重新登录')
    store.logout()
    router.push('/login')
  } catch (e) { /* 已处理 */ }
}
</script>

<style scoped>
.layout { display: flex; height: 100vh; overflow: hidden; }
.sidebar {
  width: 232px;
  background: var(--erp-sidebar-grad);
  color: #EAF1FB;
  display: flex;
  flex-direction: column;
  transition: width .25s;
  flex-shrink: 0;
}
.sidebar.collapsed { width: 64px; }
.logo {
  display: flex; align-items: center; gap: 12px;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.logo-badge {
  width: 40px; height: 40px; flex-shrink: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #2E6BE6, #5A8DF0);
  color: #fff; font-weight: 800; font-size: 15px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(46,107,230,.4);
}
.logo-title { font-size: 15px; font-weight: 700; color: #fff; white-space: nowrap; }
.logo-sub { font-size: 10px; color: #9FB6D9; white-space: nowrap; }
.nav { flex: 1; padding: 12px 10px; overflow-y: auto; }
.nav-item { margin-bottom: 4px; }
.nav-link {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 14px; border-radius: 10px;
  color: #C6D4EA; text-decoration: none; font-size: 14px;
  transition: all .2s;
}
.nav-link:hover { background: rgba(255,255,255,.08); color: #fff; }
.nav-link.active {
  background: linear-gradient(90deg, #2E6BE6, #4A82EE);
  color: #fff; font-weight: 600;
  box-shadow: 0 4px 14px rgba(46,107,230,.35);
}
.nav-icon { font-size: 18px; }
.collapse-btn {
  padding: 14px; text-align: center; cursor: pointer;
  border-top: 1px solid rgba(255,255,255,.08); color: #9FB6D9;
}
.collapse-btn:hover { color: #fff; }
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar {
  height: 60px; background: #fff;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; box-shadow: 0 2px 10px rgba(15,45,82,.06); z-index: 5;
}
.crumb { font-size: 16px; font-weight: 600; color: #16345E; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.welcome { color: #5A6B85; font-size: 14px; }
.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: var(--erp-header-grad); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; cursor: pointer; font-size: 16px;
}
.content { flex: 1; overflow-y: auto; padding: 20px 24px; }
</style>
