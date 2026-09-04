import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'base', name: 'Base', component: () => import('@/views/base/BaseMain.vue') },
      { path: 'permission', name: 'Permission', component: () => import('@/views/permission/PermissionMain.vue') },
      { path: 'rules', name: 'Rules', component: () => import('@/views/rules/RulesMain.vue') },
      { path: 'crm', name: 'Crm', component: () => import('@/views/crm/CrmMain.vue') },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/inventory/InventoryMain.vue') },
      { path: 'warehouse', name: 'Warehouse', component: () => import('@/views/warehouse/WarehouseMain.vue') },
      { path: 'reports', name: 'Reports', component: () => import('@/views/reports/ReportsMain.vue') },
      { path: 'jobs', name: 'Jobs', component: () => import('@/views/jobs/JobsMain.vue') },
      { path: 'finance', name: 'Finance', component: () => import('@/views/finance/FinanceMain.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('erp_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
