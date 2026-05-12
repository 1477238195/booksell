<template>
  <div class="profile-edit-page">
    <div class="profile-bg" aria-hidden="true" />
    <div class="profile-inner">
      <header class="profile-hero">
        <div class="profile-back">
          <el-button text class="back-btn" @click="goBack">
            <ArrowLeft class="back-icon" />
            返回
          </el-button>
        </div>
        <p class="profile-kicker">账户中心</p>
        <h1 class="profile-title">修改个人信息</h1>
        <p class="profile-sub">编辑你的账户资料</p>
      </header>

      <section class="profile-card">
        <div class="avatar-section">
          <div class="avatar-upload-area">
            <div class="avatar-preview-wrap">
              <div class="avatar-current">
                <img v-if="editPreviewResolved" :src="editPreviewResolved" :alt="displayName" />
                <span v-else>{{ avatarLetter }}</span>
              </div>
              <span class="preview-label">预览</span>
            </div>
            <div class="upload-btn-wrap">
              <el-upload
                class="avatar-local-upload"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
                :disabled="avatarUploading"
                :before-upload="beforeAvatarLocal"
                :http-request="submitAvatarLocal"
              >
                <el-button type="primary" :loading="avatarUploading">本地上传</el-button>
              </el-upload>
              <p class="avatar-hint">本地图片不超过 2MB，支持 JPG、PNG、GIF、WebP；上传后会自动保存。</p>
            </div>
          </div>
        </div>

        <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="88px" label-position="right">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="editForm.realName" placeholder="真实姓名" maxlength="32" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="editForm.phone" placeholder="11 位手机号" maxlength="11" clearable />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="常用邮箱" maxlength="64" clearable />
          </el-form-item>
        </el-form>
      </section>

      <div class="profile-actions">
        <el-button class="action-btn cancel-btn" @click="goBack">取消</el-button>
        <el-button class="action-btn save-btn" type="primary" :loading="editSubmitting" @click="submitEdit">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import AuthApi from '@/api/AuthApi'
import { UserApi } from '@/api/OtherApi'
import { resolveAvatarUrl } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户')
const avatarLetter = computed(() => {
  const n = userInfo.value?.username
  return n ? String(n).slice(0, 1).toUpperCase() : '用'
})
const avatarDisplayUrl = computed(() => resolveAvatarUrl(userInfo.value))
const avatarBroken = ref(false)

watch(avatarDisplayUrl, () => {
  avatarBroken.value = false
})

const editSubmitting = ref(false)
const avatarUploading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  realName: '',
  phone: '',
  email: '',
  avatar: ''
})

const editPreviewUrl = computed(() => editForm.avatar.trim())
const editPreviewResolved = computed(() => resolveAvatarUrl(editPreviewUrl.value))

const editRules = {
  phone: [
    {
      validator: (_rule, value, callback) => {
        const v = (value || '').trim()
        if (!v) return callback()
        if (/^1[3-9]\d{9}$/.test(v)) callback()
        else callback(new Error('请输入有效手机号'))
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value, callback) => {
        const v = (value || '').trim()
        if (!v) return callback()
        if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) callback()
        else callback(new Error('邮箱格式不正确'))
      },
      trigger: 'blur'
    }
  ]
}

const initForm = () => {
  editForm.realName = userInfo.value?.realName || ''
  editForm.phone = userInfo.value?.phone || ''
  editForm.email = userInfo.value?.email || ''
  editForm.avatar = (userInfo.value?.avatar || userInfo.value?.headUrl || '').trim()
}

const goBack = () => {
  router.push('/dashboard/profile')
}

const persistProfileFields = () => {
  const uid = userInfo.value?.userId
  if (!uid) {
    return Promise.reject(new Error('无法获取用户 ID，请重新登录'))
  }
  return UserApi.update({
    userId: uid,
    realName: (editForm.realName || '').trim(),
    phone: (editForm.phone || '').trim(),
    email: (editForm.email || '').trim(),
    avatar: (editForm.avatar || '').trim()
  })
}

const beforeAvatarLocal = file => {
  const okType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!okType) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP')
    return false
  }
  const okSize = file.size <= 2 * 1024 * 1024
  if (!okSize) {
    ElMessage.error('图片需不超过 2MB')
    return false
  }
  return true
}

const submitAvatarLocal = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  avatarUploading.value = true
  try {
    const up = await AuthApi.uploadAvatar(fd)
    if (up.status !== 0 || !up.data) {
      ElMessage.error(up.msg || '上传失败')
      return
    }
    editForm.avatar = typeof up.data === 'string' ? up.data : String(up.data)
    const res = await persistProfileFields()
    if (res.status === 0) {
      ElMessage.success('头像已上传并保存')
      await syncUserInfo()
      avatarBroken.value = false
    } else {
      ElMessage.error(res.msg || '头像已上传但保存资料失败，请检查表单后点保存')
    }
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  } finally {
    avatarUploading.value = false
  }
}

const syncUserInfo = async () => {
  try {
    const res = await AuthApi.getCurrentUser()
    if (res.status === 0 && res.data) {
      userStore.setUserInfo({ userInfo: res.data })
    }
  } catch {
    /* 保持本地 cookie 信息 */
  }
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch {
    return
  }
  editSubmitting.value = true
  try {
    const res = await persistProfileFields()
    if (res.status === 0) {
      ElMessage.success('保存成功')
      await syncUserInfo()
      router.push('/dashboard/profile')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    editSubmitting.value = false
  }
}

onMounted(async () => {
  await syncUserInfo()
  initForm()
})
</script>

<style lang="scss" scoped>
.profile-edit-page {
  position: relative;
  min-height: calc(100vh - 72px);
  margin: -10px;
  padding: 24px 16px 40px;
  overflow: auto;
}

.profile-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 480px at 20% 0%, rgba(154, 47, 60, 0.14), transparent 50%),
    radial-gradient(700px 400px at 100% 30%, rgba(255, 106, 0, 0.1), transparent 45%),
    var(--market-gradient-page);
  pointer-events: none;
}

.profile-inner {
  position: relative;
  z-index: 1;
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-hero {
  text-align: center;
  padding-top: 16px;
}

.profile-back {
  position: absolute;
  left: 0;
  top: 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: #78716c;
  font-size: 14px;
  transition: all 0.2s ease;
  
  &:hover {
    color: #9a2f3c;
    background: rgba(154, 47, 60, 0.06);
    border-radius: 8px;
  }
}

.profile-kicker {
  font-size: 12px;
  letter-spacing: 0.25em;
  color: #9a2f3c;
  font-weight: 600;
  margin: 0 0 12px;
}

.profile-title {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  color: #2c1810;
  letter-spacing: -0.5px;
}

.profile-sub {
  margin: 0;
  font-size: 14px;
  color: #a8a29e;
}

.profile-card {
  background: rgba(255, 254, 251, 0.98);
  border-radius: 20px;
  padding: 28px 24px;
  border: 1px solid rgba(44, 24, 16, 0.06);
  box-shadow: 
    0 4px 20px -8px rgba(44, 24, 16, 0.08),
    0 8px 32px -12px rgba(154, 47, 60, 0.06);
}

.form-row {
  margin-bottom: 20px;
  
  &:last-of-type {
    margin-bottom: 0;
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(44, 24, 16, 0.06);
  margin-bottom: 20px;
}

.avatar-upload-area {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.avatar-preview-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-current {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(154, 47, 60, 0.25);
  
  img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
  }
}

.upload-btn-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-hint {
  font-size: 12px;
  color: #a8a29e;
  line-height: 1.5;
  margin: 0;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.action-btn {
  min-width: 110px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.cancel-btn {
  color: #78716c;
  border-color: #d6d3d1;
  
  &:hover {
    background: #fafaf9;
    border-color: #a8a29e;
  }
}

.save-btn {
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  border: none;
  color: #fff;
  
  &:hover {
    background: linear-gradient(135deg, #a83a48, #8a2634);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(154, 47, 60, 0.3);
  }
  
  &:active {
    transform: translateY(0);
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: #44403c;
  padding-right: 12px;
}

:deep(.el-input) {
  &:focus-within {
    box-shadow: 0 0 0 3px rgba(154, 47, 60, 0.08);
  }
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  border-color: #e7e5e4;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #d6d3d1;
  }
  
  &.is-focus {
    border-color: #9a2f3c;
  }
}

:deep(.el-input__inner) {
  padding: 11px 14px;
  font-size: 14px;
  color: #2c1810;
}

:deep(.el-button--primary) {
  border-radius: 10px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
}

:deep(.el-avatar) {
  background: linear-gradient(135deg, #9a2f3c, #7b1f2a);
  color: #fff;
  font-weight: 700;
}
</style>