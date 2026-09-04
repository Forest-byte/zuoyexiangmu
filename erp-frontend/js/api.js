/**
 * API 封装：统一 axios 实例 + 响应处理（Result {code,msg,data}）
 * 由后端 same-origin 伺服，baseURL 为空即可。
 */
(function () {
  const instance = axios.create({
    baseURL: '',
    timeout: 20000
  });

  function goLogin() {
    localStorage.removeItem('erp_token');
    if (location.hash !== '#/login') {
      location.hash = '#/login';
    }
  }

  instance.interceptors.request.use(function (config) {
    const token = localStorage.getItem('erp_token');
    if (token) {
      config.headers.Authorization = 'Bearer ' + token;
    }
    return config;
  });

  instance.interceptors.response.use(
    function (res) {
      const body = res.data;
      if (body && typeof body.code !== 'undefined') {
        if (body.code === 200) {
          return body.data;
        }
        if (body.code === 401 || res.status === 401) {
          goLogin();
          return Promise.reject(new Error(body.msg || '登录已过期'));
        }
        return Promise.reject(new Error(body.msg || '操作失败'));
      }
      return body;
    },
    function (err) {
      const status = err.response && err.response.status;
      const body = err.response && err.response.data;
      if (status === 401) {
        goLogin();
      }
      return Promise.reject(new Error((body && body.msg) || '网络错误，请稍后重试'));
    }
  );

  window.Api = {
    get: function (url, params) { return instance.get(url, { params: params }); },
    post: function (url, data) { return instance.post(url, data); },
    put: function (url, data) { return instance.put(url, data); },
    del: function (url) { return instance.delete(url); },
    delBatch: function (url, ids) { return instance.delete(url, { data: { ids: ids } }); },
    raw: instance
  };

  /**
   * 带鉴权下载文件（导出 CSV 等）
   */
  window.downloadBlob = function (url, filename) {
    const token = localStorage.getItem('erp_token');
    return instance.get(url, { responseType: 'blob' }).then(function (res) {
      let blob = res.data;
      // blob 亦可来自拦截器返回值；此处 res 为 blob
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);
    });
  };
})();
