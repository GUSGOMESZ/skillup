import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// O front so conhece o gateway: tudo que comeca com /api vai para ele
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
