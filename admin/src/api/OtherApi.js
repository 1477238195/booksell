import Request from '@/utils/request'

const OrderApi = {
  page(params) {
    return Request({ url: '/api/order/page', method: 'get', params })
  },
  myBuyOrders(params) {
    return Request({ url: '/api/order/myBuyOrders', method: 'get', params })
  },
  mySellOrders(params) {
    return Request({ url: '/api/order/mySellOrders', method: 'get', params })
  },
  createOrder(bookId, quantity) {
    return Request({ url: '/api/order/create', method: 'post', params: { bookId, quantity } })
  },
  cancelOrder(orderId) {
    return Request({ url: '/api/order/cancel', method: 'post', params: { orderId } })
  },
  payOrder(orderId) {
    return Request({ url: '/api/order/pay', method: 'post', params: { orderId } })
  },
  deliverOrder(orderId) {
    return Request({ url: '/api/order/deliver', method: 'post', params: { orderId } })
  },
  confirmOrder(orderId) {
    return Request({ url: '/api/order/confirm', method: 'post', params: { orderId } })
  },
  detail(orderId) {
    return Request({ url: '/api/order/detail', method: 'get', params: { orderId } })
  }
}

const BookWantedApi = {
  page(params) {
    return Request({ url: '/api/bookWanted/page', method: 'get', params })
  },
  myWanted(params) {
    return Request({ url: '/api/bookWanted/myWanted', method: 'get', params })
  },
  add(data) {
    return Request({ url: '/api/bookWanted/add', method: 'post', data })
  },
  update(data) {
    return Request({ url: '/api/bookWanted/update', method: 'post', data })
  },
  delete(wantedId) {
    return Request({ url: '/api/bookWanted/delete', method: 'get', params: { wantedId } })
  },
  detail(wantedId) {
    return Request({ url: '/api/bookWanted/detail', method: 'get', params: { wantedId } })
  },
  updateStatus(wantedId, status) {
    return Request({ url: '/api/bookWanted/updateStatus', method: 'post', params: { wantedId, status } })
  }
}

const MessageApi = {
  page(params) {
    return Request({ url: '/api/message/page', method: 'get', params })
  },
  sendMessage(data) {
    return Request({ url: '/api/message/send', method: 'post', data })
  },
  markAsRead(messageId) {
    return Request({ url: '/api/message/markAsRead', method: 'post', params: { messageId } })
  },
  getUnreadCount() {
    return Request({ url: '/api/message/unreadCount', method: 'get' })
  },
  getConversation(targetUserId, params) {
    return Request({ 
      url: '/api/message/conversation', 
      method: 'get', 
      params: { targetUserId, ...params } 
    })
  }
}

const UserApi = {
  getBalance() {
    return Request({ url: '/admin/user/balance', method: 'get' })
  },
  /** 分页用户（管理员）；role=0 为普通用户 */
  page(params) {
    return Request({ url: '/admin/user/page', method: 'get', params })
  },
  deleteUser(id) {
    return Request({ url: '/admin/user/delete', method: 'get', params: { id } })
  },
  /** 更新当前用户资料（需传 userId，后端会校验身份并忽略敏感字段） */
  update(data) {
    return Request({ url: '/admin/user/update', method: 'post', data })
  }
}

const ForumApi = {
  boards() {
    return Request({ url: '/api/forum/boards', method: 'get' })
  },
  topicPage(params) {
    return Request({ url: '/api/forum/topic/page', method: 'get', params })
  },
  topicDetail(params) {
    return Request({ url: '/api/forum/topic/detail', method: 'get', params })
  },
  addTopic(data) {
    return Request({ url: '/api/forum/topic/add', method: 'post', data })
  },
  updateTopic(data) {
    return Request({ url: '/api/forum/topic/update', method: 'post', data })
  },
  deleteTopic(topicId) {
    return Request({ url: '/api/forum/topic/delete', method: 'get', params: { topicId } })
  },
  addReply(data) {
    return Request({ url: '/api/forum/reply/add', method: 'post', data })
  },
  deleteReply(replyId) {
    return Request({ url: '/api/forum/reply/delete', method: 'get', params: { replyId } })
  }
}

export { OrderApi, BookWantedApi, MessageApi, UserApi, ForumApi }
