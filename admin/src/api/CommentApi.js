import request from '@/utils/request'

/**
 * 书籍评论API
 */
export const CommentApi = {
  /**
   * 获取书籍评论列表
   * @param {number} bookId - 书籍ID
   * @returns {Promise}
   */
  getComments(bookId) {
    return request({
      url: '/api/book/comment/list',
      method: 'get',
      params: { bookId }
    })
  },

  /**
   * 添加评论
   * @param {number} bookId - 书籍ID
   * @param {string} content - 评论内容
   * @returns {Promise}
   */
  addComment(bookId, content) {
    return request({
      url: '/api/book/comment/add',
      method: 'post',
      params: { bookId, content }
    })
  },

  /**
   * 删除评论
   * @param {number} commentId - 评论ID
   * @param {number} bookSellerId - 书籍卖家ID（可选）
   * @returns {Promise}
   */
  deleteComment(commentId, bookSellerId) {
    return request({
      url: '/api/book/comment/delete',
      method: 'post',
      params: { commentId, bookSellerId }
    })
  }
}
