import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import process from 'node:process'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import Inspect from 'vite-plugin-inspect'
// import vueDevTools from 'vite-plugin-vue-devtools'

/** dev server 启动时间戳，用于打破浏览器 HTTP 缓存 */
const BUILD_TIMESTAMP = Date.now()

/** 与 Spring Boot（默认 8888）对齐；dev / preview 共用，避免 preview 无代理导致 API 404 */
const devProxy = {
  '/admin-api': {
    target: 'http://localhost:8888',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/admin-api/, '')
  },
  '/api': {
    target: 'http://localhost:8888',
    changeOrigin: true
  },
  '/upload': {
    target: 'http://localhost:8888',
    changeOrigin: true
  }
}

export default (mode) => {
  return defineConfig({
    base: '/',
    server: {
      open: true,
      proxy: devProxy,
      hmr: {
        overlay: true
      },
      /** 禁用持久化缓存，每次重启 dev server 强制拉取最新 chunk */
      cache: false
    },
    preview: {
      proxy: devProxy
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    esbuild: {
      drop:
        loadEnv(mode, process.cwd()).VITE_NODE_ENV === 'production'
          ? ['console', 'debugger']
          : []
    },
    plugins: [
      vue(),
      // vueDevTools(),
      Inspect(),
      createSvgIconsPlugin({
        // 指定需要缓存的图标文件夹
        iconDirs: [fileURLToPath(new URL('./src/icons', import.meta.url))],
        // 指定symbolId格式
        symbolId: 'icon-[dir]-[name]'

        /**
         * 自定义插入位置
         * @default: body-last
         */
        // inject?: 'body-last' | 'body-first'

        /**
         * custom dom id
         * @default: __svg__icons__dom__
         */
        // customDomId: '__svg__icons__dom__',
      }),
      AutoImport({
        resolvers: [ElementPlusResolver()]
      }),
      Components({
        resolvers: [ElementPlusResolver()]
      })
    ],
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler' // or 'modern'
        }
      }
    }
  })
}
