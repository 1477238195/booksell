import request from '@/utils/request'

class DeepSeekApi {
  async chat(message) {
    console.log('=== AI聊天请求开始 ===');
    console.log('请求URL:', '/api/ai/chat');
    console.log('请求数据:', message);
    
    try {
      const response = await request({
        url: '/api/ai/chat',
        method: 'post',
        headers: {
          'Content-Type': 'text/plain;charset=UTF-8'
        },
        data: message
      });
      
      console.log('响应数据:', response);
      return response;
    } catch (error) {
      console.error('请求失败:', error);
      return { status: -1, msg: '网络请求失败: ' + (error.message || error) };
    }
  }
  
  async recommend(bookName) {
    console.log('=== AI推荐请求开始 ===');
    console.log('请求URL:', '/api/ai/recommend');
    console.log('请求数据:', bookName);
    
    try {
      const response = await request({
        url: '/api/ai/recommend',
        method: 'post',
        headers: {
          'Content-Type': 'text/plain;charset=UTF-8'
        },
        data: bookName
      });
      
      console.log('响应数据:', response);
      return response;
    } catch (error) {
      console.error('请求失败:', error);
      return { status: -1, msg: '网络请求失败: ' + (error.message || error) };
    }
  }
  
  async discuss(bookName, question) {
    console.log('=== AI讨论请求开始 ===');
    console.log('请求URL:', '/api/ai/discuss');
    
    try {
      const response = await request({
        url: '/api/ai/discuss',
        method: 'post',
        params: { bookName: bookName },
        data: question
      });
      
      console.log('响应数据:', response);
      return response;
    } catch (error) {
      console.error('请求失败:', error);
      return { status: -1, msg: '网络请求失败: ' + (error.message || error) };
    }
  }
}

export default new DeepSeekApi();
