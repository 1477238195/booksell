<template>
  <div class="barcode-scanner">
    <!-- 模态框 -->
    <el-dialog
      :visible="visible"
      title="ISBN条形码识别"
      width="500px"
      :close-on-click-modal="false"
      @close="handleClose"
      @update:visible="emit('update:visible', $event)"
    >
      <div class="scanner-container">
        <!-- 上传区域 -->
        <div class="upload-section">
          <div 
            class="upload-area" 
            @click="triggerFileInput"
            :class="{ 'upload-area--dragover': isDragover }"
            @dragover.prevent="isDragover = true"
            @dragleave="isDragover = false"
            @drop.prevent="handleDrop"
          >
            <div class="upload-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <p class="upload-text">点击或拖拽上传条形码图片</p>
            <p class="upload-hint">支持 JPG、PNG、GIF，不超过 5MB</p>
            <input 
              ref="fileInput"
              type="file" 
              accept="image/jpeg,image/png,image/gif"
              class="file-input"
              @change="handleFileSelect"
            />
          </div>
        </div>

        <!-- 预览区域 -->
        <div v-if="selectedImage" class="preview-section">
          <h3 class="preview-title">图片预览</h3>
          <div class="preview-image-wrap">
            <el-image
              :src="selectedImage"
              fit="contain"
              class="preview-image"
            />
          </div>
          <el-button 
            type="text" 
            class="remove-btn"
            @click="clearSelection"
          >
            重新选择
          </el-button>
        </div>

        <!-- 识别结果 -->
        <div v-if="isScanning" class="scanning-section">
          <div class="scanning-animation">
            <div class="scanning-circle"></div>
            <span class="scanning-text">正在识别...</span>
          </div>
        </div>

        <div v-else-if="scanResult" class="result-section">
          <div class="result-card" :class="scanSuccess ? 'result-card--success' : 'result-card--error'">
            <div class="result-icon">{{ scanSuccess ? '✓' : '✗' }}</div>
            <div class="result-content">
              <h3 class="result-title">{{ scanSuccess ? '识别成功' : '识别失败' }}</h3>
              <p v-if="scanSuccess" class="result-isbn">
                ISBN: <span class="isbn-value">{{ scanResult }}</span>
              </p>
              <p v-else class="result-error">{{ scanError }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            type="primary" 
            :disabled="!scanSuccess || isScanning"
            @click="confirmResult"
          >
            使用此ISBN
          </el-button>
          <el-button @click="handleClose">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'success']);

const fileInput = ref(null);
const selectedImage = ref(null);
const isDragover = ref(false);
const isScanning = ref(false);
const scanResult = ref(null);
const scanSuccess = ref(false);
const scanError = ref('');

// 监听visible变化
watch(() => props.visible, (newVal) => {
  console.log('BarcodeScanner visible changed:', newVal);
  if (newVal) {
    // 打开时重置状态
    clearSelection();
  }
});

let ZXing = null;

// 动态加载ZXing库
const loadZXing = async () => {
  if (!ZXing) {
    ZXing = (await import('@zxing/library')).BrowserMultiFormatReader;
  }
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileSelect = (event) => {
  const file = event.target.files?.[0];
  if (file) {
    processFile(file);
  }
};

const handleDrop = (event) => {
  isDragover.value = false;
  const file = event.dataTransfer.files?.[0];
  if (file && file.type.startsWith('image/')) {
    processFile(file);
  }
};

const processFile = (file) => {
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    scanError.value = '文件大小超过5MB';
    scanSuccess.value = false;
    scanResult.value = null;
    return;
  }

  // 预览图片
  const reader = new FileReader();
  reader.onload = (e) => {
    selectedImage.value = e.target.result;
    scanBarcode(e.target.result);
  };
  reader.readAsDataURL(file);
};

const scanBarcode = async (imageData) => {
  isScanning.value = true;
  scanResult.value = null;
  scanSuccess.value = false;
  scanError.value = '';

  try {
    await loadZXing();
    
    const codeReader = new ZXing.BrowserMultiFormatReader();
    
    // 创建一个临时canvas来处理图片
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    
    await new Promise((resolve, reject) => {
      img.onload = resolve;
      img.onerror = reject;
      img.src = imageData;
    });

    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    // 调整图片大小以提高识别速度
    const maxSize = 800;
    let width = img.width;
    let height = img.height;
    
    if (width > maxSize || height > maxSize) {
      if (width > height) {
        height = (height / width) * maxSize;
        width = maxSize;
      } else {
        width = (width / height) * maxSize;
        height = maxSize;
      }
    }
    
    canvas.width = width;
    canvas.height = height;
    ctx.drawImage(img, 0, 0, width, height);

    const imageData = ctx.getImageData(0, 0, width, height);
    const result = await codeReader.decodeFromImageData(imageData);

    if (result) {
      const decodedText = result.text;
      // 验证是否为ISBN格式
      if (isValidISBN(decodedText)) {
        scanResult.value = decodedText;
        scanSuccess.value = true;
      } else {
        // 如果不是标准ISBN，也返回结果供用户参考
        scanResult.value = decodedText;
        scanSuccess.value = true;
      }
    } else {
      scanError.value = '未检测到条形码，请确保图片清晰且条形码完整';
    }
  } catch (error) {
    console.error('扫描错误:', error);
    scanError.value = '识别失败，请尝试重新上传图片';
  } finally {
    isScanning.value = false;
  }
};

const isValidISBN = (text) => {
  // ISBN-10 或 ISBN-13格式
  const isbnRegex = /^(97(8|9))?\d{9}(\d|X)$/i;
  return isbnRegex.test(text.replace(/-/g, '').replace(/\s/g, ''));
};

const clearSelection = () => {
  selectedImage.value = null;
  scanResult.value = null;
  scanSuccess.value = false;
  scanError.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const confirmResult = () => {
  if (scanSuccess.value && scanResult.value) {
    emit('success', scanResult.value);
    handleClose();
  }
};

const handleClose = () => {
  clearSelection();
  emit('update:visible', false);
};

onMounted(() => {
  // 预加载ZXing库
  loadZXing();
});

onUnmounted(() => {
  // 清理资源
});
</script>

<style scoped>
.barcode-scanner {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.scanner-container {
  padding: 20px 0;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.upload-area--dragover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.upload-icon {
  color: #999;
  margin-bottom: 16px;
  transition: color 0.3s ease;
}

.upload-area:hover .upload-icon {
  color: #667eea;
}

.upload-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px;
}

.upload-hint {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.file-input {
  display: none;
}

.preview-section {
  margin-bottom: 20px;
}

.preview-title {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px;
}

.preview-image-wrap {
  max-height: 200px;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
}

.preview-image {
  width: 100%;
  max-height: 200px;
}

.remove-btn {
  color: #667eea;
  font-size: 13px;
  margin-top: 8px;
  padding: 0;
}

.scanning-section {
  text-align: center;
  padding: 40px 0;
}

.scanning-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.scanning-circle {
  width: 50px;
  height: 50px;
  border: 3px solid #667eea;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.scanning-text {
  font-size: 14px;
  color: #666;
}

.result-section {
  margin-top: 20px;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
}

.result-card--success {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.result-card--error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.result-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.result-card--success .result-icon {
  background: #10b981;
  color: #fff;
}

.result-card--error .result-icon {
  background: #ef4444;
  color: #fff;
}

.result-content {
  flex: 1;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
}

.result-card--success .result-title {
  color: #059669;
}

.result-card--error .result-title {
  color: #dc2626;
}

.result-isbn {
  font-size: 14px;
  color: #333;
  margin: 0;
}

.isbn-value {
  font-weight: 600;
  color: #667eea;
  font-family: monospace;
  font-size: 16px;
}

.result-error {
  font-size: 14px;
  color: #dc2626;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>