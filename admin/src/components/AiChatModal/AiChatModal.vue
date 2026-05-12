<template>
  <div v-if="visible" class="ai-chat-modal-overlay" @click.self="handleClose">
    <div class="ai-chat-modal">
      <div class="ai-chat-modal__header">
        <div class="ai-chat-modal__title">
          <el-icon class="ai-chat-modal__icon"><ChatDotRound /></el-icon>
          <span>AI 书籍助手</span>
        </div>
        <el-button class="ai-chat-modal__close" @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="ai-chat-modal__body">
        <div class="ai-chat-messages" ref="messagesContainer">
          <!-- 系统消息 -->
          <div class="ai-chat-message ai-chat-message--system">
            <div class="ai-chat-message__bubble">
              您好！我是您的AI书籍助手，可以帮您：
              <ul>
                <li>📚 推荐适合您的书籍</li>
                <li>💬 讨论书籍内容和见解</li>
                <li>❓ 解答关于书籍的问题</li>
              </ul>
              请随时向我提问！
            </div>
          </div>
          
          <!-- 历史消息 -->
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            class="ai-chat-message"
            :class="{ 'ai-chat-message--user': msg.isUser }"
          >
            <div class="ai-chat-message__avatar">
              <el-icon v-if="msg.isUser"><User /></el-icon>
              <el-icon v-else><ChatDotRound /></el-icon>
            </div>
            <div class="ai-chat-message__content">
              <div class="ai-chat-message__bubble">{{ msg.content }}</div>
              <div class="ai-chat-message__time">{{ msg.time }}</div>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="ai-chat-message ai-chat-message--loading">
            <div class="ai-chat-message__avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="ai-chat-message__content">
              <div class="ai-chat-message__bubble ai-chat-message__bubble--loading">
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="ai-chat-modal__footer">
        <div class="ai-chat-input-wrap">
          <textarea 
            v-model="inputMessage" 
            class="ai-chat-input"
            placeholder="输入您的问题，例如：推荐一些科幻小说..."
            maxlength="1000"
            @keyup.enter.ctrl="handleSend"
          ></textarea>
          <div class="ai-chat-input__actions">
            <span class="ai-chat-input__count">{{ inputMessage.length }}/1000</span>
            <el-button 
              type="primary" 
              size="small" 
              @click="handleSend"
              :disabled="!inputMessage.trim() || loading"
            >
              <el-icon class="el-icon--left"><Plus /></el-icon>
              发送
            </el-button>
          </div>
        </div>
        <div class="ai-chat-quick-actions">
          <span class="ai-chat-quick-label">快捷提问：</span>
          <el-button 
            v-for="quick in quickQuestions" 
            :key="quick.text"
            text 
            size="small"
            @click="handleQuickQuestion(quick)"
          >
            {{ quick.text }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { ChatDotRound, User, Close, Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { DeepSeekApi } from '@/api/DeepSeekApi';

const props = defineProps({
  visible: Boolean
});
const emit = defineEmits(['close']);
const messages = ref([]);
const inputMessage = ref('');
const loading = ref(false);
const messagesContainer = ref(null);

const quickQuestions = [
  { text: '推荐科幻小说', action: 'recommend', params: '喜欢科幻小说' },
  { text: '讨论《三体》', action: 'discuss', params: { bookName: '三体', topic: '书中的黑暗森林法则' } },
  { text: '推荐编程书籍', action: 'recommend', params: '学习编程入门' },
  { text: '讨论《活着》', action: 'discuss', params: { bookName: '活着', topic: '福贵的人生经历' } }
];

function handleClose() {
  emit('close');
}

function formatTime(date) {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

async function handleSend() {
  const content = inputMessage.value.trim();
  if (!content || loading.value) return;
  
  messages.value.push({
    isUser: true,
    content: content,
    time: formatTime(new Date())
  });
  inputMessage.value = '';
  loading.value = true;
  await scrollToBottom();
  
  try {
    const res = await DeepSeekApi.chat(content);
    if (res.status === 0 && res.data) {
      messages.value.push({
        isUser: false,
        content: res.data,
        time: formatTime(new Date())
      });
    } else {
      messages.value.push({
        isUser: false,
        content: '抱歉，我暂时无法回答这个问题。',
        time: formatTime(new Date())
      });
    }
  } catch (error) {
    console.error('AI调用失败:', error);
    messages.value.push({
      isUser: false,
      content: '网络错误，请稍后重试。',
      time: formatTime(new Date())
    });
    ElMessage.error('AI调用失败，请检查网络连接');
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
}

async function handleQuickQuestion(quick) {
  inputMessage.value = quick.text;
  await handleSend();
}

async function scrollToBottom() {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
}
</script>

<style lang="scss" scoped>
$wine: #7b1f2a;
$wine-soft: rgba(123, 31, 42, 0.08);
$paper: #f6f1e9;

.ai-chat-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.ai-chat-modal {
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-chat-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0ebe3;
  background: linear-gradient(135deg, $wine 0%, #9a2f3c 100%);
  border-radius: 16px 16px 0 0;
}

.ai-chat-modal__title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.ai-chat-modal__icon {
  font-size: 20px;
}

.ai-chat-modal__close {
  padding: 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.ai-chat-modal__body {
  flex: 1;
  overflow: hidden;
}

.ai-chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: $paper;
}

.ai-chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  
  &--system {
    justify-content: center;
    
    .ai-chat-message__bubble {
      background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
      color: #4338ca;
      max-width: 80%;
      text-align: left;
    }
  }
  
  &--user {
    flex-direction: row-reverse;
    
    .ai-chat-message__content {
      align-items: flex-end;
    }
    
    .ai-chat-message__bubble {
      background: $wine;
      color: #fff;
      border-radius: 16px 4px 16px 16px;
    }
  }
  
  &--loading {
    .ai-chat-message__bubble {
      background: #e7e5e4;
      padding: 12px 20px;
    }
  }
}

.ai-chat-message__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e7e5e4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
  color: #737373;
}

.ai-chat-message__content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.ai-chat-message__bubble {
  padding: 10px 14px;
  border-radius: 4px 16px 16px 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  line-height: 1.6;
  
  &--loading {
    display: flex;
    gap: 4px;
  }
}

.ai-chat-message__time {
  font-size: 11px;
  color: #9ca3af;
}

.loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;
  animation: loadingPulse 1.4s infinite ease-in-out;
  
  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes loadingPulse {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.ai-chat-modal__footer {
  padding: 16px;
  border-top: 1px solid #f0ebe3;
  background: #fff;
  border-radius: 0 0 16px 16px;
}

.ai-chat-input-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-chat-input {
  width: 100%;
  min-height: 60px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  background: #fafafa;
  transition: border-color 0.2s;
  
  &:focus {
    outline: none;
    border-color: $wine;
    background: #fff;
  }
}

.ai-chat-input__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.ai-chat-input__count {
  font-size: 12px;
  color: #9ca3af;
}

.ai-chat-quick-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.ai-chat-quick-label {
  font-size: 12px;
  color: #9ca3af;
}
</style>
