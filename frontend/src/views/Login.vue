<template>
  <div class="login-wrap">
    <!-- 左侧品牌区 -->
    <div class="brand-side">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="grid-overlay"></div>
      <div class="brand-inner">
        <div class="brand-logo">
          <div class="brand-badge"><el-icon :size="30"><Coin /></el-icon></div>
          <span class="brand-name">ERP Platform</span>
        </div>
        <h1 class="brand-slogan">进销存<br />一体化管理系统</h1>
        <p class="brand-desc">采购 · 销售 · 库存 · 财务 · 报表<br />七大子系统，全流程业务闭环</p>
        <div class="brand-features">
          <div v-for="f in features" :key="f" class="feature-chip">
            <span class="feature-dot"></span>{{ f }}
          </div>
        </div>
      </div>
      <div class="brand-footer">© 2026 ERP Management Platform</div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-side">
      <div class="form-panel">
        <div class="form-head">
          <h2>欢迎回来</h2>
          <p>请登录您的账号以继续</p>
        </div>
        <el-form :model="form" class="login-form" @keyup.enter="doLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="'User'" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="'Lock'" show-password />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="doLogin">登 录</el-button>
        </el-form>
        <div class="accounts">
          <div class="accounts-title">
            <span class="line"></span>演示账号 · 点击快速填充<span class="line"></span>
          </div>
          <div class="account-grid">
            <div v-for="a in accounts" :key="a.u" class="account-chip" @click="fill(a)">
              <b>{{ a.name }}</b><span>{{ a.u }} / {{ a.p }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const store = useUserStore()
const form = ref({ username: 'admin', password: 'admin123' })
const loading = ref(false)

const features = ['采购管理', '销售管理', '智能仓储', '财务结算', '业务报表', '权限管控']

const accounts = [
  { name: '管理员', u: 'admin', p: 'admin123' },
  { name: '采购员', u: 'purchase', p: '123456' },
  { name: '销售员', u: 'saler', p: '123456' },
  { name: '库管', u: 'warehouse', p: '123456' },
  { name: '财务', u: 'finance', p: '123456' }
]

function fill(a) {
  form.value.username = a.u
  form.value.password = a.p
}

async function doLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await store.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) { /* 已处理 */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  height: 100vh;
  background: #080C14;
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.brand-side {
  position: relative;
  flex: 1.15;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 52px 60px 36px;
  background:
    radial-gradient(1000px 600px at 20% 0%, rgba(59, 130, 246, .14), transparent 55%),
    radial-gradient(700px 500px at 90% 100%, rgba(245, 158, 11, .08), transparent 55%),
    linear-gradient(160deg, #0C1322 0%, #090E1A 100%);
  border-right: 1px solid rgba(255, 255, 255, .06);
  overflow: hidden;
}
.orb { position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none; }
.orb-1 {
  width: 460px; height: 460px; top: -140px; right: -80px;
  background: radial-gradient(circle, rgba(59, 130, 246, .32), transparent 65%);
  animation: drift 15s ease-in-out infinite alternate;
}
.orb-2 {
  width: 380px; height: 380px; bottom: -140px; left: -60px;
  background: radial-gradient(circle, rgba(245, 158, 11, .20), transparent 65%);
  animation: drift 19s ease-in-out infinite alternate-reverse;
}
@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(-50px, 50px) scale(1.15); }
}
.grid-overlay {
  position: absolute; inset: 0; pointer-events: none;
  background-image:
    linear-gradient(rgba(255, 255, 255, .03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .03) 1px, transparent 1px);
  background-size: 46px 46px;
  mask-image: radial-gradient(ellipse at 30% 40%, rgba(0,0,0,.8), transparent 78%);
  -webkit-mask-image: radial-gradient(ellipse at 30% 40%, rgba(0,0,0,.8), transparent 78%);
}

.brand-inner { position: relative; animation: rise .6s ease both; }
.brand-logo { display: flex; align-items: center; gap: 14px; margin-bottom: 72px; }
.brand-badge {
  width: 52px; height: 52px;
  border-radius: 15px;
  background: var(--erp-primary-grad);
  color: #081226;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 28px rgba(59, 130, 246, .5), inset 0 1px 0 rgba(255, 255, 255, .35);
}
.brand-name { font-size: 15px; font-weight: 700; color: #DCE5F3; letter-spacing: 2px; }
.brand-slogan {
  font-size: 46px; font-weight: 800; line-height: 1.25;
  color: #F0F5FC; letter-spacing: 4px; margin-bottom: 22px;
}
.brand-desc { font-size: 14px; color: #7E8FA8; line-height: 1.9; letter-spacing: 2px; margin-bottom: 44px; }
.brand-features { display: flex; flex-wrap: wrap; gap: 10px; max-width: 420px; }
.feature-chip {
  display: flex; align-items: center; gap: 8px;
  font-size: 12.5px; color: #A8B6CC; letter-spacing: 1px;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, .08);
  border-radius: 999px;
  background: rgba(255, 255, 255, .03);
  transition: all .25s ease;
}
.feature-chip:hover {
  border-color: rgba(59, 130, 246, .4);
  color: #3B82F6;
  background: rgba(59, 130, 246, .07);
}
.feature-dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: #3B82F6; box-shadow: 0 0 8px rgba(59, 130, 246, .9);
}
.brand-footer { position: relative; font-size: 11.5px; color: #46536B; letter-spacing: 1px; }

/* ===== 右侧表单区 ===== */
.form-side {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  padding: 40px;
  background: #080C14;
}
.form-panel {
  width: 400px;
  animation: rise .6s .1s ease both;
}
@keyframes rise {
  from { opacity: 0; transform: translateY(26px); }
  to { opacity: 1; transform: translateY(0); }
}
.form-head { margin-bottom: 34px; }
.form-head h2 { font-size: 30px; font-weight: 800; color: #F0F5FC; letter-spacing: 2px; margin-bottom: 10px; }
.form-head p { font-size: 13.5px; color: #64748F; letter-spacing: 1px; }

.login-form :deep(.el-input__wrapper) { padding: 5px 15px; }
.login-form :deep(.el-input__inner) { letter-spacing: .5px; }
.login-btn {
  width: 100%; letter-spacing: 10px; font-size: 16px; font-weight: 700;
  margin-top: 10px; height: 48px; border-radius: 10px;
}

.accounts { margin-top: 34px; border-top: 1px dashed rgba(255, 255, 255, .1); padding-top: 20px; }
.accounts-title {
  display: flex; align-items: center; gap: 10px;
  font-size: 12px; color: #64748F; margin-bottom: 14px;
  justify-content: center; letter-spacing: 1px;
}
.accounts-title .line { flex: 1; height: 1px; background: rgba(255, 255, 255, .07); }
.account-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 9px; }
.account-chip {
  border: 1px solid rgba(255, 255, 255, .08);
  border-radius: 11px;
  padding: 9px 6px; text-align: center; cursor: pointer;
  transition: all .22s ease;
  background: rgba(255, 255, 255, .025);
}
.account-chip:hover {
  border-color: rgba(59, 130, 246, .45);
  background: rgba(59, 130, 246, .08);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, .35), 0 0 14px rgba(59, 130, 246, .15);
}
.account-chip b { display: block; font-size: 13px; color: #3B82F6; }
.account-chip span { font-size: 11px; color: #64748F; }

@media (max-width: 900px) {
  .brand-side { display: none; }
}
</style>
