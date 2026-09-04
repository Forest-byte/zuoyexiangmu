<template>
  <div class="login-wrap">
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
        <div class="accounts-title">演示账号</div>
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
  height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background:
    radial-gradient(1200px 600px at 20% 10%, rgba(46,107,230,.25), transparent 60%),
    radial-gradient(900px 500px at 85% 90%, rgba(232,176,75,.15), transparent 55%),
    linear-gradient(135deg, #0F2D52 0%, #16345E 55%, #1E3A6F 100%);
}
.login-panel {
  width: 440px; padding: 40px 38px 28px;
  background: rgba(255,255,255,.97);
  border-radius: 20px; box-shadow: 0 24px 60px rgba(0,0,0,.35);
}
.brand { text-align: center; margin-bottom: 26px; }
.brand-badge {
  width: 64px; height: 64px; margin: 0 auto 14px;
  border-radius: 18px;
  background: linear-gradient(135deg, #2E6BE6, #5A8DF0);
  color: #fff; font-weight: 800; font-size: 22px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 24px rgba(46,107,230,.45);
}
.brand h1 { font-size: 21px; color: #16345E; margin-bottom: 6px; }
.brand p { font-size: 12px; color: #7A8CA8; }
.login-btn { width: 100%; letter-spacing: 8px; font-size: 16px; margin-top: 6px; }
.accounts { margin-top: 22px; border-top: 1px dashed #D7E0EE; padding-top: 16px; }
.accounts-title { font-size: 12px; color: #8AA0BF; margin-bottom: 10px; }
.account-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.account-chip {
  border: 1px solid #DCE6F5; border-radius: 10px;
  padding: 8px 6px; text-align: center; cursor: pointer;
  transition: all .2s; background: #F7FAFF;
}
.account-chip:hover { border-color: var(--erp-primary); background: #EEF4FF; }
.account-chip b { display: block; font-size: 13px; color: #2E5AA8; }
.account-chip span { font-size: 11px; color: #7A8CA8; }
</style>
