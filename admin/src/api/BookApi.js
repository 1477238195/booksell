import Request from '@/utils/request'

const BookApi = {
  // 分页查询书籍列表
  page(params) {
    return Request({
      url: '/api/book/page',
      method: 'get',
      params
    })
  },

  // 新增书籍
  add(data) {
    return Request({
      url: '/api/book/add',
      method: 'post',
      data
    })
  },

  // 更新书籍
  update(data) {
    return Request({
      url: '/api/book/update',
      method: 'post',
      data
    })
  },

  // 删除书籍
  delete(bookId) {
    return Request({
      url: '/api/book/delete',
      method: 'get',
      params: { bookId }
    })
  },

  // 查询书籍详情
  detail(bookId) {
    return Request({
      url: '/api/book/detail',
      method: 'get',
      params: { bookId }
    })
  },

  // 上架/下架书籍
  updateStatus(bookId, status) {
    return Request({
      url: '/api/book/updateStatus',
      method: 'post',
      params: { bookId, status }
    })
  },

  /** 本地上传书籍封面（multipart，字段名 file） */
  uploadCover(formData) {
    return Request({
      url: '/api/auth/upload/book-cover',
      method: 'post',
      data: formData
    })
  }
}

const BookCategoryApi = {
  // 查询所有分类
  list() {
    return Request({
      url: '/api/bookCategory/list',
      method: 'get'
    })
  },

  // 新增分类
  add(data) {
    return Request({
      url: '/api/bookCategory/add',
      method: 'post',
      data
    })
  },

  // 更新分类
  update(data) {
    return Request({
      url: '/api/bookCategory/update',
      method: 'post',
      data
    })
  },

  // 删除分类
  delete(categoryId) {
    return Request({
      url: '/api/bookCategory/delete',
      method: 'get',
      params: { categoryId }
    })
  }
}

export { BookApi, BookCategoryApi }
