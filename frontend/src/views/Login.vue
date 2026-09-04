<template>
  <div class="login-wrap">
    <!-- 极光浮动光斑 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>
    <div class="grid-overlay"></div>

    <div class="login-panel">
      <div class="brand">
        <div class="brand-badge">ERP</div>
        <h1>进销存一体化管理系统</h1>
        <p>ERP Management Platform · 采购 / 销售 / 库存 / 财务 / 报表</p>
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
  position: relative;
  height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #0A1633 0%, #122550 48%, #1B3160 100%);
  overflow: hidden;
}

/* 浮动光斑 */
.orb { position: absolute; border-radius: 50%; filter: blur(70px); opacity: .55; pointer-events: none; }
.orb-1 {
  width: 480px; height: 480px; top: -140px; left: -100px;
  background: radial-gradient(circle, #3B6FF0, transparent 65%);
  animation: drift 14s ease-in-out infinite alternate;
}
.orb-2 {
  width: 420px; height: 420px; bottom: -160px; right: -80px;
  background: radial-gradient(circle, #E8B04B, transparent 65%);
  opacity: .35;
  animation: drift 18s ease-in-out infinite alternate-reverse;
}
.orb-3 {
  width: 300px; height: 300px; top: 40%; left: 60%;
  background: radial-gradient(circle, #6C93F5, transparent 65%);
  opacity: .3;
  animation: drift 11s ease-in-out infinite alternate;
}
@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(60px, 40px) scale(1.12); }
}

/* 网格纹理 */
.grid-overlay {
  position: absolute; inset: 0; pointer-events: none;
  background-image:
    linear-gradient(rgba(255, 255, 255, .04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .04) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(ellipse at center, rgba(0,0,0,.7), transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at center, rgba(0,0,0,.7), transparent 75%);
}

/* 玻璃拟态面板 */
.login-panel {
  position: relative;
  width: 450px; padding: 42px 40px 28px;
  background: rgba(255, 255, 255, .96);
  backdrop-filter: blur(20px);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, .6);
  box-shadow: 0 32px 80px rgba(0, 0, 0, .45), 0 0 0 1px rgba(91, 140, 255, .12);
  animation: panel-in .5s cubic-bezier(.2, .9, .3, 1.2);
}
@keyframes panel-in {
  from { opacity: 0; transform: translateY(24px) scale(.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.brand { text-align: center; margin-bottom: 28px; }
.brand-badge {
  width: 66px; height: 66px; margin: 0 auto 16px;
  border-radius: 19px;
  background: var(--erp-primary-grad);
  color: #fff; font-weight: 800; font-size: 22px; letter-spacing: 1px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12px 28px rgba(59, 111, 240, .5), inset 0 1px 0 rgba(255, 255, 255, .35);
}
.brand h1 { font-size: 21px; color: var(--erp-navy); margin-bottom: 6px; letter-spacing: 1px; }
.brand p { font-size: 12px; color: #8A99B5; letter-spacing: .3px; }

.login-form :deep(.el-input__wrapper) { padding: 4px 14px; }
.login-btn {
  width: 100%; letter-spacing: 8px; font-size: 16px; font-weight: 600;
  margin-top: 8px; height: 46px; border-radius: 10px;
}

.accounts { margin-top: 24px; border-top: 1px dashed #D7E0EE; padding-top: 16px; }
.accounts-title {
  display: flex; align-items: center; gap: 10px;
  font-size: 12px; color: #8AA0BF; margin-bottom: 12px;
  justify-content: center;
}
.accounts-title .line { flex: 1; height: 1px; background: #E7EDF7; }
.account-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.account-chip {
  border: 1px solid #DCE6F5; border-radius: 10px;
  padding: 8px 6px; text-align: center; cursor: pointer;
  transition: all .22s ease; background: #F7FAFF;
}
.account-chip:hover {
  border-color: var(--erp-primary); background: #EEF4FF;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(59, 111, 240, .18);
}
.account-chip b { display: block; font-size: 13px; color: #2E5AA8; }
.account-chip span { font-size: 11px; color: #7A8CA8; }
</style>
