<template>
  <div class="publish-page">
    <!-- 背景装饰 -->
    <div class="bg-glow bg-glow--1"></div>
    <div class="bg-glow bg-glow--2"></div>

    <div class="page-container">
      <!-- 顶部导航 -->
      <nav class="top-nav">
        <button class="nav-back" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回论坛
        </button>
      </nav>

      <!-- 主内容 -->
      <main class="main-content">
        <div class="form-card">
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 5v14M5 12h14"/>
              </svg>
            </div>
            <div class="header-text">
              <h1 class="card-title">发布帖子</h1>
              <p class="card-subtitle">分享你的读书心得，与书友交流</p>
            </div>
          </div>

          <!-- 表单内容 -->
          <div class="form-body">
            <!-- 版块选择 -->
            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7" rx="1"/>
                  <rect x="14" y="3" width="7" height="7" rx="1"/>
                  <rect x="3" y="14" width="7" height="7" rx="1"/>
                  <rect x="14" y="14" width="7" height="7" rx="1"/>
                </svg>
                选择版块
                <span class="required">*</span>
              </label>
              <div class="board-grid">
                <button
                  v-for="b in boards"
                  :key="b.boardId"
                  class="board-btn"
                  :class="{ 'board-btn--active': form.boardId === b.boardId }"
                  @click="form.boardId = b.boardId"
                >
                  <span class="board-icon">{{ getBoardIcon(b.name) }}</span>
                  <span class="board-name">{{ b.name }}</span>
                </button>
              </div>
            </div>

            <!-- 标题 -->
            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 7V4h16v3M9 20h6M12 4v16"/>
                </svg>
                帖子标题
                <span class="required">*</span>
              </label>
              <div class="input-wrapper">
                <el-input
                  v-model="form.title"
                  placeholder="写一个简洁有力的标题..."
                  maxlength="100"
                  show-word-limit
                  class="title-input"
                />
              </div>
            </div>

            <!-- 内容 -->
            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                正文内容
                <span class="required">*</span>
              </label>
              <div class="textarea-wrapper">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="10"
                  placeholder="分享你的读书心得、与书友交流讨论..."
                  maxlength="20000"
                  show-word-limit
                  class="content-textarea"
                />
              </div>
            </div>

            <!-- 关联书籍 -->
            <div class="form-group">
              <label class="form-label">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
                关联书籍（可选）
              </label>
              <div class="book-search">
                <el-input
                  v-model="bookSearchKeyword"
                  placeholder="搜索书名来关联书籍..."
                  clearable
                  @keyup.enter="searchBooks"
                >
                  <template #append>
                    <el-button @click="searchBooks">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="M21 21l-4.35-4.35"/>
                      </svg>
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div v-if="searchResults.length" class="book-results">
                <div
                  v-for="book in searchResults"
                  :key="book.bookId"
                  class="book-result"
                  @click="selectBook(book)"
                >
                  <div class="result-cover">
                    <el-image
                      v-if="book.coverImage"
                      :src="book.coverImage"
                      fit="cover"
                      class="cover-img"
                      referrerpolicy="no-referrer"
                    />
                    <div v-else class="cover-placeholder">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                      </svg>
                    </div>
                  </div>
                  <div class="result-info">
                    <span class="result-title">{{ book.title }}</span>
                    <span v-if="book.author" class="result-author">{{ book.author }}</span>
                  </div>
                  <svg class="result-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                </div>
              </div>

              <div v-if="linkedBook" class="selected-book">
                <div class="selected-cover">
                  <el-image
                    v-if="linkedBook.coverImage"
                    :src="linkedBook.coverImage"
                    fit="cover"
                    class="cover-img"
                    referrerpolicy="no-referrer"
                  />
                  <div v-else class="cover-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                    </svg>
                  </div>
                </div>
                <div class="selected-info">
                  <span class="selected-label">已选择</span>
                  <span class="selected-title">《{{ linkedBook.title }}》</span>
                </div>
                <button class="clear-btn" @click="clearBook">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 6L6 18M6 6l12 12"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="form-footer">
            <button class="cancel-btn" @click="goBack">取消</button>
            <button class="submit-btn" :class="{ 'submit-btn--loading': submitting }" :disabled="submitting" @click="submit">
              <span v-if="!submitting" class="btn-content">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
                </svg>
                发布帖子
              </span>
              <span v-else class="btn-loading">发布中...</span>
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ForumApi } from '@/api/OtherApi'
import { BookApi } from '@/api/BookApi'

const router = useRouter()

const boards = ref([])
const submitting = ref(false)
const bookSearchKeyword = ref('')
const searchResults = ref([])
const linkedBook = ref(null)

const form = reactive({
  boardId: null,
  title: '',
  content: '',
  bookId: null
})

function getBoardIcon(name) {
  const iconMap = {
    '综合讨论': '💭', '书评读后感': '📖', '求书换书': '🔄',
    '交易相关': '💰', '读书笔记': '📝', '书评': '⭐',
    '交换': '🔄', '推荐': '👍', '求助': '❓',
    '讨论': '💬', '分享': '🎁', '二手': '📚'
  }
  return iconMap[name] || '📚'
}

async function loadBoards() {
  try {
    const res = await ForumApi.boards()
    if (res.status === 0 && res.data) {
      boards.value = Array.isArray(res.data) ? res.data : []
      if (!form.boardId && boards.value.length) {
        form.boardId = boards.value[0].boardId
      }
    }
  } catch {
    boards.value = []
  }
}

async function searchBooks() {
  if (!bookSearchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await BookApi.list({ title: bookSearchKeyword.value, page: 1, size: 5 })
    if (res.status === 0) {
      searchResults.value = Array.isArray(res.data) ? res.data : res.data?.list || []
    }
  } catch {
    searchResults.value = []
  }
}

function selectBook(book) {
  form.bookId = book.bookId
  linkedBook.value = book
  searchResults.value = []
  bookSearchKeyword.value = ''
  ElMessage.success(`已关联《${book.title}》`)
}

function clearBook() {
  form.bookId = null
  linkedBook.value = null
}

async function submit() {
  if (!form.boardId) {
    ElMessage.warning('请选择版块')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  submitting.value = true
  try {
    const res = await ForumApi.addTopic({
      boardId: form.boardId,
      title: form.title.trim(),
      content: form.content.trim(),
      bookId: form.bookId || null
    })
    if (res.status === 0) {
      ElMessage.success('发布成功！')
      router.push('/dashboard/forum/list')
    }
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/dashboard/forum/list')
}

onMounted(async () => {
  await loadBoards()
})
</script>

<style lang="scss" scoped>
// 颜色变量
$primary: #7b1f2a;
$primary-light: #9a2f3c;
$primary-dark: #5a171f;
$accent: #d4a574;
$accent-light: #e8c9a8;
$bg-cream: #faf6f0;
$bg-warm: #f5efe6;
$text-dark: #2c1810;
$text-medium: #5c4a3d;
$text-light: #8b7355;
$white: #fffefb;

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
}

.page-container {
  max-width: 800px;
  margin: 0 auto;
}

// 顶部导航
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

// 主内容
.main-content {
  padding-bottom: 40px;
}

.form-card {
  background: $white;
  border-radius: 28px;
  border: 1px solid rgba($text-dark, 0.04);
  box-shadow: 0 8px 40px rgba($text-dark, 0.06);
  overflow: hidden;
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
    background: linear-gradient(135deg, $primary, $primary-light);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 8px 24px rgba($primary, 0.3);

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

.form-group {
  margin-bottom: 28px;

  &:last-child { margin-bottom: 0; }
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: $text-dark;

  svg { width: 18px; height: 18px; color: $primary; }

  .required { color: #dc2626; }
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.board-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: $bg-cream;
  border: 2px solid transparent;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: left;
  font-family: inherit;

  .board-icon { font-size: 20px; }
  .board-name { font-size: 14px; font-weight: 600; color: $text-medium; }

  &:hover {
    background: rgba($primary, 0.08);
    .board-name { color: $primary; }
  }

  &--active {
    background: rgba($primary, 0.1);
    border-color: $primary;

    .board-name { color: $primary; font-weight: 700; }
  }
}

.input-wrapper {
  :deep(.el-input__wrapper) {
    padding: 14px 16px;
    border-radius: 12px;
    border: 1px solid rgba($text-dark, 0.1);
    box-shadow: none;
    transition: all 0.25s ease;

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

  :deep(.el-input__count-inner) {
    background: transparent;
  }
}

.textarea-wrapper {
  :deep(.el-textarea__inner) {
    padding: 16px;
    border-radius: 14px;
    border: 1px solid rgba($text-dark, 0.1);
    font-size: 15px;
    line-height: 1.75;
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

  :deep(.el-input__count) {
    background: transparent;
    color: $text-light;
  }
}

.book-search {
  :deep(.el-input__wrapper) {
    padding: 12px 16px;
    border-radius: 12px;
    border: 1px solid rgba($text-dark, 0.1);
    box-shadow: none;
    transition: all 0.25s ease;

    &:focus-within {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba($primary, 0.1);
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: $text-dark;

    &::placeholder { color: $text-light; }
  }

  :deep(.el-button) {
    svg { width: 16px; height: 16px; }
  }
}

.book-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.book-result {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: $bg-cream;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: rgba($primary, 0.08);
    transform: translateX(4px);
  }

  .result-cover {
    width: 40px;
    height: 52px;
    border-radius: 6px;
    overflow: hidden;
    background: $white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    flex-shrink: 0;
  }

  .cover-img { width: 100%; height: 100%; object-fit: cover; }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-light;

    svg { width: 18px; height: 18px; }
  }

  .result-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  .result-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-dark;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .result-author {
    font-size: 12px;
    color: $text-light;
  }

  .result-arrow {
    width: 20px;
    height: 20px;
    color: $text-light;
    flex-shrink: 0;
  }
}

.selected-book {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
  padding: 14px;
  background: linear-gradient(135deg, rgba($accent-light, 0.4), rgba($accent, 0.3));
  border-radius: 14px;

  .selected-cover {
    width: 48px;
    height: 60px;
    border-radius: 8px;
    overflow: hidden;
    background: $white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    flex-shrink: 0;
  }

  .selected-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .selected-label {
    font-size: 10px;
    color: $text-light;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .selected-title {
    font-size: 14px;
    font-weight: 700;
    color: $text-dark;
  }

  .clear-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba($white, 0.6);
    border: none;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.25s ease;
    color: $text-light;

    svg { width: 16px; height: 16px; }

    &:hover {
      background: #dc2626;
      color: #fff;
    }
  }
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 32px;
  background: $bg-cream;
  border-top: 1px solid rgba($text-dark, 0.06);
}

.cancel-btn {
  padding: 14px 28px;
  background: $white;
  border: 1px solid rgba($text-dark, 0.1);
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  color: $text-medium;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;

  &:hover {
    background: $bg-warm;
    border-color: rgba($text-dark, 0.2);
  }
}

.submit-btn {
  padding: 14px 32px;
  background: linear-gradient(135deg, $primary, $primary-light);
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba($primary, 0.35);
  font-family: inherit;

  .btn-content {
    display: flex;
    align-items: center;
    gap: 8px;

    svg { width: 18px; height: 18px; }
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba($primary, 0.45);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &--loading {
    background: linear-gradient(135deg, $primary-dark, $primary);
  }
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
