<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-glow"></div>
      <div class="logo">
        <div class="logo-badge">ERP</div>
        <div v-if="!collapsed" class="logo-text">
          <div class="logo-title">进销存一体化系统</div>
          <div class="logo-sub">ERP Management Platform</div>
        </div>
      </div>
      <nav class="nav">
        <div v-for="m in menus" :key="m.path" class="nav-item">
          <router-link :to="m.path" class="nav-link" :class="{ active: $route.path === m.path }" :title="collapsed ? m.title : ''">
            <span class="nav-indicator"></span>
            <el-icon class="nav-icon"><component :is="m.icon" /></el-icon>
            <span v-if="!collapsed" class="nav-text">{{ m.title }}</span>
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
          <el-icon class="topbar-icon"><component :is="currentIcon" /></el-icon>
          <span class="crumb">{{ currentTitle }}</span>
        </div>
        <div class="topbar-right">
          <span class="topbar-date">{{ today }}</span>
          <div class="topbar-divider"></div>
          <span class="welcome">欢迎，{{ user?.name || user?.username }}</span>
          <el-dropdown @command="onCommand">
            <div class="avatar-wrap">
              <span class="avatar">{{ avatarText }}</span>
              <el-icon class="avatar-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Key /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
const currentIcon = computed(() => (menus.find(m => m.path === route.path) || menus[0]).icon)
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
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

/* ===== 侧边栏 ===== */
.sidebar {
  position: relative;
  width: 236px;
  background: var(--erp-sidebar-grad);
  color: #EAF1FB;
  display: flex;
  flex-direction: column;
  transition: width .28s cubic-bezier(.4, 0, .2, 1);
  flex-shrink: 0;
  overflow: hidden;
  z-index: 10;
  box-shadow: 4px 0 24px rgba(13, 27, 58, .18);
}
.sidebar.collapsed { width: 68px; }
.sidebar-glow {
  position: absolute; top: -120px; left: -80px;
  width: 280px; height: 280px; border-radius: 50%;
  background: radial-gradient(circle, rgba(91, 140, 255, .28), transparent 65%);
  pointer-events: none;
}
.logo {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, .07);
  position: relative;
}
.logo-badge {
  width: 42px; height: 42px; flex-shrink: 0;
  border-radius: 13px;
  background: var(--erp-primary-grad);
  color: #fff; font-weight: 800; font-size: 15px; letter-spacing: .5px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 16px rgba(59, 111, 240, .5), inset 0 1px 0 rgba(255, 255, 255, .3);
}
.logo-title { font-size: 15px; font-weight: 700; color: #fff; white-space: nowrap; letter-spacing: .5px; }
.logo-sub { font-size: 10px; color: #8FA6CC; white-space: nowrap; margin-top: 2px; }

.nav { flex: 1; padding: 14px 12px; overflow-y: auto; position: relative; }
.nav::-webkit-scrollbar { width: 4px; }
.nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, .15); }
.nav-item { margin-bottom: 5px; }
.nav-link {
  position: relative;
  display: flex; align-items: center; gap: 12px;
  padding: 11px 14px; border-radius: 11px;
  color: #B9C8E4; text-decoration: none; font-size: 14px;
  transition: all .22s ease;
  overflow: hidden;
}
.nav-indicator {
  position: absolute; left: 0; top: 50%; transform: translateY(-50%) scaleY(0);
  width: 3px; height: 20px; border-radius: 0 3px 3px 0;
  background: #7EA4FF;
  transition: transform .22s ease;
}
.nav-link:hover { background: rgba(255, 255, 255, .08); color: #fff; }
.nav-link.active {
  background: linear-gradient(90deg, rgba(59, 111, 240, .95), rgba(91, 140, 255, .75));
  color: #fff; font-weight: 600;
  box-shadow: 0 6px 18px rgba(38, 84, 210, .45), inset 0 1px 0 rgba(255, 255, 255, .18);
}
.nav-link.active .nav-indicator { transform: translateY(-50%) scaleY(1); }
.nav-icon { font-size: 18px; flex-shrink: 0; }
.nav-text { white-space: nowrap; }

.collapse-btn {
  padding: 14px; text-align: center; cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, .07); color: #8FA6CC;
  transition: color .2s, background .2s;
}
.collapse-btn:hover { color: #fff; background: rgba(255, 255, 255, .06); }

/* ===== 主区域 ===== */
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar {
  height: 62px;
  background: rgba(255, 255, 255, .85);
  backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 26px;
  border-bottom: 1px solid rgba(229, 234, 243, .9);
  z-index: 5;
}
.topbar-left { display: flex; align-items: center; gap: 10px; }
.topbar-icon {
  font-size: 18px; color: var(--erp-primary);
  background: var(--el-color-primary-light-9);
  padding: 7px; border-radius: 9px;
  width: 32px; height: 32px; box-sizing: border-box;
}
.crumb { font-size: 16px; font-weight: 700; color: var(--erp-navy); letter-spacing: .5px; }
.topbar-right { display: flex; align-items: center; gap: 14px; }
.topbar-date { color: #8A99B5; font-size: 13px; }
.topbar-divider { width: 1px; height: 20px; background: #E5EAF3; }
.welcome { color: #54647E; font-size: 14px; }
.avatar-wrap { display: flex; align-items: center; gap: 6px; cursor: pointer; outline: none; }
.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: var(--erp-primary-grad); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px;
  box-shadow: 0 4px 12px rgba(59, 111, 240, .35), inset 0 1px 0 rgba(255, 255, 255, .3);
  transition: transform .2s ease;
}
.avatar-wrap:hover .avatar { transform: scale(1.06); }
.avatar-arrow { font-size: 12px; color: #8A99B5; }
.content { flex: 1; overflow-y: auto; padding: 22px 26px; }
</style>
