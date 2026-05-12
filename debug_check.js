/**
 * 前端调试检查脚本
 * 用于检查书籍相关API的数据流
 */

// 检查登录状态
console.log('=== 用户登录状态检查 ===');
const userInfo = localStorage.getItem('userInfo');
console.log('localStorage.userInfo:', userInfo);

// 检查 JWT Token
const token = localStorage.getItem('token');
console.log('localStorage.token:', token);

// 检查用户信息
const userStore = JSON.parse(userInfo || '{}');
console.log('用户ID:', userStore.userId);

// 检查书籍API调用
console.log('=== 书籍API调用测试 ===');

// 测试获取书籍列表
fetch('/admin-api/api/book/page?page=1&size=10', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('书籍列表API响应:', data);
  console.log('书籍总数:', data.data?.total || 0);
  console.log('书籍列表:', data.data?.list || []);
})
.catch(error => {
  console.error('API调用错误:', error);
});

// 测试获取分类列表
fetch('/admin-api/api/bookCategory/list', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('分类列表API响应:', data);
  console.log('分类数量:', data.data?.length || 0);
})
.catch(error => {
  console.error('分类API调用错误:', error);
});