<template>
  <div class="edit-book">
    <div class="edit-container">
      <div class="edit-header">
        <div class="edit-header__accent"></div>
        <div class="edit-header__text">
          <h1 class="edit-header__title">编辑书籍</h1>
          <p class="edit-header__sub">修改书籍信息，让信息更准确</p>
        </div>
        <el-button class="edit-header__back" @click="goBack">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回
        </el-button>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="edit-form">
        <div class="form-section-horizontal">
          <div class="form-section-horizontal__left">
            <div class="cover-section">
              <h2 class="cover-section__title">封面图片</h2>
              <div class="cover-upload">
                <div class="cover-upload__area" @click="triggerUpload" v-if="!form.coverImage">
                  <div class="cover-upload__icon-wrap">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M12 5v14M5 12h14"/>
                    </svg>
                  </div>
                  <p class="cover-upload__text">点击上传封面图片</p>
                  <p class="cover-upload__hint">支持 JPG、PNG、GIF、WebP，不超过 5MB</p>
                  <input 
                    ref="fileInput"
                    type="file" 
                    accept="image/jpeg,image/png,image/gif,image/webp" 
                    class="cover-upload__input"
                    @change="handleFileSelect"
                  />
                </div>
                
                <div class="cover-preview" v-else>
                  <div class="cover-preview__image-wrap">
                    <el-image
                      :src="form.coverImage"
                      fit="cover"
                      class="cover-preview__image"
                      :preview-src-list="[form.coverImage]"
                      preview-teleported
                    />
                    <div class="cover-preview__actions">
                      <el-button 
                        icon="Refresh" 
                        circle 
                        size="small" 
                        class="cover-preview__btn"
                        @click="triggerUpload"
                      />
                      <el-button 
                        icon="Delete" 
                        circle 
                        size="small" 
                        type="danger"
                        class="cover-preview__btn"
                        @click="handleRemoveCover"
                      />
                    </div>
                  </div>
                </div>
                
                <el-upload
                  ref="uploadRef"
                  :show-file-list="false"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  :disabled="coverUploading"
                  :before-upload="beforeCoverUpload"
                  :http-request="submitCoverUpload"
                  class="upload-hidden"
                >
                  <el-button type="primary" :loading="coverUploading">上传封面</el-button>
                </el-upload>
              </div>
            </div>
          </div>

          <div class="form-section-horizontal__right">
            <h2 class="form-section__title">基本信息</h2>
            
            <div class="form-row">
              <el-form-item label="书名" prop="title" class="form-item">
                <el-input 
                  v-model="form.title" 
                  placeholder="请输入书名" 
                  maxlength="200" 
                  show-word-limit
                  class="form-input"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="作者" prop="author" class="form-item form-item--half">
                <el-input 
                  v-model="form.author" 
                  placeholder="请输入作者" 
                  maxlength="100"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item label="ISBN" class="form-item form-item--half">
                <el-input 
                  v-model="form.isbn" 
                  placeholder="请输入ISBN编号（选填）" 
                  maxlength="50"
                  class="form-input"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="分类" prop="categoryId" class="form-item">
                <el-select 
                  v-model="form.categoryId" 
                  placeholder="请选择分类" 
                  class="form-select"
                >
                  <el-option
                    v-for="item in categories"
                    :key="item.categoryId"
                    :label="item.name"
                    :value="item.categoryId"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2 class="form-section__title">价格信息</h2>
          
          <div class="form-row">
            <el-form-item label="售价" prop="price" class="form-item form-item--half">
              <div class="price-input">
                <span class="price-input__symbol">¥</span>
                <el-input-number 
                  v-model="form.price" 
                  :min="0" 
                  :precision="2" 
                  :step="1" 
                  class="price-input__field"
                />
              </div>
            </el-form-item>
            <el-form-item label="原价" class="form-item form-item--half">
              <div class="price-input">
                <span class="price-input__symbol">¥</span>
                <el-input-number 
                  v-model="form.originalPrice" 
                  :min="0" 
                  :precision="2" 
                  :step="1" 
                  class="price-input__field"
                />
              </div>
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <h2 class="form-section__title">书籍状态</h2>
          
          <div class="form-row">
            <el-form-item label="新旧程度" prop="condition" class="form-item">
              <el-radio-group v-model="form.condition" class="condition-radio">
                <el-radio :label="1" class="condition-radio__item">
                  <span class="condition-radio__icon">✨</span>
                  <span class="condition-radio__label">全新</span>
                </el-radio>
                <el-radio :label="2" class="condition-radio__item">
                  <span class="condition-radio__icon">🌟</span>
                  <span class="condition-radio__label">几乎全新</span>
                </el-radio>
                <el-radio :label="3" class="condition-radio__item">
                  <span class="condition-radio__icon">📖</span>
                  <span class="condition-radio__label">轻微痕迹</span>
                </el-radio>
                <el-radio :label="4" class="condition-radio__item">
                  <span class="condition-radio__icon">📚</span>
                  <span class="condition-radio__label">明显痕迹</span>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="库存数量" prop="stock" class="form-item form-item--half">
              <div class="stock-input">
                <el-input-number 
                  v-model="form.stock" 
                  :min="1" 
                  :max="999" 
                  class="stock-input__field"
                />
                <span class="stock-input__unit">本</span>
              </div>
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <h2 class="form-section__title">详细描述</h2>
          
          <div class="form-row">
            <el-form-item label="书籍描述" class="form-item">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                placeholder="请简要描述书籍的内容、特点、适合人群等..."
                maxlength="1000"
                show-word-limit
                class="form-textarea"
              />
            </el-form-item>
          </div>
        </div>

        <div class="form-actions">
          <el-button class="form-actions__btn form-actions__btn--cancel" @click="goBack">取消</el-button>
          <el-button 
            type="primary" 
            class="form-actions__btn form-actions__btn--submit"
            @click="handleSubmit"
            :loading="submitting"
          >
            保存修改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookApi, BookCategoryApi } from '@/api/BookApi'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const fileInput = ref(null)
const uploadRef = ref(null)

const submitting = ref(false)
const coverUploading = ref(false)
const categories = ref([])

const bookId = computed(() => {
  const id = route.params.bookId
  const n = Number(id)
  return Number.isFinite(n) ? n : NaN
})

const form = reactive({
  bookId: null,
  title: '',
  author: '',
  isbn: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  stock: 1,
  condition: 1,
  description: '',
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const loadCategories = async () => {
  try {
    const res = await BookCategoryApi.list()
    if (res.status === 0) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadBook = async () => {
  if (!Number.isFinite(bookId.value)) {
    ElMessage.error('书籍ID无效')
    return
  }
  
  submitting.value = true
  try {
    const res = await BookApi.detail(bookId.value)
    if (res.status === 0 && res.data) {
      const book = res.data
      form.bookId = book.bookId
      form.title = book.title || ''
      form.author = book.author || ''
      form.isbn = book.isbn || ''
      form.categoryId = book.categoryId || null
      form.price = book.price ? Number(book.price) : null
      form.originalPrice = book.originalPrice ? Number(book.originalPrice) : null
      form.stock = book.stock ? Number(book.stock) : 1
      form.condition = book.condition ? Number(book.condition) : 1
      form.description = book.description || ''
      form.coverImage = book.coverImage || ''
    }
  } catch (error) {
    ElMessage.error('加载书籍信息失败')
  } finally {
    submitting.value = false
  }
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    submitCoverUpload({ file })
  }
  event.target.value = ''
}

const beforeCoverUpload = (file) => {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('封面图片大小不能超过 5MB')
    return false
  }
  return true
}

const submitCoverUpload = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  coverUploading.value = true
  try {
    const res = await BookApi.uploadCover(fd)
    if (res.status === 0 && res.data) {
      form.coverImage = typeof res.data === 'string' ? res.data : String(res.data)
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  } finally {
    coverUploading.value = false
  }
}

const handleRemoveCover = () => {
  form.coverImage = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await BookApi.update(form)
        if (res.status === 0) {
          ElMessage.success('修改成功')
          router.push(`/dashboard/book/detail/${form.bookId}`)
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败：' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/dashboard/book/my')
  }
}

onMounted(() => {
  loadCategories()
  loadBook()
})
</script>

<style lang="scss" scoped>
$paper: #f6f1e9;
$paper-light: #faf6ef;
$wine: #7b1f2a;
$wine-light: #9a2f3c;
$orange: #ff6a00;
$border-light: rgba(44, 24, 16, 0.08);
$text-primary: #2c1810;
$text-secondary: #6b5344;
$text-muted: #a8a29e;

.edit-book {
  min-height: calc(100vh - 72px);
  padding: 24px 32px;
  background: linear-gradient(180deg, $paper-light 0%, $paper 42%, #efe8dc 100%);
}

.edit-container {
  max-width: 960px;
  margin: 0 auto;
}

.edit-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(123, 31, 42, 0.12);
  
  &__accent {
    width: 4px;
    height: 40px;
    border-radius: 3px;
    background: linear-gradient(180deg, $wine-light, $wine);
    flex-shrink: 0;
  }
  
  &__text {
    flex: 1;
    min-width: 0;
  }
  
  &__title {
    margin: 0 0 4px;
    font-size: 24px;
    font-weight: 800;
    color: $text-primary;
    letter-spacing: 0.06em;
    font-family: 'Source Han Serif SC', 'Songti SC', 'SimSun', serif;
  }
  
  &__sub {
    margin: 0;
    font-size: 13px;
    color: $text-secondary;
    letter-spacing: 0.08em;
  }
  
  &__back {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 8px;
    border-color: $border-light;
    color: $text-secondary;
    
    &:hover {
      background: $paper-light;
      border-color: rgba(123, 31, 42, 0.2);
    }
  }
}

.edit-form {
  background: #fffefb;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(44, 24, 16, 0.06);
}

.form-section-horizontal {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 32px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid $border-light;
  
  &__left {
    flex-shrink: 0;
  }
  
  &__right {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}

.form-section {
  margin-bottom: 32px;
  
  &__title {
    margin: 0 0 20px;
    font-size: 16px;
    font-weight: 700;
    color: $wine;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::before {
      content: '';
      width: 4px;
      height: 16px;
      background: $wine;
      border-radius: 2px;
    }
  }
}

.cover-section {
  &__title {
    margin: 0 0 16px;
    font-size: 16px;
    font-weight: 700;
    color: $wine;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::before {
      content: '';
      width: 4px;
      height: 16px;
      background: $wine;
      border-radius: 2px;
    }
  }
}

.cover-upload {
  &__area {
    width: 100%;
    height: 200px;
    border: 2px dashed rgba(123, 31, 42, 0.2);
    border-radius: 12px;
    background: $paper-light;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: $wine;
      background: rgba(123, 31, 42, 0.04);
    }
  }
  
  &__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: rgba(123, 31, 42, 0.08);
    color: rgba(123, 31, 42, 0.5);
    margin-bottom: 16px;
    transition: all 0.2s ease;
    
    .cover-upload__area:hover & {
      background: rgba(123, 31, 42, 0.15);
      color: $wine;
      transform: scale(1.05);
    }
    
    svg {
      width: 32px;
      height: 32px;
    }
  }
  
  &__text {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
  }
  
  &__hint {
    margin: 0;
    font-size: 12px;
    color: $text-muted;
  }
  
  &__input {
    display: none;
  }
}

.cover-preview {
  &__image-wrap {
    position: relative;
    width: 100%;
    height: 200px;
    border-radius: 12px;
    overflow: hidden;
    background: $paper;
  }
  
  &__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &__actions {
    position: absolute;
    bottom: 10px;
    right: 10px;
    display: flex;
    gap: 8px;
  }
  
  &__btn {
    background: rgba(255, 255, 255, 0.95);
    border: none;
    box-shadow: 0 2px 8px rgba(44, 24, 16, 0.15);
    
    &:hover {
      background: #fff;
    }
  }
}

.upload-hidden {
  display: none;
}

.form-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.form-item {
  flex: 1;
  min-width: 200px;
  
  &--half {
    flex: 0 0 calc(50% - 8px);
    min-width: 180px;
  }
  
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: $text-primary;
    padding-right: 12px;
  }
}

.form-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    border-color: $border-light;
    background: $paper-light;
    
    &:hover {
      border-color: rgba(123, 31, 42, 0.25);
    }
    
    &.is-focus {
      border-color: $wine;
      box-shadow: 0 0 0 2px rgba(123, 31, 42, 0.08);
    }
  }
}

.form-select {
  :deep(.el-select__wrapper) {
    border-radius: 8px;
    border-color: $border-light;
    background: $paper-light;
    
    &:hover {
      border-color: rgba(123, 31, 42, 0.25);
    }
    
    &.is-focus {
      border-color: $wine;
      box-shadow: 0 0 0 2px rgba(123, 31, 42, 0.08);
    }
  }
}

.price-input {
  display: flex;
  align-items: center;
  
  &__symbol {
    font-size: 16px;
    font-weight: 700;
    color: $orange;
    margin-right: 4px;
  }
  
  &__field {
    :deep(.el-input-number) {
      width: 100%;
      
      .el-input__wrapper {
        border-radius: 8px;
        border-color: $border-light;
        background: $paper-light;
      }
    }
  }
}

.stock-input {
  display: flex;
  align-items: center;
  gap: 8px;
  
  &__field {
    :deep(.el-input-number) {
      width: 100%;
      
      .el-input__wrapper {
        border-radius: 8px;
        border-color: $border-light;
        background: $paper-light;
      }
    }
  }
  
  &__unit {
    font-size: 14px;
    color: $text-secondary;
  }
}

.condition-radio {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  
  &__item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    border-radius: 10px;
    border: 2px solid $border-light;
    background: $paper-light;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: rgba(123, 31, 42, 0.25);
    }
    
    :deep(.el-radio__input.is-checked + .el-radio__label) {
      border-color: $wine;
      background: rgba(123, 31, 42, 0.06);
    }
  }
  
  &__icon {
    font-size: 18px;
  }
  
  &__label {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
  }
}

.form-textarea {
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    border-color: $border-light;
    background: $paper-light;
    resize: none;
    
    &:hover {
      border-color: rgba(123, 31, 42, 0.25);
    }
    
    &:focus {
      border-color: $wine;
      box-shadow: 0 0 0 2px rgba(123, 31, 42, 0.08);
    }
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid $border-light;
  
  &__btn {
    padding: 10px 28px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    
    &--cancel {
      border-color: $border-light;
      color: $text-secondary;
      
      &:hover {
        background: $paper-light;
      }
    }
    
    &--submit {
      background: $wine;
      border: none;
      
      &:hover {
        background: $wine-light;
      }
    }
  }
}

@media (max-width: 768px) {
  .form-section-horizontal {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .form-item--half {
    flex: 1;
    min-width: 100%;
  }
}
</style>
