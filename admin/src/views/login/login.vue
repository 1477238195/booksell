<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="login-welcome">
          <h1 class="welcome-title">二手书交易平台</h1>
          <p class="welcome-subtitle">让知识流动起来</p>
          <div class="welcome-description">
            <p>为读者提供便捷的二手书交易服务</p>
            <p>让好书找到新主人</p>
          </div>
        </div>
      </div>
      <div class="login-right">
        <!-- 已登录提示 -->
        <div v-if="hasLoggedIn" class="already-logged-in">
          <div class="logged-in-icon">✓</div>
          <h3 class="logged-in-title">您已登录</h3>
          <p class="logged-in-name">当前账号：{{ currentUser }}</p>
          <div class="logged-in-actions">
            <el-button type="primary" @click="goHome">进入首页</el-button>
            <el-button @click="handleLogout">切换账号</el-button>
          </div>
        </div>
        
        <!-- 登录表单 -->
        <el-form v-else ref="refLoginForm" :model="loginForm" :rules="loginRules" class="login-form animated-form"
          autocomplete="on" label-width="80px">
          <div class="title-container">
            <h3 class="title">用户登录</h3>
            <p class="subtitle">Login to Your Account</p>
          </div>

          <el-form-item label="用户名" prop="username">
            <el-input ref="refUsername" v-model="loginForm.username" placeholder="请输入用户名" name="username" type="text"
              tabindex="1" autocomplete="on" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input ref="refPassword" v-model="loginForm.password" :type="passwordType" placeholder="请输入密码"
              name="password" tabindex="2" autocomplete="on" @keyup.enter="handleLogin" />
            <span class="show-pwd" @click="showPwd()">
              <svg-icon :name="passwordType === 'password' ? 'eye' : 'eye-open'" />
            </span>
          </el-form-item>

          <el-button :loading="loading" type="primary" size="large" class="login-button" @click.prevent="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>

          <div class="tips">
            <span class="link-type" @click="goRegister">还没有账号？立即注册</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, reactive, toRefs, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import AuthApi from '@/api/AuthApi'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const state = reactive({
  refLoginForm: null,
  refUsername: null,
  refPassword: null,
  loginForm: {
    username: '',
    password: ''
  },
  loginRules: {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  },
  passwordType: 'password',
  loading: false,
  showDialog: false,
  redirect: undefined,
  otherQuery: {}
})

const { refLoginForm, refUsername, refPassword, loginForm, loginRules, passwordType, loading } = toRefs(state)

// 检查是否已登录
const hasLoggedIn = computed(() => {
  return userStore.token && userStore.token.access_token
})

// 当前用户名
const currentUser = computed(() => {
  return userStore.userInfo?.username || '未知用户'
})

// 进入首页
const goHome = () => {
  router.push('/dashboard')
}

// 切换账号 - 退出登录
const handleLogout = async () => {
  await userStore.resetToken()
  await router.replace({ path: '/login' })
  ElMessage.success('已退出登录，请重新登录')
}

const getOtherQuery = query => {
  return Object.keys(query).reduce((acc, cur) => {
    if (cur !== 'redirect') {
      acc[cur] = query[cur]
    }
    return acc
  }, {})
}

const showPwd = () => {
  if (state.passwordType === 'password') {
    state.passwordType = ''
  } else {
    state.passwordType = 'password'
  }
  nextTick(() => {
    if (state.refPassword) {
      state.refPassword.focus()
    }
  })
}

function resolveAfterLoginRedirect(redirect) {
  if (redirect && typeof redirect === 'string') {
    const r = redirect.trim()
    if (r.startsWith('/') && !r.startsWith('//')) {
      if (r === '/login' || r === '/register') return '/dashboard'
      return r
    }
  }
  return '/dashboard'
}

const handleLogin = async () => {
  if (!state.refLoginForm) return
  await state.refLoginForm.validate(async valid => {
    if (valid) {
      state.loading = true
      try {
        const response = await AuthApi.login({
          username: state.loginForm.username,
          password: state.loginForm.password
        })

        const userStore = useUserStore()
        const token = response.data.token
        const userInfo = response.data.userInfo

        userStore.setToken({ token })
        if (userInfo) {
          userStore.setUserInfo({ userInfo })
        }

        await nextTick()

        const target = resolveAfterLoginRedirect(state.redirect)
        await router.replace(target)

        ElMessage.success('登录成功')
      } catch {
      } finally {
        state.loading = false
      }
    }
  })
}

const goRegister = () => {
  router.push({ path: '/register', query: state.otherQuery })
}

const init = () => {
  const { query } = route
  if (query) {
    state.redirect = query.redirect
    state.otherQuery = getOtherQuery(query)
  }
}

init()
</script>
<style lang="scss" scoped>
$bg: #f6f1e9;
$dark_gray: #78716c;
$light_gray: #2c1810;
$primary-color: #7b1f2a;
$secondary-color: #15803d;
$welcome-bg: linear-gradient(145deg, #9a2f3c 0%, #7b1f2a 45%, #5c1520 100%);

.login-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  .login-box {
    display: flex;
    width: 900px;
    height: 500px;
    background: #fffefb;
    border-radius: 10px;
    box-shadow: 0 18px 40px rgba(44, 24, 16, 0.12);
    overflow: hidden;

    .login-left {
      flex: 1;
      background: $welcome-bg;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      .login-welcome {
        text-align: center;
        padding: 40px;

        .welcome-title {
          font-size: 32px;
          font-weight: bold;
          margin-bottom: 20px;
          letter-spacing: 2px;
        }

        .welcome-subtitle {
          font-size: 18px;
          margin-bottom: 30px;
          opacity: 0.9;
        }

        .welcome-description {
          font-size: 16px;
          line-height: 1.8;
          opacity: 0.8;
        }
      }
    }

    .login-right {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;

      .login-form {
        width: 100%;
        max-width: 350px;

        .title-container {
          text-align: center;
          margin-bottom: 40px;

          .title {
            font-size: 28px;
            color: #333;
            margin: 0 auto 10px;
            font-weight: bold;
          }

          .subtitle {
            font-size: 14px;
            color: $dark_gray;
            margin: 0;
          }
        }

        .svg-container {
          padding: 6px 5px 6px 15px;
          color: $dark_gray;
          vertical-align: middle;
          display: inline-block;
        }

        .show-pwd {
          position: absolute;
          right: 10px;
          top: 0px;
          font-size: 16px;
          color: $dark_gray;
          cursor: pointer;
          user-select: none;
        }
      }
    }
  }

  .tips {
    font-size: 14px;
    color: #666;
    margin-top: 20px;
    text-align: center;

    .link-type {
      color: $primary-color;
      cursor: pointer;
      transition: color 0.3s;
      font-weight: 500;

      &:hover {
        color: #e85d00;
        text-decoration: underline;
      }
    }
  }

  .login-button {
    width: 100%;
    background: $primary-color;
    border-color: $primary-color;
    margin-top: 20px;
    transition: all 0.3s;
    height: 45px;
    font-size: 16px;

    &:hover {
      background: #9a2f3c;
      border-color: #9a2f3c;
      transform: translateY(-2px);
      box-shadow: 0 15px 30px rgba(123, 31, 42, 0.28);
    }
  }

  /* 已登录状态样式 */
  .already-logged-in {
    width: 100%;
    max-width: 350px;
    text-align: center;
    padding: 40px 20px;
    animation: form-appear 0.8s ease-out;

    .logged-in-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      background: linear-gradient(135deg, #10b981, #059669);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
      color: white;
      box-shadow: 0 10px 30px rgba(16, 185, 129, 0.3);
    }

    .logged-in-title {
      font-size: 24px;
      color: #333;
      margin: 0 0 10px;
      font-weight: bold;
    }

    .logged-in-name {
      font-size: 14px;
      color: $dark_gray;
      margin: 0 0 30px;
    }

    .logged-in-actions {
      display: flex;
      gap: 12px;

      button {
        flex: 1;
        height: 45px;
        font-size: 14px;
      }
    }
  }

  .animated-form {
    animation: form-appear 0.8s ease-out;
  }

  @keyframes form-appear {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}
</style>
