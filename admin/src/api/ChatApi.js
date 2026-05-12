import request from '@/utils/request'

class ChatApi {
  // 获取用户所有会话
  async getSessions() {
    try {
      const response = await request({
        url: '/api/ai/sessions',
        method: 'get'
      })
      return response
    } catch (error) {
      console.error('获取会话列表失败:', error)
      return { status: -1, msg: '获取会话列表失败', data: [] }
    }
  }

  // 获取单个会话详情(含消息)
  async getSession(sessionId) {
    try {
      const response = await request({
        url: `/api/ai/sessions/${sessionId}`,
        method: 'get'
      })
      return response
    } catch (error) {
      console.error('获取会话详情失败:', error)
      return { status: -1, msg: '获取会话详情失败' }
    }
  }

  // 创建新会话
  async createSession(title) {
    try {
      const response = await request({
        url: '/api/ai/sessions',
        method: 'post',
        data: title
      })
      return response
    } catch (error) {
      console.error('创建会话失败:', error)
      return { status: -1, msg: '创建会话失败' }
    }
  }

  // 删除会话
  async deleteSession(sessionId) {
    try {
      const response = await request({
        url: `/api/ai/sessions/${sessionId}`,
        method: 'delete'
      })
      return response
    } catch (error) {
      console.error('删除会话失败:', error)
      return { status: -1, msg: '删除会话失败' }
    }
  }

  // 发送消息并获取AI回复(新接口)
  async sendMessage(sessionId, content) {
    try {
      const response = await request({
        url: '/api/ai/chat',
        method: 'post',
        data: { sessionId, content }
      })
      return response
    } catch (error) {
      console.error('发送消息失败:', error)
      return { status: -1, msg: '发送消息失败' }
    }
  }
}

export default new ChatApi()
