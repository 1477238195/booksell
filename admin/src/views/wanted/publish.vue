<template>
  <div class="publish-page">
    <!-- 背景装饰 -->
    <div class="bg-glow bg-glow--1"></div>
    <div class="bg-glow bg-glow--2"></div>

    <div class="page-container">
      <!-- 顶部导航 -->
      <nav class="top-nav">
        <button class="nav-back" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回
        </button>
      </nav>

      <!-- 主内容 -->
      <main class="main-content">
        <div class="form-card">
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <path d="M21 21l-4.35-4.35"/>
              </svg>
            </div>
            <div class="header-text">
              <h1 class="card-title">{{ isEdit ? '编辑求购' : '发布求购' }}</h1>
              <p class="card-subtitle">{{ isEdit ? '修改您的求购需求' : '填写求购需求，找到您想要的书籍' }}</p>
            </div>
          </div>

          <!-- 表单内容 -->
          <el-form ref="formRef" :model="form" :rules="rules" class="form-body">
            <!-- 封面 & 书籍信息 -->
            <div class="form-row form-row--cover">
              <!-- 参考图片 -->
              <div class="cover-section">
                <h3 class="section-label">参考图片</h3>
                <div class="cover-upload" @click="triggerUpload" v-if="!form.coverImage">
                  <div class="cover-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <path d="M21 15l-5-5L5 21"/>
                    </svg>
                  </div>
                  <span class="cover-text">点击上传参考图片</span>
                  <span class="cover-hint">可上传书籍封面或相关图片</span>
                  <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" class="cover-input" @change="handleFileSelect" />
                </div>
                <div class="cover-preview" v-else>
                  <el-image :src="form.coverImage" fit="cover" class="preview-image" :preview-src-list="[form.coverImage]" preview-teleported />
                  <div class="preview-actions">
                    <button class="preview-btn" @click="triggerUpload">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                        <polyline points="17 8 12 3 7 8"/>
                        <line x1="12" y1="3" x2="12" y2="15"/>
                      </svg>
                    </button>
                    <button class="preview-btn preview-btn--delete" @click="handleRemoveImage">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                    </button>
                  </div>
                </div>
                <el-upload ref="uploadRef" :show-file-list="false" accept="image/jpeg,image/png,image/gif,image/webp" :disabled="imageUploading" :before-upload="beforeImageUpload" :http-request="submitImageUpload" class="upload-hidden">
                  <el-button type="primary" :loading="imageUploading">上传图片</el-button>
                </el-upload>
              </div>

              <!-- 书籍信息 -->
              <div class="info-section">
                <div class="form-group">
                  <label class="form-label">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 7V4h16v3M9 20h6M12 4v16"/>
                    </svg>
                    求购书名 <span class="required">*</span>
                  </label>
                  <el-input v-model="form.bookTitle" placeholder="请输入求购的书名" maxlength="200" show-word-limit class="form-input" />
                </div>

                <div class="form-row form-row--inline">
                  <div class="form-group">
                    <label class="form-label">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                      </svg>
                      作者
                    </label>
                    <el-input v-model="form.author" placeholder="选填" class="form-input" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                        <line x1="7" y1="7" x2="7.01" y2="7"/>
                      </svg>
                      ISBN
                    </label>
                    <div class="isbn-wrap">
                      <el-input v-model="form.isbn" placeholder="选填" class="form-input" />
                      <button type="button" class="scan-btn" @click="showScanner = true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M3 7V5a2 2 0 0 1 2-2h2"/>
                          <path d="M17 3h2a2 2 0 0 1 2 2v2"/>
                          <path d="M21 17v2a2 2 0 0 1-2 2h-2"/>
                          <path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
                          <line x1="7" y1="12" x2="17" y2="12"/>
                        </svg>
                        扫码
                      </button>
                    </div>
                  </div>
                </div>

                <div class="form-group">
                  <label class="form-label">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="1" x2="12" y2="23"/>
                      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                    </svg>
                    期望最高价 <span class="required">*</span>
                  </label>
                  <div class="price-input">
                    <span class="price-symbol">¥</span>
                    <el-input v-model.number="form.maxPrice" type="number" :min="0" :precision="2" placeholder="0.00" class="price-field" />
                    <span class="price-unit">元</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 求购说明 -->
            <div class="form-section">
              <h3 class="section-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                求购说明 <span class="required">*</span>
              </h3>
              <div class="form-group">
                <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请详细描述您的求购需求，例如：书籍版本、成色要求、是否接受影印版、期望的入手价格等..." maxlength="1000" show-word-limit class="form-textarea" />
              </div>
            </div>

            <!-- 底部操作 -->
            <div class="form-footer">
              <button type="button" class="btn-cancel" @click="handleReset">重置</button>
              <button type="button" class="btn-submit" :class="{ 'btn-submit--loading': loading }" :disabled="loading" @click="handleSubmit">
                <svg v-if="!loading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
                </svg>
                <span v-if="loading">提交中...</span>
                <span v-else>{{ isEdit ? '保存修改' : '发布求购' }}</span>
              </button>
            </div>
          </el-form>
        </div>
      </main>
    </div>

    <!-- 条形码扫描器 -->
    <div v-if="showScanner">
      <el-dialog v-model="showScanner" title="ISBN条形码识别" width="500px" :close-on-click-modal="false" @close="clearScanState">
        <div class="scanner-body">
          <div v-if="!scanSelectedImage" class="scanner-upload" @click="triggerScanFileInput" :class="{ 'scanner-upload--dragover': scanIsDragover }" @dragover.prevent="scanIsDragover = true" @dragleave="scanIsDragover = false" @drop.prevent="handleScanDrop">
            <div class="upload-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <p class="upload-text">点击或拖拽上传条形码图片</p>
            <p class="upload-hint">支持 JPG、PNG，不超过 5MB</p>
            <input ref="scanFileInput" type="file" accept="image/jpeg,image/png,image/gif" class="file-input" @change="handleScanFileSelect" />
          </div>

          <div v-if="scanSelectedImage" class="scanner-preview">
            <el-image :src="scanSelectedImage" fit="contain" class="preview-img" />
            <button class="reset-btn" @click="clearScanState">重新选择</button>
          </div>

          <div v-if="scanIsScanning" class="scanner-loading">
            <div class="loading-spinner"></div>
            <span>正在识别...</span>
          </div>

          <div v-else-if="scanResult" class="scanner-result" :class="scanSuccess ? 'scanner-result--success' : 'scanner-result--error'">
            <div class="result-icon">{{ scanSuccess ? '✓' : '✗' }}</div>
            <div class="result-info">
              <span class="result-title">{{ scanSuccess ? '识别成功' : '识别失败' }}</span>
              <span v-if="scanSuccess" class="result-isbn">ISBN: {{ scanResult }}</span>
              <span v-else class="result-error">{{ scanError }}</span>
            </div>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeScanner">取消</el-button>
            <el-button type="primary" :disabled="!scanSuccess || scanIsScanning" @click="confirmScanResult">使用此ISBN</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { BookWantedApi } from '@/api/OtherApi'
import { BookApi } from '@/api/BookApi'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const uploadRef = ref(null)
const fileInput = ref(null)
const loading = ref(false)
const imageUploading = ref(false)
const showScanner = ref(false)
const scanFileInput = ref(null)
const scanSelectedImage = ref(null)
const scanIsDragover = ref(false)
const scanIsScanning = ref(false)
const scanResult = ref(null)
const scanSuccess = ref(false)
const scanError = ref('')
let BrowserMultiFormatReader = null

const form = reactive({
  wantedId: null,
  bookTitle: '',
  author: '',
  isbn: '',
  maxPrice: null,
  description: '',
  coverImage: ''
})

const rules = {
  bookTitle: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  description: [{ required: true, message: '请输入求购说明', trigger: 'blur' }]
}

const wantedId = computed(() => {
  const id = route.query.wantedId
  const n = Number(id)
  return Number.isFinite(n) ? n : null
})
const isEdit = computed(() => wantedId.value !== null)

const triggerUpload = () => { fileInput.value?.click() }
const handleFileSelect = (event) => { const file = event.target.files?.[0]; if (file) { beforeImageUpload(file) && submitImageUpload({ file }) }; event.target.value = '' }
const beforeImageUpload = file => { const okType = ['image/jpeg','image/png','image/gif','image/webp'].includes(file.type); if (!okType) { ElMessage.error('仅支持 JPG、PNG、GIF、WebP'); return false }; if (file.size > 5*1024*1024) { ElMessage.error('图片需不超过 5MB'); return false }; return true }
const submitImageUpload = async ({ file }) => { const fd = new FormData(); fd.append('file', file); imageUploading.value = true; try { const res = await BookApi.uploadCover(fd); if (res.status === 0 && res.data) { form.coverImage = typeof res.data === 'string' ? res.data : String(res.data); ElMessage.success('图片上传成功') } else { ElMessage.error(res.msg || '上传失败') } } catch (e) { ElMessage.error(e?.message || '上传失败') } finally { imageUploading.value = false } }
const handleRemoveImage = () => { form.coverImage = '' }

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = isEdit.value ? await BookWantedApi.update(form) : await BookWantedApi.add(form)
        if (res.status === 0) {
          ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
          router.push('/dashboard/wanted/my')
        } else {
          ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '发布失败'))
        }
      } catch (error) {
        ElMessage.error(isEdit.value ? '修改失败' : '发布失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const loadWanted = async () => {
  if (!wantedId.value) return
  try {
    const res = await BookWantedApi.detail(wantedId.value)
    if (res.status === 0 && res.data) {
      const data = res.data
      form.wantedId = data.wantedId
      form.bookTitle = data.bookTitle || ''
      form.author = data.author || ''
      form.isbn = data.isbn || ''
      form.maxPrice = data.maxPrice
      form.description = data.description || ''
      form.coverImage = data.coverImage || ''
    }
  } catch (error) {
    ElMessage.error('加载求购信息失败')
  }
}
onMounted(() => { loadWanted() })

const handleReset = () => { formRef.value?.resetFields(); form.coverImage = '' }
const handleBack = () => { router.back() }

const loadZXing = async () => { if (!BrowserMultiFormatReader) { const zxing = await import('@zxing/library'); BrowserMultiFormatReader = zxing.BrowserMultiFormatReader || zxing.default } }
const triggerScanFileInput = () => { scanFileInput.value?.click() }
const handleScanFileSelect = (event) => { const file = event.target.files?.[0]; if (file) { processScanFile(file) } }
const handleScanDrop = (event) => { scanIsDragover.value = false; const file = event.dataTransfer.files?.[0]; if (file && file.type.startsWith('image/')) { processScanFile(file) } }
const processScanFile = (file) => { if (file.size > 5*1024*1024) { scanError.value = '文件大小超过5MB'; scanSuccess.value = false; scanResult.value = null; return }; const reader = new FileReader(); reader.onload = (e) => { scanSelectedImage.value = e.target.result; scanBarcode(e.target.result) }; reader.readAsDataURL(file) }

const scanBarcode = async (imageData) => {
  scanIsScanning.value = true; scanResult.value = null; scanSuccess.value = false; scanError.value = ''
  try {
    await loadZXing(); if (!BrowserMultiFormatReader) throw new Error('ZXing库加载失败')
    const codeReader = new BrowserMultiFormatReader(); const img = new Image(); img.crossOrigin = 'Anonymous'
    await new Promise((resolve, reject) => { img.onload = resolve; img.onerror = reject; img.src = imageData })
    try { const r = await codeReader.decodeFromImageElement(img); if (r) { scanResult.value = r.text; scanSuccess.value = true; return } } catch (e) {}
    try { const c = document.createElement('canvas'); c.width = img.width; c.height = img.height; c.getContext('2d').drawImage(img,0,0); const r = await codeReader.decodeFromImageData(c.getContext('2d').getImageData(0,0,c.width,c.height)); if (r) { scanResult.value = r.text; scanSuccess.value = true; return } } catch (e) {}
    try { const c = document.createElement('canvas'); const max = 600; let w=img.width, h=img.height; if(w>max||h>max){if(w>h){h=(h/w)*max;w=max}else{w=(w/h)*max;h=max}}c.width=w;c.height=h;c.getContext('2d').drawImage(img,0,0,w,h); const r = await codeReader.decodeFromImageData(c.getContext('2d').getImageData(0,0,w,h)); if (r) { scanResult.value = r.text; scanSuccess.value = true; return } } catch (e) {}
    scanError.value = '未检测到条形码'
  } catch (error) { scanError.value = '识别失败' } finally { scanIsScanning.value = false }
}

const clearScanState = () => { scanSelectedImage.value = null; scanResult.value = null; scanSuccess.value = false; scanError.value = ''; if (scanFileInput.value) scanFileInput.value.value = '' }
const closeScanner = () => { clearScanState(); showScanner.value = false }
const confirmScanResult = () => { if (scanSuccess.value && scanResult.value) { form.isbn = scanResult.value; ElMessage.success('ISBN识别成功'); closeScanner() } }
</script>

<style lang="scss" scoped>
// 颜色变量
$primary: #7b1f2a;
$primary-light: #9a2f3c;
$accent: #d4a574;
$accent-light: #e8c9a8;
$bg-cream: #faf6f0;
$bg-warm: #f5efe6;
$text-dark: #2c1810;
$text-medium: #5c4a3d;
$text-light: #8b7355;
$white: #fffefb;
$success: #059669;
$error: #dc2626;

// 背景装饰
.bg-glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  z-index: 0;

  &--1 {
    width: 500px;
    height: 500px;
    top: -100px;
    right: -100px;
    background: radial-gradient(circle, rgba($primary, 0.1) 0%, transparent 70%);
  }

  &--2 {
    width: 400px;
    height: 400px;
    bottom: 0;
    left: -100px;
    background: radial-gradient(circle, rgba($accent, 0.12) 0%, transparent 70%);
  }
}

.publish-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(160deg, $bg-cream 0%, $bg-warm 50%, darken($bg-cream, 3%) 100%);
  z-index: 1;
  padding: 24px;

  @media (min-width: 1400px) {
    padding: 32px 48px;
  }
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;

  @media (min-width: 1400px) {
    max-width: 1400px;
  }
}

.top-nav {
  margin-bottom: 24px;

  .nav-back {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: $white;
    border: 1px solid rgba($text-dark, 0.08);
    border-radius: 14px;
    font-size: 14px;
    font-weight: 600;
    color: $text-dark;
    cursor: pointer;
    transition: all 0.25s ease;
    font-family: inherit;
    box-shadow: 0 4px 16px rgba($text-dark, 0.04);

    svg { width: 18px; height: 18px; }

    &:hover {
      background: $primary;
      color: #fff;
      border-color: $primary;
      transform: translateX(-4px);
    }
  }
}

.main-content {
  padding-bottom: 40px;
}

.form-card {
  background: $white;
  border-radius: 28px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 8px 40px rgba($text-dark, 0.06);
  overflow: hidden;
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 32px;
  background: linear-gradient(135deg, rgba($primary, 0.05) 0%, rgba($accent, 0.08) 100%);
  border-bottom: 1px solid rgba($text-dark, 0.06);

  .header-icon {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, $accent, darken($accent, 10%));
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 8px 24px rgba($accent, 0.3);

    svg { width: 28px; height: 28px; }
  }

  .card-title {
    margin: 0 0 4px;
    font-size: 1.5rem;
    font-weight: 800;
    color: $text-dark;
  }

  .card-subtitle {
    margin: 0;
    font-size: 14px;
    color: $text-light;
  }
}

.form-body {
  padding: 32px;
}

.form-row {
  display: grid;
  gap: 28px;
  margin-bottom: 28px;

  &--cover {
    grid-template-columns: 240px 1fr;

    @media (max-width: 900px) {
      grid-template-columns: 180px 1fr;
    }

    @media (max-width: 600px) {
      grid-template-columns: 1fr;
    }
  }

  &--inline {
    grid-template-columns: 1fr 1fr;

    @media (max-width: 600px) {
      grid-template-columns: 1fr;
    }
  }
}

.form-section {
  padding-top: 28px;
  margin-top: 28px;
  border-top: 1px solid rgba($text-dark, 0.06);

  .section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 0 20px;
    font-size: 16px;
    font-weight: 700;
    color: $text-dark;

    svg { width: 20px; height: 20px; color: $primary; }

    .required { color: $error; font-size: 14px; }
  }
}

.section-label {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  color: $text-dark;
}

.form-group {
  margin-bottom: 20px;

  &:last-child { margin-bottom: 0; }
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: $text-dark;

  svg { width: 16px; height: 16px; color: $text-light; }

  .required { color: $error; }
}

.form-input {
  width: 100%;

  :deep(.el-input__wrapper) {
    padding: 14px 16px;
    border-radius: 12px;
    border: 1px solid rgba($text-dark, 0.1);
    box-shadow: none;
    transition: all 0.25s ease;

    &:hover { border-color: rgba($primary, 0.3); }
    &:focus-within {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba($primary, 0.1);
    }
  }

  :deep(.el-input__inner) {
    font-size: 15px;
    color: $text-dark;

    &::placeholder { color: $text-light; }
  }
}

.form-textarea {
  :deep(.el-textarea__inner) {
    padding: 16px;
    border-radius: 14px;
    border: 1px solid rgba($text-dark, 0.1);
    font-size: 15px;
    line-height: 1.7;
    color: $text-dark;
    resize: vertical;
    transition: all 0.25s ease;
    font-family: inherit;

    &::placeholder { color: $text-light; }
    &:focus {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba($primary, 0.1);
    }
  }
}

// 封面
.cover-section {
  .section-label {
    margin-bottom: 16px;
  }
}

.cover-upload {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 220px;
  height: 290px;
  background: linear-gradient(135deg, rgba($accent, 0.05) 0%, rgba($accent-light, 0.1) 100%);
  border: 2px dashed rgba($accent, 0.3);
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: $accent;
    background: linear-gradient(135deg, rgba($accent, 0.08) 0%, rgba($accent-light, 0.15) 100%);

    .cover-icon {
      transform: scale(1.1);
      color: $accent;
    }
  }

  .cover-icon {
    width: 60px;
    height: 60px;
    margin-bottom: 14px;
    color: rgba($accent, 0.6);
    transition: all 0.3s ease;

    svg { width: 100%; height: 100%; }
  }

  .cover-text {
    font-size: 14px;
    font-weight: 600;
    color: $text-dark;
    margin-bottom: 6px;
  }

  .cover-hint {
    font-size: 11px;
    color: $text-light;
    text-align: center;
    padding: 0 16px;
  }

  .cover-input {
    position: absolute;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
  }
}

.cover-preview {
  position: relative;
  width: 100%;
  max-width: 220px;
  height: 290px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba($text-dark, 0.12);

  .preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .preview-actions {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    gap: 10px;
    padding: 14px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    opacity: 0;
    transition: opacity 0.25s ease;
  }

  &:hover .preview-actions {
    opacity: 1;
  }

  .preview-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.95);
    border: none;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.25s ease;
    color: $text-dark;

    svg { width: 18px; height: 18px; }

    &:hover { background: #fff; transform: scale(1.1); }
    &--delete:hover { background: $error; color: #fff; }
  }
}

.upload-hidden { display: none; }

// 信息区
.info-section {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.isbn-wrap {
  display: flex;
  gap: 10px;

  .form-input { flex: 1; min-width: 0; }
}

.scan-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
  white-space: nowrap;

  svg { width: 16px; height: 16px; }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
  }
}

// 价格输入
.price-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba($accent-light, 0.2), rgba($accent, 0.15));
  border: 1px solid rgba($accent, 0.3);
  border-radius: 14px;
  transition: all 0.25s ease;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
    background: #fff;
  }

  .price-symbol {
    font-size: 18px;
    font-weight: 800;
    color: $primary;
  }

  .price-field {
    flex: 1;

    :deep(.el-input__wrapper) {
      padding: 0;
      background: transparent;
      border: none;
      box-shadow: none;
    }

    :deep(.el-input__inner) {
      padding: 0;
      font-size: 20px;
      font-weight: 700;
      color: $text-dark;
      text-align: center;
    }
  }

  .price-unit {
    font-size: 13px;
    color: $text-light;
  }
}

// 底部操作
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba($text-dark, 0.06);
}

.btn-cancel {
  padding: 14px 28px;
  background: $white;
  border: 1px solid rgba($text-dark, 0.15);
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: $text-medium;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  &:hover {
    background: $bg-cream;
    border-color: rgba($text-dark, 0.25);
  }
}

.btn-submit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, $accent, darken($accent, 10%));
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba($accent, 0.35);
  font-family: inherit;

  svg { width: 18px; height: 18px; }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba($accent, 0.45);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &--loading {
    background: linear-gradient(135deg, darken($accent, 5%), darken($accent, 15%));
  }
}

// 扫描器
.scanner-body {
  padding: 12px 0;
}

.scanner-upload {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  border: 2px dashed rgba($text-dark, 0.15);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover, &--dragover {
    border-color: #6366f1;
    background: rgba(99, 102, 241, 0.05);
  }

  .upload-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    color: rgba($text-dark, 0.4);

    svg { width: 100%; height: 100%; }
  }

  .upload-text {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 600;
    color: $text-dark;
  }

  .upload-hint {
    margin: 0;
    font-size: 12px;
    color: $text-light;
  }

  .file-input {
    position: absolute;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
  }
}

.scanner-preview {
  text-align: center;

  .preview-img {
    max-height: 250px;
    border-radius: 12px;
    margin-bottom: 12px;
  }

  .reset-btn {
    padding: 8px 16px;
    background: transparent;
    border: none;
    font-size: 13px;
    color: #6366f1;
    cursor: pointer;
  }
}

.scanner-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px;

  .loading-spinner {
    width: 48px;
    height: 48px;
    border: 3px solid rgba(99, 102, 241, 0.2);
    border-top-color: #6366f1;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  span {
    font-size: 14px;
    color: $text-light;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.scanner-result {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  margin-top: 16px;

  &--success {
    background: rgba($success, 0.1);
    border: 1px solid rgba($success, 0.2);
  }

  &--error {
    background: rgba($error, 0.1);
    border: 1px solid rgba($error, 0.2);
  }

  .result-icon {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    font-size: 20px;
    font-weight: 700;

    .scanner-result--success & {
      background: $success;
      color: #fff;
    }

    .scanner-result--error & {
      background: $error;
      color: #fff;
    }
  }

  .result-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .result-title {
    font-size: 15px;
    font-weight: 600;
    color: $text-dark;
  }

  .result-isbn {
    font-size: 14px;
    font-weight: 600;
    color: #6366f1;
    font-family: monospace;
  }

  .result-error {
    font-size: 13px;
    color: $error;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid rgba($text-dark, 0.06);
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: $text-dark;
}

:deep(.el-form-item__error) {
  font-size: 12px;
}
</style>
