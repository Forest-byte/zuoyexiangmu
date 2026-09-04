<template>
  <div class="layout">
    <!-- 浮动侧边栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-glow"></div>
      <div class="logo">
        <div class="logo-badge"><el-icon :size="20"><Coin /></el-icon></div>
        <div v-if="!collapsed" class="logo-text">
          <div class="logo-title">进销存一体化系统</div>
          <div class="logo-sub">ERP MANAGEMENT PLATFORM</div>
        </div>
      </div>
      <nav class="nav">
        <div v-for="m in menus" :key="m.path" class="nav-item">
          <router-link :to="m.path" class="nav-link" :class="{ active: $route.path === m.path }" :title="collapsed ? m.title : ''">
            <el-icon class="nav-icon"><component :is="m.icon" /></el-icon>
            <span v-if="!collapsed" class="nav-text">{{ m.title }}</span>
            <span v-if="!collapsed" class="nav-dot"></span>
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
          <div>
            <div class="crumb">{{ currentTitle }}</div>
            <div class="crumb-sub">{{ today }}</div>
          </div>
        </div>
        <div class="topbar-right">
          <span class="welcome">欢迎，{{ user?.name || user?.username }}</span>
          <el-dropdown @command="onCommand">
            <div class="avatar-wrap">
              <span class="avatar">{{ avatarText }}</span>
              <span class="avatar-ring"></span>
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
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  padding: 14px;
  gap: 14px;
}

/* ===== 浮动侧边栏 ===== */
.sidebar {
  position: relative;
  width: 224px;
  background: var(--erp-sidebar-grad);
  border: 1px solid rgba(255, 255, 255, .06);
  border-radius: 20px;
  color: #C9D8D1;
  display: flex;
  flex-direction: column;
  transition: width .28s cubic-bezier(.4, 0, .2, 1);
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, .45);
}
.sidebar.collapsed { width: 72px; }
.sidebar-glow {
  position: absolute; top: -100px; left: 50%; transform: translateX(-50%);
  width: 260px; height: 220px; border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, .16), transparent 65%);
  pointer-events: none;
}
.logo {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, .05);
  position: relative;
}
.logo-badge {
  width: 42px; height: 42px; flex-shrink: 0;
  border-radius: 13px;
  background: var(--erp-primary-grad);
  color: #081226;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 20px rgba(59, 130, 246, .45), inset 0 1px 0 rgba(255, 255, 255, .35);
}
.logo-title { font-size: 14.5px; font-weight: 700; color: #EDF2FA; white-space: nowrap; letter-spacing: .5px; }
.logo-sub { font-size: 9px; color: #5B6B85; white-space: nowrap; margin-top: 3px; letter-spacing: 1.5px; }

.nav { flex: 1; padding: 14px 12px; overflow-y: auto; position: relative; }
.nav::-webkit-scrollbar { width: 4px; }
.nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, .1); }
.nav-item { margin-bottom: 4px; }
.nav-link {
  position: relative;
  display: flex; align-items: center; gap: 12px;
  padding: 11px 13px; border-radius: 12px;
  color: #7E8FA8; text-decoration: none; font-size: 14px;
  transition: all .22s ease;
}
.nav-dot {
  margin-left: auto;
  width: 6px; height: 6px; border-radius: 50%;
  background: transparent;
  transition: all .22s ease;
}
.nav-link:hover { background: rgba(255, 255, 255, .045); color: #DCE5F3; }
.nav-link.active {
  background: linear-gradient(90deg, rgba(59, 130, 246, .18), rgba(59, 130, 246, .06));
  color: #3B82F6; font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, .28), 0 0 18px rgba(59, 130, 246, .10);
}
.nav-link.active .nav-dot { background: #3B82F6; box-shadow: 0 0 8px rgba(59, 130, 246, .8); }
.nav-icon { font-size: 18px; flex-shrink: 0; }
.nav-text { white-space: nowrap; }

.collapse-btn {
  padding: 14px; text-align: center; cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, .05); color: #5B6B85;
  transition: color .2s, background .2s;
}
.collapse-btn:hover { color: #DCE5F3; background: rgba(255, 255, 255, .04); }

/* ===== 主区域 ===== */
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; gap: 14px; }

/* 悬浮玻璃顶栏 */
.topbar {
  height: 64px;
  background: rgba(20, 30, 25, .72);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, .06);
  border-radius: 18px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 22px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, .35);
  flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; gap: 12px; }
.topbar-icon {
  font-size: 17px; color: #3B82F6;
  background: rgba(59, 130, 246, .12);
  border: 1px solid rgba(59, 130, 246, .25);
  padding: 8px; border-radius: 11px;
  width: 34px; height: 34px; box-sizing: border-box;
  box-shadow: 0 0 14px rgba(59, 130, 246, .15);
}
.crumb { font-size: 15.5px; font-weight: 700; color: #EDF2FA; letter-spacing: .5px; }
.crumb-sub { font-size: 11.5px; color: #64748F; margin-top: 2px; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.welcome { color: #8A9BB5; font-size: 13.5px; }
.avatar-wrap { position: relative; cursor: pointer; outline: none; }
.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: var(--erp-primary-grad); color: #081226;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 16px;
  box-shadow: 0 0 16px rgba(59, 130, 246, .35), inset 0 1px 0 rgba(255, 255, 255, .35);
  transition: transform .2s ease;
}
.avatar-wrap:hover .avatar { transform: scale(1.07); }
.avatar-ring {
  position: absolute; right: -1px; bottom: -1px;
  width: 11px; height: 11px; border-radius: 50%;
  background: #3B82F6;
  border: 2.5px solid #0E1626;
  box-shadow: 0 0 8px rgba(59, 130, 246, .8);
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 4px 4px 8px;
}
</style>
