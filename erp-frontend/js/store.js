/**
 * 全局状态：登录 token / 用户信息 / 可见菜单 / 功能权限
 */
(function () {
  const AppStore = Vue.reactive({
    token: localStorage.getItem('erp_token') || '',
    user: null,          // UserInfoVO
    menus: [],           // 可见菜单树（菜单+页面）
    permissions: null,   // 功能权限编码集合，null=全部（超管）
    loaded: false        // 是否已加载用户信息
  });

  /** 功能权限判断：超管拥有全部；can() 无 code 恒真（页面级展示） */
  function hasPerm(code) {
    if (!code) return true;
    const u = AppStore.user;
    if (u && u.superAdmin) return true;
    const perms = AppStore.permissions;
    if (!perms) return false;
    return perms.indexOf(code) >= 0;
  }

  /** 将后端菜单树扁平化收集页面路径集合（用于路由守卫） */
  function collectPaths(nodes, acc) {
    acc = acc || [];
    (nodes || []).forEach(function (n) {
      if (n.path && n.path.indexOf('/') === 0) acc.push(n.path);
      collectPaths(n.children || [], acc);
    });
    return acc;
  }

  window.AppStore = AppStore;
  window.hasPerm = hasPerm;
  window.collectPaths = collectPaths;
})();
