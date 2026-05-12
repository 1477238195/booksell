import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

// 业务请求
// allowAbsoluteUrls: false — axios 1.x 默认为 true 时，部分以 / 开头的 url 会脱离 baseURL，
// 在仅配置了 /admin-api 代理时易导致请求未转发到后端而出现 HTTP 404
const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '',
  allowAbsoluteUrls: false
})
// request interceptor
request.interceptors.request.use(
  config => {
    // do something before request is sent
    const userStore = useUserStore()
    if (userStore.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers['Authorization'] = userStore.token
      // config.headers['userId'] = userStore.userInfo.id
    }
    return config
  },
  error => {
    // do something with request error
    return Promise.reject(error)
  }
)

// response interceptor
request.interceptors.response.use(
  response => {
    // console.log('🚀 ~ file: request.js:37 ~ response:', response.data)
    const resData = response.data
    // if the custom status is not 20000, it is judged as an error.
    if (resData.status !== 0) {
      ElMessage({
        message: resData.msg || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 50008: Illegal token; 50012: Other clients logged in; 50014: Token expired;
      if ([50008, 50012, 50014].includes(resData.status)) {
        // to re-login
        ElMessageBox.confirm(
          'You have been logged out, you can cancel to stay on this page, or log in again',
          'Confirm logout',
          {
            confirmButtonText: 'Re-Login',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        ).then(() => {
          const userStore = useUserStore()
          userStore.resetToken().then(() => {
            location.reload()
          })
        })
      } else if (resData.status === 10) {
      } else {
        return Promise.reject(new Error(resData.msg || 'Error'))
      }
    } else {
      return resData
    }
  },
  error => {
    let message = error.message
    const data = error.response?.data
    if (data != null && typeof data === 'object' && !Array.isArray(data)) {
      message =
        data.msg ||
        data.message ||
        data.error_description ||
        (typeof data.error === 'string' ? data.error : null) ||
        message
    } else if (typeof data === 'string' && data.trim()) {
      message = data
    }
    ElMessage({
      message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)
export default request
