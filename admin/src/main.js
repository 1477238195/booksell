import { createApp } from 'vue'
import 'vue-cropper/dist/index.css'
import 'normalize.css'

import App from '@/App'
import { createPinia } from 'pinia'
import router from '@/router/index'
import 'element-plus/dist/index.css'
import '@/styles/market-theme.scss'
import '@/styles/index.scss'
import 'virtual:svg-icons-register'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import ElementPlus from 'element-plus'
import VueCropper from 'vue-cropper'
import '@/permission'
import directives from '@/directives/index'
import autoUpdate from '@/utils/auto-update'

const app = createApp(App)

app
  .use(createPinia())
  .use(router)
  .use(directives)
  .use(autoUpdate)
  .component('Plus', ElementPlusIconsVue.Plus)
  .use(ElementPlus, { locale: zhCn })
  .use(VueCropper)
  .mount('#app')
